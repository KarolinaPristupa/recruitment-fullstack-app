import React, { useState, useEffect } from "react";
import axios from "axios";
import styles from "@pages/candidate/styles.module.scss";
import Toast from "@components/Toast";

const ResumeForm = ({ resume, onUpdate }) => {
    const [formData, setFormData] = useState({
        skills: "",
        education: "",
        languages: "",
        certifications: "",
        projects: "",
        responsibilities: "",
        campaigns: "",
    });

    const [toast, setToast] = useState(null);

    useEffect(() => {
        if (resume) {
            const { resumeId, ...filteredResume } = resume;
            setFormData(filteredResume);
        } else {
            setFormData({
                skills: "",
                education: "",
                languages: "",
                certifications: "",
                projects: "",
                responsibilities: "",
                campaigns: "",
            });
        }
    }, [resume]);


    const handleChange = (key) => (e) => {
        setFormData({ ...formData, [key]: e.target.value });
    };

    const showToast = (message, type) => {
        setToast({ message, type });
        setTimeout(() => setToast(null), 3500);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const token = sessionStorage.getItem("token");
            if (!token) {
                showToast("Ошибка аутентификации. Перезайдите в аккаунт.", "error");
                return;
            }

            const method = resume && resume.resumeId ? "put" : "post";
            console.log("Выбранный метод:", method); // Для отладки
            console.log("Текущее resume:", resume);  // Для отладки

            const url = "http://localhost:1111/api/candidate/resume";

            const response = await axios({
                method,
                url,
                data: formData,
                headers: { Authorization: `Bearer ${token}` },
            });

            const updatedResume = response.data.resume || response.data;

            onUpdate(updatedResume);

            showToast(`Резюме успешно ${method === "put" ? "обновлено" : "создано"}!`, "success");
        } catch (error) {
            console.error("Ошибка при сохранении резюме:", error);
            showToast("Ошибка при сохранении резюме!", "error");
        }
    };

    const handleDelete = async () => {
        const token = sessionStorage.getItem("token");
        if (!token) {
            showToast("Ошибка аутентификации. Перезайдите в аккаунт.", "error");
            return;
        }

        try {
            await axios.delete("http://localhost:1111/api/candidate/resume", {
                headers: { Authorization: `Bearer ${token}` },
            });

            showToast("Резюме успешно удалено!", "success");

            onUpdate(null);
            setTimeout(() => {
                window.location.reload();
            }, 500);

        } catch (error) {
            console.error("Ошибка при удалении резюме:", error);
            showToast("Ошибка при удалении резюме!", "error");
        }
    };

    return (
        <div className={styles.leftPanel}>
            <h2>Резюме</h2>
            <form onSubmit={handleSubmit} className={styles.form}>
                {Object.keys(formData).map((key) => (
                    <div key={key} className={styles.formGroup}>
                        <textarea
                            value={formData[key]}
                            onChange={handleChange(key)}
                            placeholder={
                                key === "skills" ? "Навыки" :
                                    key === "education" ? "Образование" :
                                        key === "languages" ? "Иностранные языки" :
                                            key === "certifications" ? "Сертификаты" :
                                                key === "projects" ? "Проекты" :
                                                    key === "responsibilities" ? "Обязанности на предыдущем месте работы" :
                                                        key === "campaigns" ? "Компании" : ""
                            }
                        />
                    </div>
                ))}
                <div className={styles.buttons}>
                    <button type="submit" className={styles.button}>
                        Сохранить резюме
                    </button>
                    {resume && resume.resumeId && (
                        <button onClick={handleDelete} className={styles.button}>
                            Удалить резюме
                        </button>
                    )}
                </div>
            </form>



            {toast && <Toast message={toast.message} type={toast.type} onClose={() => setToast(null)} />}
        </div>
    );
};

export default ResumeForm;
