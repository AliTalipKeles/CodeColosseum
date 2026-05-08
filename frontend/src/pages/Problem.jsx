import { useState } from "react"
import "./Problem.css"
import api from "../services/api"
import { useNavigate } from "react-router-dom"

function Problem() {
    const navigate = useNavigate();

    const [formData, setFormData] = useState({
        title: '',
        statement: '',
        input_format: '',
        output_format: '',
        constraints: '',
        difficulty: ''
    });

    const [testCases, setTestCases] = useState([
        { stdin: '', expected_stdout: '' },
        { stdin: '', expected_stdout: '' },
        { stdin: '', expected_stdout: '' }
    ]);

    const handleChange = (e) => {
        const { name, value } = e.target;

        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleTestCaseChange = (index, e) => {
        const { name, value } = e.target;

        const updatedTestCases = [...testCases];

        updatedTestCases[index][name] = value;

        setTestCases(updatedTestCases);
    };

    const addTestCase = () => {
        setTestCases(prev => [
            ...prev,
            { stdin: '', expected_stdout: '' }
        ]);
    };

    const removeTestCase = (index) => {
        const updatedTestCases = testCases.filter((_, i) => i !== index);

        setTestCases(updatedTestCases);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        try {
            const problem_payload = {
                ...formData,
            };

            const problem_response = await api.post(
                '/problem/createrequest',
                problem_payload
            );
            const problem_id = problem_response.data.data;

            let testcase_responses = []
            let len = testCases.length;

            for (let i = 0; i < len; i++) {
                let testcase_payload = {
                    problem_id: problem_id,
                    stdin: testCases[i].stdin,
                    expected_stdout: testCases[i].expected_stdout
                }

                testcase_responses[i] = await api.post(
                    '/problem/addtestcase',
                    testcase_payload
                );
            }

            setFormData({
                title: '',
                statement: '',
                input_format: '',
                output_format: '',
                constraints: '',
                difficulty: ''
            });

            setTestCases([
                { stdin: '', expected_stdout: '' },
                { stdin: '', expected_stdout: '' },
                { stdin: '', expected_stdout: '' }
            ]);

            // navigate('/dashboard');

        } catch (error) {
            console.error('Error submitting problem:', error);
            alert('Failed to submit problem. Please try again.');
        }
    };

    return (
        <div className="problem-page">
            <div className="form-container">

                <div className="form-title">
                    Suggest a Problem
                </div>

                <form onSubmit={handleSubmit}>

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
                        <label htmlFor="statement">
                            Problem Statement
                        </label>

                        <textarea
                            id="statement"
                            name="statement"
                            value={formData.statement}
                            onChange={handleChange}
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="input_format">
                            Input Format
                        </label>

                        <textarea
                            id="input_format"
                            name="input_format"
                            value={formData.input_format}
                            onChange={handleChange}
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="output_format">
                            Output Format
                        </label>

                        <textarea
                            id="output_format"
                            name="output_format"
                            value={formData.output_format}
                            onChange={handleChange}
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="constraints">
                            Constraints
                        </label>

                        <textarea
                            id="constraints"
                            name="constraints"
                            value={formData.constraints}
                            onChange={handleChange}
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="difficulty">
                            Difficulty
                        </label>

                        <select
                            id="difficulty"
                            name="difficulty"
                            value={formData.difficulty}
                            onChange={handleChange}
                            required
                        >
                            <option value="">
                                Select difficulty...
                            </option>

                            <option value="EASY">
                                Easy
                            </option>

                            <option value="MEDIUM">
                                Medium
                            </option>

                            <option value="HARD">
                                Hard
                            </option>
                        </select>
                    </div>

                    <div className="testcases-section">

                        <h2>Test Cases</h2>

                        {testCases.map((testCase, index) => (
                            <div
                                className="testcase-card"
                                key={index}
                            >

                                <div className="form-group">
                                    <label>
                                        Input
                                    </label>

                                    <textarea
                                        name="stdin"
                                        value={testCase.stdin}
                                        onChange={(e) =>
                                            handleTestCaseChange(index, e)
                                        }
                                        required
                                    />
                                </div>

                                <div className="form-group">
                                    <label>
                                        Expected Output
                                    </label>

                                    <textarea
                                        name="expected_stdout"
                                        value={testCase.expected_stdout}
                                        onChange={(e) =>
                                            handleTestCaseChange(index, e)
                                        }
                                        required
                                    />
                                </div>

                                {testCases.length > 3 && (
                                    <button
                                        type="button"
                                        className="remove-testcase-button"
                                        onClick={() =>
                                            removeTestCase(index)
                                        }
                                    >
                                        Remove Test Case
                                    </button>
                                )}

                            </div>
                        ))}

                        <button
                            type="button"
                            className="add-testcase-button"
                            onClick={addTestCase}
                        >
                            Add Test Case
                        </button>

                    </div>

                    <button
                        type="submit"
                        className="submit-button"
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

                </form>
            </div>
        </div>
    );
}

export default Problem;