import React, { useState, useEffect } from "react";
import LoginForm from "@components/login-form";
import RegistrationForm from "@components/registration-form";
import ForgotPasswordForm from "@components/forgot-password-form";
import { useNavigate } from "react-router-dom";

const Account = () => {
    const [isLoginForm, setIsLoginForm] = useState(true);
    const [isForgotPasswordForm, setIsForgotPasswordForm] = useState(false);
    const navigate = useNavigate();
    const handleLogin = (token, role) => {
        console.log("Успешный вход! Токен:", token, "Роль:", role);

        sessionStorage.setItem("token", token);
        sessionStorage.setItem("role", role);

        // Принудительное обновление `Header`
        window.dispatchEvent(new Event("storage"));

        if (role === "Админ") {
            navigate("/admin");
        } else if (role === "Кандидат") {
            navigate("/candidate");
        } else if (role === "HR") {
            navigate("/employee");
        } else {
            navigate("/");
        }
    };

    const handleRegister = (formData) => {
        console.log("Register:", formData);
    };

    const handleResetPassword = async (email, newPassword) => {
        try {
            const response = await fetch("http://localhost:1111/login/reset-password", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email, newPassword }),
            });
            return response;
        } catch (error) {
            console.error("Ошибка при сбросе пароля:", error);
            return { ok: false };
        }
    };

    useEffect(() => {
        const handleUnload = () => {
            sessionStorage.removeItem("token");
            sessionStorage.removeItem("role");
        };

        window.addEventListener("beforeunload", handleUnload);
        return () => window.removeEventListener("beforeunload", handleUnload);
    }, []);


    return (
        <>
            {isForgotPasswordForm ? (
                <ForgotPasswordForm
                    onSwitchToLogin={() => {
                        setIsForgotPasswordForm(false);
                        setIsLoginForm(true);
                    }}
                    onResetPassword={handleResetPassword}
                />
            ) : isLoginForm ? (
                <LoginForm
                    onSwitchToRegister={() => {
                        setIsLoginForm(false);
                        setIsForgotPasswordForm(false);
                    }}
                    onForgotPassword={() => {
                        setIsLoginForm(false);
                        setIsForgotPasswordForm(true);
                    }}
                    onLogin={handleLogin}
                />
            ) : (
                <RegistrationForm
                    onSwitchToLogin={() => {
                        setIsLoginForm(true);
                        setIsForgotPasswordForm(false);
                    }}
                    onRegister={handleRegister}
                />
            )}
        </>
    );
};

export default Account;