import React, { useState } from "react";
import ReactDOM from "react-dom";
import Calendar from "react-calendar";
import { FaTimes } from "react-icons/fa";
import Toast from "@components/Toast";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import styles from "./styles.module.scss";
import "react-calendar/dist/Calendar.css";

const Modal_invite = ({ onClose, candidate, vacancy }) => {
    const [selectedDate, setSelectedDate] = useState(null);
    const [selectedTime, setSelectedTime] = useState("12:00");
    const [toast, setToast] = useState({ show: false, message: "", type: "" });
    const navigate = useNavigate();

    console.log("Рендер модального окна с приглашением");

    const handleDateSelect = (date) => setSelectedDate(date);

    const sendInvitation = async () => {
        if (!selectedDate || !selectedTime) {
            setToast({ show: true, message: "Выберите дату и время", type: "error" });
            return;
        }

        try {
            const token = sessionStorage.getItem("token");
            console.log("Токен для отправки:", token);

            if (!token) {
                setToast({ show: true, message: "Токен не найден", type: "error" });
                return;
            }

            // Проверка candidate и vacancy
            if (!candidate || !candidate.candidateId) {
                console.error("Кандидат не определен или отсутствует id:", candidate);
                setToast({ show: true, message: "Кандидат не выбран", type: "error" });
                return;
            }

            if (!vacancy || !vacancy.vacancies_id) {
                console.error("Вакансия не определена или отсутствует vacancies_id:", vacancy);
                setToast({ show: true, message: "Вакансия не выбрана", type: "error" });
                return;
            }

            const [hours, minutes] = selectedTime.split(":");
            const interviewDate = new Date(selectedDate);
            interviewDate.setHours(parseInt(hours));
            interviewDate.setMinutes(parseInt(minutes));

            const formData = new URLSearchParams();
            formData.append("candidateId", candidate.candidateId.toString());
            formData.append("vacancyId", vacancy.vacancies_id.toString());
            formData.append("interviewDate", interviewDate.toISOString());

            await axios.post("http://localhost:1111/api/invites", formData, {
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/x-www-form-urlencoded"
                }
            });

            setToast({ show: true, message: "Приглашение успешно отправлено!", type: "success" });

            setTimeout(() => {
                onClose();
                navigate("/vacancies");
            }, 2000);
        } catch (error) {
            console.error("Ошибка при отправке приглашения:", error);
            setToast({ show: true, message: "Ошибка при отправке: " + error.message, type: "error" });
        }
    };


    return ReactDOM.createPortal(
        <div className={styles.overlay}>
            <div className={styles.modal}>
                <button className={styles.close} onClick={onClose}>
                    <FaTimes />
                </button>

                <h2>Выберите дату и время собеседования</h2>
                <Calendar
                    onChange={handleDateSelect}
                    value={selectedDate}
                    className={styles.calendar}
                />
                <div className={styles.timeAndButton}>
                    <input
                        type="time"
                        value={selectedTime}
                        onChange={(e) => setSelectedTime(e.target.value)}
                        className={styles.timeInput}
                    />
                    <button className={styles.confirmButton} onClick={sendInvitation}>
                        Отправить приглашение
                    </button>
                </div>

                {toast.show && (
                    <Toast
                        message={toast.message}
                        type={toast.type}
                        onClose={() => setToast({ show: false, message: "", type: "" })}
                    />
                )}
            </div>
        </div>,
        document.body
    );
};

export default Modal_invite;
