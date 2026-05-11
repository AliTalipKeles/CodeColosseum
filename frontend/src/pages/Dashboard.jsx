import { useEffect, useState } from "react"
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

    function handleLogout(){
        localStorage.removeItem("token")
        navigate("/login")
    }

    function gotoProbPage(){
        navigate("/problem")
    }

    function gotoProbReviewPage(){
        navigate("/problem_review")
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
            </div>

        </div>

    )
}

export default Dashboard