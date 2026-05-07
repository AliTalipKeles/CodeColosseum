import { useState } from "react"
import { useNavigate } from "react-router-dom"
import api from "../services/api"

function Register() {
  const [username, setUsername] = useState("")
  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [message, setMessage] = useState("")
  const [error, setError] = useState("")
  const navigate = useNavigate()

  const handleRegister = async (e) => {
    e.preventDefault()
    setMessage("")
    setError("")

    try {
      await api.post("/user/register", {
        username,
        email,
        password,
      })

      setMessage("Registered successfully! Redirecting to login...")
      
      setTimeout(() => {
        navigate("/login")
      }, 500)

    } catch (err) {
      setError("Registration failed. Please try again.")
      console.error("Registration error:", err)
    }
  }

  return (
    <div>
      <h2>Register</h2>

      <form onSubmit={handleRegister}>
        <input
          type="text"
          placeholder="Username"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          required
        />

        <input
          type="email"
          placeholder="Email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />

        <input
          type="password"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />

        <button type="submit">Register</button>
      </form>

      {message && <p style={{ color: "green" }}>{message}</p>}
      {error && <p style={{ color: "red" }}>{error}</p>}
    </div>
  )
}

export default Register