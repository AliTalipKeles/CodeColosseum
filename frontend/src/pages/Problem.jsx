import { useEffect, useState } from "react"
import "./Problem.css"
import { Link } from "react-router-dom"
import api from "../services/api"
import { useNavigate } from "react-router-dom"

function Problem(){
    const navigate = useNavigate();
    let prob_id = null
    const [formData, setFormData] = useState({
        title: '',
        statement: '',
        input_format: '',
        output_format: '',
        constraints: '',
        difficulty: ''
    });

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        
        try {
            const response = await api.post('/problem/createrequest', formData);
            alert('Problem submitted successfully!');
            setFormData({
                title: '',
                statement: '',
                input_format: '',
                output_format: '',
                constraints: '',
                difficulty: ''
            });

            prob_id = response.data.data
            console.log(prob_id)

            // navigate('/dashboard');
        } catch (error) {
            console.error('Error submitting problem:', error);
            alert('Failed to submit problem. Please try again.');
        }
    };

    return (
        <div className="problem-page">
            <div className="form-container">
                <div className="form-title">Suggest a Problem</div>
                
                <div>
                    <div className="form-group">
                        <label htmlFor="title">Title</label>
                        <input 
                            type="text" 
                            id="title" 
                            name="title" 
                            value={formData.title}
                            onChange={handleChange}
                            required 
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="statement">Problem Statement</label>
                        <textarea 
                            id="statement" 
                            name="statement" 
                            value={formData.statement}
                            onChange={handleChange}
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="input_format">Input Format</label>
                        <textarea 
                            id="input_format" 
                            name="input_format" 
                            value={formData.input_format}
                            onChange={handleChange}
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="output_format">Output Format</label>
                        <textarea 
                            id="output_format" 
                            name="output_format" 
                            value={formData.output_format}
                            onChange={handleChange}
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="constraints">Constraints</label>
                        <textarea 
                            id="constraints" 
                            name="constraints" 
                            value={formData.constraints}
                            onChange={handleChange}
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="difficulty">Difficulty</label>
                        <select 
                            id="difficulty" 
                            name="difficulty" 
                            value={formData.difficulty}
                            onChange={handleChange}
                            required
                        >
                            <option value="">Select difficulty...</option>
                            <option value="EASY">Easy</option>
                            <option value="MEDIUM">Medium</option>
                            <option value="HARD">Hard</option>
                        </select>
                    </div>

                    <button 
                        type="button"
                        className="submit-button"
                        onClick={handleSubmit}
                    >
                        Submit Problem
                    </button>
                    <button 
                        type="button" 
                        className="cancel-button" 
                        onClick={() => navigate(-1)}
                    >
                        Cancel
                    </button>
                </div>
            </div>
        </div>
    )
}

export default Problem