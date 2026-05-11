import { useEffect, useState } from "react"
import { useNavigate } from "react-router-dom"
import api from "../services/api"
import "./ProblemReview.css"

function ProblemReview() {

    const [problems, setProblems] = useState([])
    const navigate = useNavigate()

    useEffect(() => {

        async function fetchProblems() {
            try {
                const res = await api.get("/problem?status=PENDING")
                setProblems(res.data.data)
            }
            catch(err) {
                console.log(err)
            }
        }

        fetchProblems()

    }, [])

    return(
        <div className="review-page">

            <div className="review-title">
                Pending Problems
            </div>

            <div className="problem-list">

                {problems.map((problem) => (

                    <div
                        key={problem.id}
                        className="problem-card"
                        onClick={() => navigate(`/review/${problem.id}`)}
                    >

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
                                <strong>Time:</strong> {problem.time_limit_s}s
                            </div>

                            <div>
                                <strong>Memory:</strong> {problem.memory_limit_mb} MB
                            </div>
                        </div>

                        <div className="problem-constraints">
                            {problem.statement}
                        </div>

                    </div>

                ))}

            </div>

        </div>
    )
}

export default ProblemReview