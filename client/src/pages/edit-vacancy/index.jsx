import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import styles from "@pages/create-vacancy/styles.module.scss";
import Toast from "@components/toast";
import axios from "axios";

const VacancyEdit = () => {
    const navigate = useNavigate();
    const [email, setEmail] = useState(null);
    const [formData, setFormData] = useState({
        position_title: "",
        department: "",
        requirements: "",
        description: "",
        status: "Активно",
        salary: ""
    });
    const [toast, setToast] = useState({ show: false, message: "", type: "" });
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        const token = sessionStorage.getItem("token");

        if (token) {
            try {
                const decoded = JSON.parse(atob(token.split(".")[1]));
                setEmail(decoded.sub);
            } catch (e) {
                setToast({ show: true, message: "Ошибка при декодировании токена", type: "error" });
            }
        }

        const savedVacancy = sessionStorage.getItem("vacancyToEdit");
        if (savedVacancy) {
            const parsed = JSON.parse(savedVacancy);
            setFormData({
                position_title: parsed.position_title,
                department: parsed.department,
                requirements: parsed.requirements,
                description: parsed.description,
                status: parsed.status,
                salary: parsed.salary,
                id: parsed.vacancies_id
            });
        }
    }, []);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData((prev) => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!email) {
            setToast({ show: true, message: "Email не найден", type: "error" });
            return;
        }

        setLoading(true);
        try {
            const response = await axios.put("http://localhost:1111/api/vacancies/vacancies", {
                ...formData,
                email
            }, {
                headers: {
                    Authorization: `Bearer ${sessionStorage.getItem("token")}`,
                    "Content-Type": "application/json"
                }
            });

            if (response.data.success) {
                setToast({ show: true, message: "Вакансия обновлена", type: "success" });
                setTimeout(() => navigate("/vacancies"), 1000);
            } else {
                setToast({ show: true, message: "Ошибка при обновлении", type: "error" });
            }
        } catch (err) {
            console.error(err);
            setToast({ show: true, message: "Ошибка сервера", type: "error" });
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className={styles.container}>
            <h1>Редактировать вакансию</h1>

            <form onSubmit={handleSubmit} className={styles.form}>
                <div className={styles.row}>
                    <input
                        type="text"
                        placeholder="Должность"
                        name="position_title"
                        value={formData.position_title}
                        onChange={handleChange}
                        required
                    />
                    <input
                        type="text"
                        placeholder="Отдел"
                        name="department"
                        value={formData.department}
                        onChange={handleChange}
                        required
                    />
                </div>

                <input
                    type="text"
                    placeholder="Требования"
                    name="requirements"
                    value={formData.requirements}
                    onChange={handleChange}
                />

                <textarea
                    placeholder="Описание"
                    name="description"
                    value={formData.description}
                    onChange={handleChange}
                />

                <select name="status" value={formData.status} onChange={handleChange}>
                    <option value="Активно">Активно</option>
                    <option value="Неактивно">Неактивно</option>
                    <option value="Заморожено">Заморожено</option>
                </select>

                <input
                    type="number"
                    name="salary"
                    placeholder="Зарплата (USD)"
                    value={formData.salary}
                    onChange={handleChange}
                    required
                />

                <button type="submit" disabled={loading}>
                    {loading ? "Обновление..." : "Сохранить изменения"}
                </button>
            </form>

            {toast.show && (
                <Toast
                    message={toast.message}
                    type={toast.type}
                    onClose={() => setToast({ show: false, message: "", type: "" })}
                />
            )}
        </div>
    );
};

export default VacancyEdit;
