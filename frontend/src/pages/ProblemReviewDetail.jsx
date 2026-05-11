import { useEffect, useState } from "react"
import { useParams, useNavigate } from "react-router-dom"
import api from "../services/api"
import "./ProblemReviewDetail.css"

function ProblemReviewDetail() {

    const { id } = useParams()
    const navigate = useNavigate()

    const [problem, setProblem] = useState(null)

    useEffect(() => {

        async function fetchProblem() {
            try {
                const res = await api.get(`/problem?id=${id}`)
                setProblem(res.data.data[0])
            }
            catch(err) {
                console.log(err)
            }
        }

        fetchProblem()

    }, [id])

    async function approveProblem() {
        try {
            await api.put(`/problem/setapproved/${problem.title}`)
            navigate("/problem_review")
        }
        catch(err) {
            console.log(err)
        }
    }

    async function rejectProblem() {
        try {
            await api.put(`/problem/setrejected/${problem.title}`)
            navigate("/problem_review")
        }
        catch(err) {
            console.log(err)
        }
    }

    if (!problem) {
        return <div className="loading">Loading...</div>
    }

    return(
        <div className="problem-review-container">

            <div className="review-problem-card">

                <div className="review-header">

                    <div>
                        <div className="review-title">
                            {problem.title}
                        </div>

                        <div className="review-id">
                            {problem.id}
                        </div>
                    </div>

                    <div className={`review-difficulty ${problem.difficulty}`}>
                        {problem.difficulty}
                    </div>

                </div>

                <div className="review-limits">

                    <div>
                        <strong>Time Limit:</strong> {problem.time_limit_s}s
                    </div>

                    <div>
                        <strong>Memory Limit:</strong> {problem.memory_limit_mb} MB
                    </div>

                </div>

                <div className="review-section">
                    <div className="section-title">Statement</div>
                    <div className="section-content">
                        {problem.statement}
                    </div>
                </div>

                <div className="review-section">
                    <div className="section-title">Input Format</div>
                    <div className="section-content">
                        {problem.input_format}
                    </div>
                </div>

                <div className="review-section">
                    <div className="section-title">Output Format</div>
                    <div className="section-content">
                        {problem.output_format}
                    </div>
                </div>

                <div className="review-section">
                    <div className="section-title">Constraints</div>
                    <div className="section-content">
                        {problem.constraints}
                    </div>
                </div>

                <div className="review-buttons">

                    <button
                        className="approve-button"
                        onClick={approveProblem}
                    >
                        Approve
                    </button>

                    <button
                        className="reject-button"
                        onClick={rejectProblem}
                    >
                        Reject
                    </button>

                </div>

            </div>

        </div>
    )
}

export default ProblemReviewDetail