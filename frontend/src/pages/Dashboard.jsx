import { useEffect, useState } from "react"
import api from "../services/api"

function Dashboard() {

    const [user, setUser] = useState(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState("")

    useEffect(() => {

        const fetchUser = async () => {
            try {
                const res = await api.get("/user/me")
                setUser(res.data)

            } catch (err) {
                setError("Failed to load user info")

            } finally {
                setLoading(false)

            }
        }

        fetchUser()

    }, [])

    if (loading) {
        return <h1>Loading...</h1>
    }

    if (error) {
        return <h1>{error}</h1>
    }

    return (
        <div className="dashboard">
            <div className="dashboard-card">
                <h1 className="dashboard-title">Dashboard</h1>

                <div className="profile-section">
                    <div className="rank-circle">{user.rank}</div>

                    <div className="profile-info">
                        <div className="username-row">
                            <span className="username"> {user.username} </span>

                            {user.role === "ADMIN" && (
                                <span className="admin-tag"> ADMIN </span>
                            )}
                        </div>

                        <span className="user-id"> ID: {user.id} </span>
                    </div>
                </div>

                <hr className="divider" />

                <div className="info-list">
                    <div className="info-item">
                        <span className="label"> Account Date </span>

                        <span> {user.account_create_date} </span>
                    </div>

                    <div className="info-item">
                        <span className="label"> Email </span>

                        <span> {user.email} </span>
                    </div>

                    <div className="info-item">
                        <span className="label"> Total Matches </span>

                        <span> {user.total_matches} </span>
                    </div>
                </div>
            </div>
        </div>

    )
}

export default Dashboard