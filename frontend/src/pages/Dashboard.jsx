import { useEffect, useState, useRef } from "react"
import "./Dashboard.css"
import { Link } from "react-router-dom"
import api from "../services/api"
import { useNavigate } from "react-router-dom"

function Dashboard() {

    const [user, setUser] = useState(null)
    const [leaderboard, setLeaderboard] = useState(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState("")
    const navigate = useNavigate()

    // idle, connecting, searching, found, error
    const [matchmakingStatus, setMatchmakingStatus] = useState('idle')
    const [matchData, setMatchData] = useState(null)
    const [queueTime, setQueueTime] = useState(0)
    const wsRef = useRef(null)
    const queueTimerRef = useRef(null)

    useEffect(() => {

        const fetchUser = async () => {
            try {
                const userinfo_res = await api.get("/user/me")
                setUser(userinfo_res.data.data)

                const leaderboard_res = await api.get("/leaderboard")
                setLeaderboard(leaderboard_res.data.data)

            } catch (err) {
                navigate("/login")
                setError("Failed to load dashboard")

            } finally {
                setLoading(false)
            }
        }

        fetchUser()

    }, [])

    useEffect(() => {
        return () => {
            if (wsRef.current?.readyState == WebSocket.OPEN) {
                wsRef.current.close()
            }
            if (queueTimerRef.current) {
                clearInterval(queueTimerRef.current)
            }
        }
    }, [])

    useEffect(() => {
        if (matchmakingStatus == 'searching') {
            queueTimerRef.current = setInterval(() => {
                setQueueTime(prev => prev + 1)
            }, 1000)
        } else {
            if (queueTimerRef.current) {
                clearInterval(queueTimerRef.current)
            }
            setQueueTime(0)
        }

        return () => {
            if (queueTimerRef.current) {
                clearInterval(queueTimerRef.current)
            }
        }

    }, [matchmakingStatus])

    function handleLogout() {
        localStorage.removeItem("token")
        navigate("/login")
    }

    function gotoProbPage() {
        navigate("/problem")
    }

    function gotoProbReviewPage() {
        navigate("/problem_review")
    }

    function connectToMatchmaking() {
        const token = localStorage.getItem("token")
        if (!token) {
            setError("Authentication token not found")
            return
        }

        setMatchmakingStatus('connecting')

        const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
        const wsHost = import.meta.env.VITE_WS_URL || 'localhost:8080'
        const wsUrl = `${wsProtocol}//${wsHost}/matchmaking?token=${token}`

        console.log('Connecting to WebSocket:', wsUrl)

        const ws = new WebSocket(wsUrl)
        wsRef.current = ws

        ws.onopen = () => {
            console.log('WebSocket connected')
            setMatchmakingStatus('searching')
            setError("")
        }

        ws.onmessage = (event) => {
            try {
                const data = JSON.parse(event.data)
                console.log('Received message:', data)

                switch (data.event) {
                    case 'CONNECTED':
                        console.log('Successfully joined matchmaking queue')
                        break

                    case 'MATCHED':
                        setMatchmakingStatus('found')
                        setMatchData(data)
                        navigate("/match")
                        break

                    case 'LEFT':
                        setMatchmakingStatus('idle')
                        ws.close()
                        break

                    case 'ERROR':
                        setMatchmakingStatus('error')
                        setError(data.message || 'An error occurred')
                        break

                    default:
                        console.log('Unknown event:', data.event)
                }
            } catch (err) {
                console.error('Error parsing message:', err)
            }
        }

        ws.onerror = (error) => {
            console.error('WebSocket error:', error)
            setMatchmakingStatus('error')
            setError('Connection error occurred')
        }

        ws.onclose = (event) => {
            console.log('WebSocket closed:', event.code, event.reason)

            if (matchmakingStatus !== 'error' && matchmakingStatus !== 'found') {
                setMatchmakingStatus('idle')
            }

            if (event.code === 1006) {
                setError('Connection lost. Please try again.')
            } else if (event.code === 1003) {
                setError('Already in queue or invalid connection')
            }
        }
    }

    function leaveMatchmaking() {
        if (wsRef.current?.readyState === WebSocket.OPEN) {
            wsRef.current.send(JSON.stringify({ action: 'LEAVE' }))
        } else {
            setMatchmakingStatus('idle')
        }
    }

    function handleFindMatch() {
        if (matchmakingStatus === 'idle' || matchmakingStatus === 'error') {
            connectToMatchmaking()
        } else if (matchmakingStatus === 'searching') {
            leaveMatchmaking()
        }
    }

    function formatQueueTime(seconds) {
        const mins = Math.floor(seconds / 60)
        const secs = seconds % 60
        return `${mins}:${secs.toString().padStart(2, '0')}`
    }

    function renderMatchmakingStatus() {
        switch (matchmakingStatus) {
            case 'connecting':
                return <div className="matchmaking-status">Connecting to matchmaking...</div>

            case 'searching':
                return (
                    <div className="matchmaking-status searching">
                        <div className="searching-animation">
                            <div className="spinner"></div>
                            <div>Searching for opponent...</div>
                        </div>
                        <div className="queue-time">Time in queue: {formatQueueTime(queueTime)}</div>
                        <div className="elo-range">Your ELO: {user?.elo}</div>
                    </div>
                )

            case 'found':
                return (
                    <div className="matchmaking-status found">
                        <div className="match-found-message">Match Found!</div>
                        {matchData && (
                            <div className="match-details">
                                <p>Opponent: {matchData.opponentUsername || 'Loading...'}</p>
                                <p>Opponent ELO: {matchData.opponentElo || 'N/A'}</p>
                            </div>
                        )}
                    </div>
                )

            case 'error':
                return <div className="matchmaking-status error">{error}</div>

            default:
                return null
        }
    }

    if (loading) {
        return <h1>Loading...</h1>
    }

    if (error) {
        return <h1>{error}</h1>
    }

    let normalised_date = user.account_create_date.slice(0, 10);

    return (
        <div>
            <div className="user-info-card">
                <div className="user-info-title">
                    <div className={user.role == "USER" ? "username" : "admin-username"}>{user.username}</div>
                    <div className="id">{user.id}</div>
                    <div className="elo">{user.elo}</div>
                </div>
                <hr></hr>
                <div>Total matches: {user.total_matches}</div>
                <div>Email: {user.email}</div>
                <div>Account date: {normalised_date}</div>
            </div>

            {matchmakingStatus !== 'idle' && (
                <div className="matchmaking-container">
                    {renderMatchmakingStatus()}
                </div>
            )}

            <div className="leaderboard-container">
                <h2 className="leaderboard-title">Leaderboard</h2>

                <table className="leaderboard-table">
                    <thead>
                        <tr>
                            <th>Rank</th>
                            <th>Username</th>
                            <th>ELO</th>
                        </tr>
                    </thead>

                    <tbody>
                        {leaderboard.map((user, index) => (
                            <tr key={user.username}>
                                <td>#{index + 1}</td>
                                <td className={
                                    index === 0
                                        ? "first"
                                        : index === 1
                                            ? "second"
                                            : index === 2
                                                ? "third"
                                                : ""
                                }>
                                    <Link to={`/user/${user.username}`} className="username-link">
                                        {user.username}
                                    </Link>
                                </td>
                                <td>{user.elo}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
            <div display="flex">
                <button className="logout-button" onClick={handleLogout}>Log out</button>
                <button className="logout-button" onClick={gotoProbPage}>Suggest Problem</button>
                {user.role == "ADMIN" && <button className="logout-button" onClick={gotoProbReviewPage}>Review Problems</button>}
                <button
                    className={`logout-button ${matchmakingStatus === 'searching' ? 'cancel-button' : 'find-match-button'}`}
                    onClick={handleFindMatch}
                    disabled={matchmakingStatus === 'connecting' || matchmakingStatus === 'found'}
                >
                    {matchmakingStatus === 'searching' ? 'Cancel Search' : 'Find Match'}
                </button>
            </div>

        </div>

    )
}

export default Dashboard