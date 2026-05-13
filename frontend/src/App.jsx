import { Routes, Route } from "react-router-dom"
import Login from "./pages/Login"
import Register from "./pages/Register"
import Dashboard from "./pages/Dashboard"
import Profile from "./pages/Profile"
import Problem from "./pages/Problem"
import ProblemReview from "./pages/ProblemReview"
import ProblemReviewDetail from "./pages/ProblemReviewDetail"
import MatchPage from "./pages/MatchPage"

function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/" element={<Dashboard />} />
      <Route path="/user/:username" element={<Profile />} />
      <Route path="/problem" element={<Problem />} />
      <Route path="/problem_review" element={<ProblemReview />} />
      <Route path="/review/:id" element={<ProblemReviewDetail />} />
      <Route path="/match" element={<MatchPage />} />
    </Routes>
  )
}

export default App
