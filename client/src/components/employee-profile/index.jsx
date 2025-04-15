import React from "react";
import styles from "@pages/candidate/styles.module.scss";
import { FaPhone, FaEnvelope, FaBuilding, FaUserTie } from "react-icons/fa";

const EmployeeProfile = ({ employee }) => {
    if (!employee) return <div>Загрузка...</div>;

    const { user, position, department } = employee;

    return (
        <div>
            <div>
                <img
                    src={user?.photo ? `${user.photo}?t=${Date.now()}` : "/default-avatar.png"}
                    alt="Фото профиля"
                    className={styles.photo}
                />
                <div>
                    <h2>{user?.firstName} {user?.lastName}</h2>
                    <p><FaPhone className={styles.icon} /> {user?.phone || "Не указан"}</p>
                    <p><FaEnvelope className={styles.icon} /> {user?.email || "Не указан"}</p>
                </div>
            </div>

            <div>
                <p><FaBuilding className={styles.icon} /> <strong>Отдел:</strong> {department || "Не указан"}</p>
                <p><FaUserTie className={styles.icon} /> <strong>Должность:</strong> {position || "Не указана"}</p>
            </div>
        </div>
    );
};

export default EmployeeProfile;
