import React, { useState } from "react";
import axios from "axios";
import styles from "@pages/account/styles.module.scss";

const RegistrationForm = ({ onSwitchToLogin }) => {
    const [firstName, setFirstName] = useState("");
    const [lastName, setLastName] = useState("");
    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [phone, setPhone] = useState("");
    const [email, setEmail] = useState("");
    const [photo, setPhoto] = useState(null);
    const [error, setError] = useState("");
    const [fileName, setFileName] = useState("Файл не выбран");

    const handleFileChange = (e) => {
        const file = e.target.files[0];
        if (file) {
            setPhoto(file);
            setFileName(file.name);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (password !== confirmPassword) {
            setError("Пароли не совпадают");
            return;
        }

        try {
            const formData = new FormData();
            const userData = {
                firstName,
                lastName,
                email,
                phone,
                password
            };

            formData.append("user", new Blob([JSON.stringify(userData)], { type: "application/json" }));
            if (photo) {
                formData.append("photo", photo);
            }

            const response = await axios.post("http://localhost:1111/api/register", formData, {
                headers: { "Content-Type": "multipart/form-data" },
            });

            console.log("Регистрация успешна:", response.data);
            onSwitchToLogin();
        } catch (error) {
            console.error("Ошибка при регистрации:", error.response?.data || error.message);
            setError(error.response?.data || "Ошибка регистрации");
        }
    };


    return (
        <div className={styles.container}>
            <div className={styles.formContainer}>
                <h2 className={styles.formTitle}>Регистрация</h2>
                <form onSubmit={handleSubmit}>
                    <input
                        type="text"
                        placeholder="Имя"
                        value={firstName}
                        onChange={(e) => setFirstName(e.target.value)}
                        className={styles.inputField}
                        required
                    />
                    <input
                        type="text"
                        placeholder="Фамилия"
                        value={lastName}
                        onChange={(e) => setLastName(e.target.value)}
                        className={styles.inputField}
                        required
                    />
                    <input
                        type="email"
                        placeholder="Email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        className={styles.inputField}
                        required
                    />
                    <input
                        type="tel"
                        placeholder="Телефон"
                        value={phone}
                        onChange={(e) => setPhone(e.target.value)}
                        className={styles.inputField}
                        required
                    />
                    <input
                        type="password"
                        placeholder="Пароль"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        className={styles.inputField}
                        required
                    />
                    <input
                        type="password"
                        placeholder="Повторите пароль"
                        value={confirmPassword}
                        onChange={(e) => setConfirmPassword(e.target.value)}
                        className={styles.inputField}
                        required
                    />
                    {error && <p className={styles.error}>{error}</p>}
                    <label className={styles.fileUploadLabel}>
                        <span>{fileName}</span>
                        <input
                            type="file"
                            onChange={handleFileChange}
                            className={styles.fileInput}
                            accept="image/*"
                        />
                    </label>
                    <button type="submit" className={styles.button}>
                        Зарегистрироваться
                    </button>
                    <p className={styles.login}>
                        Уже есть аккаунт?{" "}
                        <button type="button" onClick={onSwitchToLogin} className={styles.link}>
                            Войти
                        </button>
                    </p>
                </form>
            </div>
        </div>
    );
};

export default RegistrationForm;
