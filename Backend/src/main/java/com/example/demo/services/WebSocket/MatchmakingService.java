package com.example.demo.services.WebSocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;

import com.example.demo.dtos.MatchDto;
import com.example.demo.dtos.MatchmakingDto;
import com.example.demo.repositories.MatchRepository;

@Service
public class MatchmakingService {
    
    private final Queue<MatchmakingDto> waitingQueue;
    private final MatchRepository matchRepository;
    private final Object lock = new Object();
    private final MatchService matchService;
    public MatchmakingService(MatchService matchService,MatchRepository matchRepository) {
        this.waitingQueue = new ConcurrentLinkedQueue<>();
        this.matchRepository = matchRepository;
        this.matchService = matchService;
    }

    public void addPlayer(MatchmakingDto player) {
        waitingQueue.offer(player);
        System.out.printf("Kuyruğa eklendi: %s (elo: %d) | Kuyruk: %d%n",
            player.getUsername(), player.getElo(), waitingQueue.size());
    }

    public void removePlayer(String sessionId) {
        boolean isRemoved = waitingQueue.removeIf(p -> p.getSessionId().equals(sessionId));
        if(isRemoved){
            System.out.printf("Kuyruktan çıkarıldı : Kuyruk%d%n",waitingQueue.size());
        }
    }

    @Scheduled(fixedDelay = 2000)
    public void processMatches() throws IOException {
        synchronized (lock) {
            while (waitingQueue.size() >= 2) {
                MatchmakingDto player = waitingQueue.poll();
                if (player == null) break;

                if (!player.getSession().isOpen()) continue;
            
                MatchmakingDto opponent = null;
                List<MatchmakingDto> skipped = new ArrayList<>();

                while (!waitingQueue.isEmpty()) {
                    MatchmakingDto candidate = waitingQueue.poll();

                    if (!candidate.getSession().isOpen()) continue;

                    if (player.canMatchWith(candidate)) {
                        opponent = candidate;
                        break;
                    } else {
                        skipped.add(candidate);
                    }
                }

                waitingQueue.addAll(skipped);

                if (opponent != null) {
                    notifyMatch(player, opponent);
                } else {
                    waitingQueue.offer(player);
                }
            }
        }
    }

    private void notifyMatch(MatchmakingDto p1, MatchmakingDto p2) throws IOException {
        String matchId = UUID.randomUUID().toString();
        String difficulty = calculateDifficulty(p1.getElo(), p2.getElo());
        
        MatchDto matchDto = matchRepository.createMatch(matchId, p1.getId(), p2.getId(),difficulty);
        
        if(!(matchDto == null)){
            String msg1 = String.format(
            "{\"event\":\"MATCHED\",\"matchId\":\"%s\",\"opponent\":\"%s\",\"opponentElo\":%d}",
            matchId, p2.getUsername(), p2.getElo()
            );
            String msg2 = String.format(
                "{\"event\":\"MATCHED\",\"matchId\":\"%s\",\"opponent\":\"%s\",\"opponentElo\":%d}",
                matchId, p1.getUsername(), p1.getElo()
            );

            p1.getSession().sendMessage(new TextMessage(msg1));
            p2.getSession().sendMessage(new TextMessage(msg2));

            p1.getSession().close();
            p2.getSession().close();


            System.out.printf("Eşleşti: %s (elo:%d) vs %s (elo:%d) | matchId: %s%n",
                p1.getUsername(), p1.getElo(),
                p2.getUsername(), p2.getElo(), matchId);
            
            matchService.addMatch(matchDto);
            
        }else{
            addPlayer(p1);
            addPlayer(p2);
        }
        
    }

    public boolean isInQueue(String username){
        if(waitingQueue.stream().anyMatch(p -> p.getUsername().equals(username))){
            return true;
        }else{
            return false;
        }
    }

    private String calculateDifficulty(int elo1, int elo2) {
    double avg = (elo1 + elo2) / 2.0;  // 2.0 önemli — int bölme truncate eder
    if (avg <= 1200) return "EASY";
    if (avg <= 1500) return "MEDIUM";  // < 1500 yeterli, > 1200 zaten sağlandı
    return "HARD";
}
}
