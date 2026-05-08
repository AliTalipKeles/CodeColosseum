import { useState } from "react"
import api from "../services/api"
import { useNavigate } from "react-router-dom"

function Login() {
  const [username, setUsername] = useState("")
  const [password, setPassword] = useState("")
  const [error, setError] = useState("")
  const [message, setMessage] = useState("")
  const navigate = useNavigate()

  const handleLogin = async (e) => {
    e.preventDefault()
    setError("")
    setMessage("")

    try {
      const res = await api.post("/user/login", {
        username,
        password,
      })

      localStorage.setItem("token", res.data.data)

      setMessage("logged in succesfully")
      setTimeout(() => {
        navigate("/")
      }, 500)

    } catch (err) {
      setError("Invalid username or password")
      console.error("login error:", err)
    }
  }

  return (
    <div>
      <h2>Login</h2>

      <form onSubmit={handleLogin}>
        <input
          type="text"
          placeholder="Username"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
        />

        <input
          type="password"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />

        <button type="submit">Login</button>
      </form>

      <button onClick={() => navigate("/register")}>Register</button>

      {message && <p style={{ color: "green" }}>{message}</p>}
      {error && <p style={{ color: "red" }}>{error}</p>}
    </div>
  )
}

export default Login