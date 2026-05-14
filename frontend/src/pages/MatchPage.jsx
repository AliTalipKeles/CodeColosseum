import { useEffect, useState, useRef } from "react"
import { useLocation } from "react-router-dom"
import "./Problem.css"

function MatchPage() {
    const location = useLocation()
    const matchId = location.state?.matchId || localStorage.getItem('currentMatchId')
    const token = localStorage.getItem('token')
    
    const [problem, setProblem] = useState(null)
    const [testCases, setTestCases] = useState([])
    const [connectionStatus, setConnectionStatus] = useState('connecting')
    const wsRef = useRef(null)

    useEffect(() => {
        const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
        const wsHost = import.meta.env.VITE_WS_URL || 'localhost:8080'
        const wsUrl = `${wsProtocol}//${wsHost}/match`

        const ws = new WebSocket(wsUrl)
        wsRef.current = ws

        ws.onopen = () => {
            console.log('Match WebSocket connected')
            setConnectionStatus('authenticating')
            
            // Send auth message
            ws.send(JSON.stringify({
                type: 'auth',
                token: token,
                matchId: matchId
            }))
        }

        ws.onmessage = (event) => {
            try {
                const data = JSON.parse(event.data)
                console.log('Received:', data)

                switch (data.type) {
                    case 'auth_ok':
                        setConnectionStatus('connected')
                        break

                    case 'problem_info':
                        setProblem({
                            id: data.id,
                            title: data.title,
                            difficulty: data.difficulty,
                            statement: data.statement,
                            inputFormat: data.input_format,
                            outputFormat: data.output_format,
                            constraints: data.constraints,
                            timeLimit: data.time_limit_s,
                            memoryLimit: data.memory_limit_mb,
                            status: data.status
                        })
                        break

                    case 'testcases_info':
                        setTestCases(data.test_cases || [])
                        break

                    case 'error':
                        console.error('Error:', data.message)
                        setConnectionStatus('error')
                        break

                    default:
                        console.log('Unknown message type:', data.type)
                }
            } catch (err) {
                console.error('Error parsing message:', err)
            }
        }

        ws.onerror = (error) => {
            console.error('WebSocket error:', error)
            setConnectionStatus('error')
        }

        ws.onclose = () => {
            console.log('WebSocket closed')
            setConnectionStatus('disconnected')
        }

        return () => {
            if (ws.readyState === WebSocket.OPEN) {
                ws.close()
            }
        }
    }, [matchId, token])

    if (connectionStatus === 'connecting' || connectionStatus === 'authenticating') {
        return <div className="loading">Connecting to match...</div>
    }

    if (connectionStatus === 'error') {
        return <div className="error">Failed to connect to match</div>
    }

    if (!problem) {
        return <div className="loading">Loading problem...</div>
    }

    return (
        <div className="match-page">
            <div className="match-header">
                <h1>Match in Progress</h1>
                <div className="match-id">Match ID: {matchId}</div>
            </div>

            <div className="problem-display">
                <div className="problem-card">
                    <div className="problem-header">
                        <div className="problem-name">
                            {problem.title}
                        </div>
                        <div className={`difficulty ${problem.difficulty.toLowerCase()}`}>
                            {problem.difficulty}
                        </div>
                    </div>
                    
                    <div className="problem-id">
                        {problem.id}
                    </div>
                    
                    <div className="problem-info">
                        <div>
                            <strong>Time Limit:</strong> {problem.timeLimit}s
                        </div>
                        <div>
                            <strong>Memory Limit:</strong> {problem.memoryLimit} MB
                        </div>
                    </div>

                    <div className="problem-section">
                        <h3>Problem Statement</h3>
                        <div className="problem-statement">
                            {problem.statement}
                        </div>
                    </div>

                    <div className="problem-section">
                        <h3>Constraints</h3>
                        <div className="problem-constraints">
                            {problem.constraints}
                        </div>
                    </div>

                    <div className="problem-section">
                        <h3>Input Format</h3>
                        <div className="problem-input-format">
                            {problem.inputFormat}
                        </div>
                    </div>

                    <div className="problem-section">
                        <h3>Output Format</h3>
                        <div className="problem-output-format">
                            {problem.outputFormat}
                        </div>
                    </div>
                </div>

                {testCases.length > 0 && (
                    <div className="test-cases-section">
                        <h2>Sample Test Cases</h2>
                        <div className="test-cases-list">
                            {testCases.map((testCase, index) => (
                                <div key={testCase.id} className="test-case-card">
                                    <h4>Test Case {index + 1}</h4>
                                    
                                    <div className="test-case-content">
                                        <div className="test-input">
                                            <strong>Input:</strong>
                                            <pre>{testCase.stdin}</pre>
                                        </div>
                                        
                                        <div className="test-output">
                                            <strong>Expected Output:</strong>
                                            <pre>{testCase.expected_stdout}</pre>
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                )}

                <div className="code-editor-section">
                    <h2>Your Solution</h2>
                    {/* Add your code editor component here */}
                    <textarea 
                        className="code-editor" 
                        placeholder="Write your solution here..."
                    />
                    
                    <div className="submit-section">
                        <button className="submit-button">
                            Submit Solution
                        </button>
                    </div>
                </div>
            </div>
        </div>
    )
}

export default MatchPage