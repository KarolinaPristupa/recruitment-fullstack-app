import React from "react";
import HrPagination from "@components/HrPagination";
import useFetchEmployees from "@hooks/useFetchEmployees";
import styles from "./styles.module.scss";

const About = () => {
    const { employees, loading, error } = useFetchEmployees();

    if (loading) {
        return <div className={styles.container}>Загрузка...</div>;
    }

    if (error) {
        return <div className={styles.container}>Ошибка: {error}</div>;
    }

    return (
        <div>
            <h1 className={styles.title}>О сайте</h1>
            <p className={styles.text}>
                Наш сайт предназначен для упрощения процесса найма сотрудников. Мы помогаем соискателям находить подходящие вакансии, а работодателям — находить квалифицированных специалистов.
            </p>
            <h2 className={styles.subtitle}>Этапы найма</h2>
            <ol className={styles.list}>
                <li>
                    <strong>Регистрация:</strong> Для начала работы с сайтом необходимо зарегистрироваться. Вы можете создать аккаунт на странице <a href="/account" className={styles.link}>Аккаунт</a>.
                </li>
                <li>
                    <strong>Поиск вакансий:</strong> После регистрации откройте список доступных вакансий.
                </li>
                <li>
                    <strong>Выбор вакансии:</strong> Выберите понравившуюся вакансию и ознакомьтесь с её описанием.
                </li>
                <li>
                    <strong>Отклик:</strong> Нажмите кнопку "Откликнуться", чтобы отправить своё резюме.
                </li>
                <li>
                    <strong>Ожидание ответа:</strong> После отклика ожидайте ответа от HR-специалиста.
                </li>
            </ol>
            <h2 className={styles.subtitle}>Наши HR-специалисты</h2>
            <p className={styles.text}>
                Наши HR-специалисты всегда готовы помочь вам с выбором вакансии и ответить на все ваши вопросы.
            </p>
            <HrPagination employees={employees} />
        </div>
    );
};

export default About;