import React, { useState } from "react";
import styles from "./styles.module.scss";
import clsx from "clsx";
import { Pencil, Trash2, Mail } from "lucide-react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import Toast from "@components/toast";

const Modal_employee = ({ vacancy, currentUser, onClose, onDelete }) => {
    if (!vacancy || !currentUser) return null;

    const isEmployee = currentUser.role === "HR";
    const isOwner = vacancy.employee?.user?.email === currentUser.email;
    const status = vacancy.status?.toLowerCase();
    const navigate = useNavigate();

    const statusClass = clsx({
        [styles.active]: status === "активно",
        [styles.inactive]: status === "неактивно",
        [styles.frozen]: status === "заморожено"
    });

    const canInvite = status === "активно";
    const [toast, setToast] = useState({ show: false, message: "", type: "" });
    const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);

    const handleInvite = () => {
        sessionStorage.setItem("vacancyToInvite", JSON.stringify(vacancy));
        onClose();
        navigate("/candidates/invite");
    };

    const handleEdit = () => {
        sessionStorage.setItem("vacancyToEdit", JSON.stringify(vacancy));
        onClose();
        navigate("/vacancies/edit");
    };

    const handleDelete = async () => {
        try {
            const token = sessionStorage.getItem("token");
            const decodedToken = token ? JSON.parse(atob(token.split('.')[1])) : null;
            const email = decodedToken?.sub;

            if (!email) {
                setToast({ message: "Не удалось найти email в токене", type: "error", show: true });
                return;
            }

            await axios.delete("http://localhost:1111/api/vacancies/vacancies", {
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json"
                },
                data: {
                    email: email,
                    id: vacancy.vacancies_id
                }
            });

            setToast({ message: "Вакансия успешно удалена", type: "success", show: true });

            setTimeout(() => {
                onDelete(vacancy.vacancies_id);
                onClose();
            }, 1000);
        } catch (err) {
            console.error("Ошибка удаления вакансии:", err);
            setToast({
                message: "Не удалось удалить вакансию",
                type: "error",
                show: true,
            });
        }
    };

    const handleRespond = async () => {
        try {
            const token = sessionStorage.getItem("token");
            if (!token) {
                setToast({ show: true, message: "Токен не найден", type: "error" });
                return;
            }

            const vacancyId = vacancy.vacancies_id;
            if (!vacancyId || typeof vacancyId !== "number") {
                setToast({ show: true, message: "ID вакансии недействителен", type: "error" });
                return;
            }

            console.log("Отправка отклика с vacancyId =", vacancyId);

            await axios.post(
                "http://localhost:1111/api/responses",
                { vacancyId }, // ключ должен быть точно таким, как на сервере
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                        "Content-Type": "application/json"
                    }
                }
            );

            setToast({ show: true, message: "Вы успешно откликнулись на вакансию!", type: "success" });
        } catch (error) {
            console.error("Ошибка при отправке отклика:", error.response?.data || error.message);
            setToast({
                show: true,
                message: `Ошибка при отклике: ${error.response?.data || error.message}`,
                type: "error"
            });
        }
    };


    return (
        <div className={styles["modal-overlay"]}>
            <div className={styles["modal-content"]}>
                <button className={styles["close-btn"]} onClick={onClose}>×</button>

                <div className={styles["status-top-right"]}>
                    <span className={clsx(styles.status, statusClass)}>{vacancy.status}</span>
                </div>

                <h2 className={styles["vacancy-title"]}>{vacancy.position_title}</h2>

                <div className={styles["info-block"]}>
                    <p><strong>Отдел:</strong> {vacancy.department}</p>
                    <p><strong>Требования:</strong> {vacancy.requirements}</p>
                    <p><strong>Описание:</strong> {vacancy.description}</p>
                    <div className={styles["salary-block"]}>
                        <strong>Зарплата:</strong> {vacancy.salary} USD
                    </div>
                </div>

                {vacancy.employee?.user && (
                    <div className={styles["creator-block"]}>
                        <h3>Создатель вакансии</h3>
                        <p><strong>Фамилия:</strong> {vacancy.employee.user.lastName || "Не указано"}</p>
                        <p><strong>Имя:</strong> {vacancy.employee.user.firstName || "Не указано"}</p>
                        <p><strong>Email:</strong> {vacancy.employee.user.email || "Не указано"}</p>
                        <p><strong>Телефон:</strong> {vacancy.employee.user.phone || "Не указано"}</p>
                    </div>
                )}

                <div className={styles["bottom-actions-block"]}>
                    {currentUser.role === "Кандидат" && status === "активно" && (
                        <div className={styles["icon-wrapper"]} title="Откликнуться">
                            <Mail className={styles["icon-btn"]} onClick={handleRespond} />
                        </div>
                    )}

                    {isEmployee && isOwner && (
                        <div className={styles["icon-actions"]}>
                            {canInvite && (
                                <div className={styles["icon-wrapper"]} title="Пригласить">
                                    <Mail className={styles["icon-btn"]} onClick={handleInvite} />
                                </div>
                            )}
                            <div className={styles["icon-wrapper"]} title="Редактировать" onClick={handleEdit}>
                                <Pencil className={styles["icon-btn"]} />
                            </div>
                            <div
                                className={styles["icon-wrapper"]}
                                title="Удалить"
                                onClick={() => setShowDeleteConfirm(true)}
                            >
                                <Trash2 className={styles["icon-btn"]} />
                            </div>
                        </div>
                    )}
                </div>

                {toast && toast.show && (
                    <Toast
                        message={toast.message}
                        type={toast.type}
                        onClose={() => setToast({ show: false, message: "", type: "" })}
                    />
                )}

                {showDeleteConfirm && (
                    <div className={styles["delete-confirm-overlay"]}>
                        <div className={styles["delete-confirm-content"]}>
                            <p>Вы действительно хотите удалить эту вакансию?</p>
                            <div className={styles["delete-confirm-actions"]}>
                                <button
                                    className={clsx(styles["action-btn"], styles["confirm-btn"])}
                                    onClick={handleDelete}
                                >
                                    Да
                                </button>
                                <button
                                    className={clsx(styles["action-btn"], styles["cancel-btn"])}
                                    onClick={() => setShowDeleteConfirm(false)}
                                >
                                    Нет
                                </button>
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default Modal_employee;