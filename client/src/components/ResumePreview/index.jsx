import React from "react";
import styles from "@pages/candidate/styles.module.scss";
import { FaPhone, FaEnvelope } from "react-icons/fa";

const ResumePreview = ({ candidate }) => {
    if (!candidate) return <div>Загрузка...</div>;

    const { user, resume } = candidate;

    return (
        <div>
            <div className={styles.contentContainer}>
                <div className={styles.previewContent}>
                    <div>
                        <img
                            src={user?.photo ? `${user.photo}?t=${Date.now()}` : "/default-avatar.png"}
                            alt="Фото профиля"
                            className={styles.photo}
                        />
                        <div>
                            <p className={styles.contactInfo}><FaPhone className={styles.icon}/> {user?.phone || "Не указан"}</p>
                            <br />
                            <p className={styles.contactInfo}><FaEnvelope className={styles.icon}/> {user?.email || "Не указан"}</p>
                        </div>
                    </div>

                    <div>
                        <h4 className={styles.sectionTitle}>Навыки:</h4>
                        <ul className={styles.skillsList}>
                            {resume?.skills
                                ? resume.skills.split(",").map((skill, index) => (
                                    <li key={index}>{skill.trim()}</li>
                                ))
                                : <li>Навыки не указаны</li>}
                        </ul>

                        <h4 className={styles.sectionTitle}>Образование:</h4>
                        <p className={styles.underline}>{resume?.education || "Образование не указано"}</p>

                        <h4 className={styles.sectionTitle}>Компании:</h4>
                        <ul className={styles.skillsList}>
                            {resume?.campaigns
                                ? resume.campaigns.split(",").map((company, index) => (
                                    <li key={index}>{company.trim()}</li>
                                ))
                                : <li>Кампании не указаны</li>}
                        </ul>

                        <h4 className={styles.sectionTitle}>Сертификаты:</h4>
                        <ul className={styles.skillsList}>
                            {resume?.certifications
                                ? resume.certifications.split(",").map((cert, index) => (
                                    <li key={index}>{cert.trim()}</li>
                                ))
                                : <li>Сертификаты не указаны</li>}
                        </ul>
                    </div>
                </div>

                <div className={styles.rightColumn}>
                    <div className={styles.resumeHeader}>
                        <h2 className={styles.lastName}>{user?.lastName || "Фамилия не указана"}</h2>
                        <h3 className={styles.firstName}>{user?.firstName || "Имя не указано"}</h3>
                    </div>

                    <div>
                        <h4 className={styles.sectionTitle}>Проекты:</h4>
                        <ul className={styles.skillsList}>
                            {resume?.projects
                                ? resume.projects.split(",").map((project, index) => {
                                    const formattedProject = project.trim();
                                    return (
                                        <li key={index}>
                                            {formattedProject.charAt(0).toUpperCase() + formattedProject.slice(1)}
                                        </li>
                                    );
                                })
                                : <li>Проекты не указаны</li>}
                        </ul>
                    </div>

                    <div>
                        <h4 className={styles.sectionTitle}>Обязанности на предыдущем месте работы:</h4>
                        <ul className={styles.skillsList}>
                            {resume?.responsibilities
                                ? resume.responsibilities.split(",").map((task, index) => {
                                    const formattedTask = task.trim();
                                    return (
                                        <li key={index}>
                                            {formattedTask.charAt(0).toUpperCase() + formattedTask.slice(1)}
                                        </li>
                                    );
                                })
                                : <li>Обязанности не указаны</li>}
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ResumePreview;
