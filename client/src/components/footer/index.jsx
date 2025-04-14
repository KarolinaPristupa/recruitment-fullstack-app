import React from "react";
import styles from "./styles.module.scss";

const Footer = () => {
    return (
        <footer className={styles.footer}>
            <div className={styles.container}>
                <div className={styles.about}>
                    <h3>О нас</h3>
                    <p>
                        Наша платформа предоставляет программную поддержку подбора персонала
                        с возможностью автоматизированной оценки уровня компетенций кандидатов.
                    </p>
                </div>
                <div className={styles.contact}>
                    <h3>Контакты</h3>
                    <p>Email: support@recruitment.com</p>
                    <p>Телефон: +7 (123) 456-78-90</p>
                </div>
                <div className={styles.social}>
                    <h3>Мы в соцсетях</h3>
                    <ul>
                        <li><a href="https://facebook.com" target="_blank" rel="noopener noreferrer">Facebook</a></li>
                        <li><a href="https://twitter.com" target="_blank" rel="noopener noreferrer">Twitter</a></li>
                        <li><a href="https://linkedin.com" target="_blank" rel="noopener noreferrer">LinkedIn</a></li>
                    </ul>
                </div>
            </div>
            <div className={styles.copyright}>
                <p>&copy; 2025 Recruitment Platform. Все права защищены.</p>
            </div>
        </footer>
    );
};

export default Footer;