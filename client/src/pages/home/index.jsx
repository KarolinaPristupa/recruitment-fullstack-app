import React, { useEffect, useState } from "react";
import axios from "axios";
import styles from "./styles.module.scss";

const Home = () => {
    const [topEmployee, setTopEmployee] = useState(null);

    useEffect(() => {
        const fetchTopEmployee = async () => {
            try {
                const response = await axios.get("http://localhost:1111/api/statistics");
                setTopEmployee(response.data);
            } catch (error) {
                console.error("Ошибка при загрузке лучшего сотрудника", error);
            }
        };

        fetchTopEmployee();
    }, []);

    return (
        <div className={styles.container}>
            <h1 className={styles.title}>Добро пожаловать в систему подбора и найма сотрудников!</h1>

            {topEmployee && (
                <div className={styles.topEmployee}>
                    <h2 className={styles.subtitle}>Лучший сотрудник месяца</h2>
                    <div className={styles.employeeCard}>
                        <img
                            src={topEmployee.photoUrl}
                            alt={`${topEmployee.lastName} ${topEmployee.firstName}`}
                            className={styles.employeePhoto}
                        />
                        <div className={styles.employeeName}>
                            {topEmployee.lastName} {topEmployee.firstName}
                        </div>
                        <div className={styles.employeeInfo}>
                            <p><strong>Должность:</strong> {topEmployee.position}</p>
                            <p><strong>Успешность собеседований:</strong> {Math.round(topEmployee.successRate)}%</p>
                        </div>
                    </div>
                </div>
            )}

            <div className={styles.infoSection}>
                <h3 className={styles.subtitle}>Как рассчитывается успешность собеседований?</h3>
                <div className={styles.formulaContainer}>
                    <div className={styles.formula}>
                        <span className={styles.formulaText}>Успешность = </span>
                        <div className={styles.fraction}>
                            <span className={styles.numerator}>Количество успешных собеседований</span>
                            <span className={styles.denominator}>Общее количество собеседований</span>
                        </div>
                        <span className={styles.formulaText}> × 100%</span>
                    </div>
                </div>

                <h3 className={styles.subtitle}>Зачем это важно?</h3>
                <p className={styles.text}>
                    Эффективные собеседования — ключ к качественному найму. Мы анализируем результаты, чтобы поощрить лучших сотрудников и
                    постоянно улучшать процессы подбора. Следите за своими показателями, улучшайте навыки общения и помогайте компании расти!
                </p>
            </div>
        </div>
    );
};

export default Home;