import { useState } from "react"
import api from "../services/api"

function Login() {
  const [username, setUsename] = useState("")
  const [password, setPassword] = useState("")
  const [error, setError] = useState("")

  const handleLogin = async (e) => {
    e.preventDefault()
    setError("")

    try {
      const res = await api.post("/login", {
        username,
        password,
      })

      localStorage.setItem("token", res.data.token)

      alert("Login success")
    } catch (err) {
      setError("Invalid username or password")
    }
  }

  return (
    <div>
      <h2>Login</h2>

      <form onSubmit={handleLogin}>
        <input
          type="username"
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

      {error && <p>{error}</p>}
    </div>
  )
}

export default Login