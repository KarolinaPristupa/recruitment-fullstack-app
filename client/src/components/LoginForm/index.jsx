import React, { useState } from "react";
import axios from "axios";
import { jwtDecode } from "jwt-decode"
import styles from "@pages/account/styles.module.scss";

const LoginForm = ({ onSwitchToRegister, onLogin, onForgotPassword }) => {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const response = await axios.post("http://localhost:1111/login", {
                username: email,
                password: password,
            });

            const token = response.data.jwt;
            sessionStorage.setItem("token", token);

            const decoded = jwtDecode(token);
            sessionStorage.setItem("role", decoded.role);

            onLogin(token, decoded.role);
        } catch (error) {
            console.error("Ошибка при входе:", error);
        }
    };


    return (
        <div className={styles.container}>
            <div className={styles.formContainer}>
                <h2 className={styles.formTitle}>Login</h2>
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
                        placeholder="Пароль"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        className={styles.inputField}
                        required
                    />
                    <div className={styles.forgotPasswordLink}>
                        <button
                            type="button"
                            onClick={onForgotPassword}
                            className={styles.link}
                        >
                            Забыли пароль?
                        </button>
                    </div>
                    <button type="submit" className={styles.button}>
                        Login
                    </button>
                    <p className={styles.registration}>
                        Еще нет аккаунта?{" "}
                        <button
                            type="button"
                            onClick={onSwitchToRegister}
                            className={styles.link}
                        >
                            Зарегистрироваться
                        </button>
                    </p>
                </form>
            </div>
        </div>
    );
};

export default LoginForm;