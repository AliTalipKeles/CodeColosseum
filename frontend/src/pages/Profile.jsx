import { useEffect, useState } from "react"
import { useParams } from "react-router-dom"
import "./Dashboard.css"
import api from "../services/api"

function Profile() {

    const { username } = useParams()
    const [user, setUser] = useState(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState("")

    useEffect(() => {

        const fetchUser = async () => {
            try {
                const userinfo_res = await api.get(`/user/${username}`)
                setUser(userinfo_res.data.data)

            } catch (err) {
                setError("Failed to load profile")

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

    let normalised_date = user.account_create_date.slice(0, 10);

    return (
        <div>
            <div className="user-info-card">
                <div className="user-info-title">
                    <div className="username">{user.username}</div>
                    <div className="id">{user.id}</div>
                    <div className="elo">{user.elo}</div>
                </div>
                <hr></hr>
                <div>Total matches: {user.total_matches}</div>
                <div>Email: {user.email}</div>
                <div>Account date: {normalised_date}</div>
            </div>

        </div>

    )
}

export default Profile