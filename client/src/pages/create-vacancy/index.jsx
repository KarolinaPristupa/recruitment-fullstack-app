import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { jwtDecode } from "jwt-decode";
import axios from "axios";
import Toast from "@components/toast";
import styles from "./styles.module.scss";

const CreateVacancy = () => {
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
                const decodedToken = jwtDecode(token);
                if (decodedToken?.sub) {
                    setEmail(decodedToken.sub);
                } else {
                    setToast({ show: true, message: "Не удалось извлечь email из токена", type: "error" });
                }
            } catch (error) {
                setToast({ show: true, message: "Ошибка при декодировании токена", type: "error" });
            }
        } else {
            setToast({ show: true, message: "Токен отсутствует", type: "error" });
        }
    }, []);

    const handleChange = (e) => {
        const { name, value } = e.target;
        if (name === "salary" && value < 0) {
            setToast({ show: true, message: "Зарплата не может быть отрицательной", type: "error" });
            return;
        }
        setFormData((prev) => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!email) {
            setToast({ show: true, message: "Email еще не загружен", type: "error" });
            return;
        }

        setLoading(true);
        const dataToSend = { ...formData, email };

        try {
            const token = sessionStorage.getItem("token");
            const result = await axios.post("http://localhost:1111/api/vacancies", dataToSend, {
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json"
                }
            });

            if (result.data.success) {
                setToast({ show: true, message: "Вакансия успешно создана", type: "success" });
                setTimeout(() => navigate("/vacancies"), 1000);
            } else {
                setToast({ show: true, message: result.data.error || "Ошибка при создании вакансии", type: "error" });
            }
        } catch (err) {
            console.error("Ошибка при создании вакансии:", err);
            setToast({ show: true, message: `Ошибка: ${err.response?.data?.error || err.message}`, type: "error" });
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className={styles.container}>
            <h1>Создание вакансии</h1>

            {!email ? (
                <p>Загрузка данных пользователя...</p>
            ) : (
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
                        required
                    />

                    <textarea
                        placeholder="Описание"
                        name="description"
                        value={formData.description}
                        onChange={handleChange}
                        required
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
                        min="0"
                        required
                    />

                    <button type="submit" disabled={loading}>
                        {loading ? "Создание..." : "Создать"}
                    </button>
                </form>
            )}

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

export default CreateVacancy;