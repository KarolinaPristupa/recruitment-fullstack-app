import React, { useState } from "react";
import styles from "@pages/account/styles.module.scss";

const ForgotPasswordForm = ({ onSwitchToLogin, onResetPassword }) => {
    const [email, setEmail] = useState("");
    const [newPassword, setNewPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [error, setError] = useState("");
    const [successMessage, setSuccessMessage] = useState("");

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError("");
        setSuccessMessage("");

        if (newPassword !== confirmPassword) {
            setError("Пароли не совпадают");
            return;
        }

        try {
            const response = await onResetPassword(email, newPassword);
            if (response.ok) {
                setSuccessMessage("Пароль успешно изменен! Перенаправление...");
                setTimeout(() => {
                    onSwitchToLogin();
                }, 1000);
            } else {
                setError("Ошибка при смене пароля. Проверьте email.");
            }
        } catch (err) {
            setError("Ошибка соединения с сервером");
        }
    };

    return (
        <div className={styles.container}>
            <div className={styles.formContainer}>
                <h2 className={styles.formTitle}>Сброс пароля</h2>
                <form onSubmit={handleSubmit}>
                    <input
                        type="email"
                        placeholder="Email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        className={styles.inputField}
                        required
                    />
                    <input
                        type="password"
                        placeholder="Новый пароль"
                        value={newPassword}
                        onChange={(e) => setNewPassword(e.target.value)}
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
                    {successMessage && <p className={styles.success}>{successMessage}</p>}
                    <button type="submit" className={styles.button}>
                        Изменить
                    </button>
                    <p className={styles.login}>
                        <button
                            type="button"
                            onClick={onSwitchToLogin}
                            className={styles.link}
                        >
                            Вернуться ко входу
                        </button>
                    </p>
                </form>
            </div>
        </div>
    );
};

export default ForgotPasswordForm;
