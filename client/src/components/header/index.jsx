import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import logo from "@assets/logo.png";
import { jwtDecode } from "jwt-decode";

import styles from "./styles.module.scss";

const Header = () => {
    const [role, setRole] = useState(() => {
        const token = sessionStorage.getItem("token");
        return token ? jwtDecode(token).role : null;
    });

    useEffect(() => {
        const updateRole = () => {
            const token = sessionStorage.getItem("token");
            if (token) {
                try {
                    const decoded = jwtDecode(token);
                    setRole(decoded.role);
                } catch (error) {
                    console.error("Ошибка декодирования JWT", error);
                    sessionStorage.removeItem("token");
                    setRole(null);
                }
            } else {
                setRole(null);
            }
        };

        window.addEventListener("storage", updateRole);
        return () => {
            window.removeEventListener("storage", updateRole);
        };
    }, []);

    const getRoleLink = () => {
        switch (role) {
            case "Админ":
                return { path: "/admin" };
            case "Кандидат":
                return { path: "/candidate" };
            case "HR":
                return { path: "/employee" };
            default:
                return { path: "/account" };
        }
    };

    const getAdditionalLinks = () => {
        switch (role) {
            case "Админ":
                return (
                    <>
                        <Link to="/vacancies" className={styles["nav-link"]}>vacancies</Link>
                        <Link to="/users" className={styles["nav-link"]}>users</Link>
                        <Link to="/notifications" className={styles["nav-link"]}>notifications</Link>
                    </>
                );
            case "Кандидат":
                return (
                    <>
                        <Link to="/vacancies" className={styles["nav-link"]}>vacancies</Link>
                        <Link to="/favorites" className={styles["nav-link"]}>favorites</Link>
                        <Link to="/notifications" className={styles["nav-link"]}>notifications</Link>
                    </>
                );
            case "HR":
                return (
                    <>
                        <Link to="/vacancies" className={styles["nav-link"]}>vacancies</Link>
                        <Link to="/candidates" className={styles["nav-link"]}>candidates</Link>
                        <Link to="/notifications" className={styles["nav-link"]}>notifications</Link>
                    </>
                );
            default:
                return null;
        }
    };

    const roleLink = getRoleLink();

    return (
        <header className={styles.header}>
            <nav className={styles.nav}>{getAdditionalLinks()}</nav>

            <div className={styles.logoContainer}>
                <img
                    src={logo}
                    alt="Logo"
                    className={`${styles.logo} ${["Кандидат", "HR", "Админ"].includes(role) ? styles.castomLogo : ""}`}
                />
            </div>

            <nav className={styles.nav}>
                <Link to="/" className={styles["nav-link"]}>home</Link>
                <Link to="/about" className={styles["nav-link"]}>about</Link>
                <Link to={roleLink.path} className={styles["nav-link"]}>account</Link>
            </nav>
        </header>
    );
};

export default Header;
