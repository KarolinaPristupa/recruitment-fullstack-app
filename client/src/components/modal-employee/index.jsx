import React, { useState, useEffect } from "react";
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
        [styles.frozen]: status === "заморожено",
    });

    const canInvite = status === "активно";
    const [toast, setToast] = useState({ show: false, message: "", type: "" });
    const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
    const [currency, setCurrency] = useState("BYN"); // BYN or USD
    const [exchangeRate, setExchangeRate] = useState(null); // { USDtoBYN, BYNtoUSD }
    const [isLoadingRate, setIsLoadingRate] = useState(false);

    // Fetch exchange rate from NBRB API
    useEffect(() => {
        const fetchExchangeRate = async () => {
            // Check cache first
            const cachedRate = sessionStorage.getItem("exchangeRate");
            if (cachedRate) {
                setExchangeRate(JSON.parse(cachedRate));
                setIsLoadingRate(false);
                return;
            }

            setIsLoadingRate(true);
            try {
                // Try dynamics endpoint with recent dates (last 7 days)
                const today = new Date();
                let rateFound = false;
                let USDtoBYN;
                for (let i = 0; i < 7; i++) {
                    const dateStr = today.toISOString().split("T")[0]; // YYYY-MM-DD
                    const response = await axios.get(
                        `https://www.nbrb.by/api/exrates/rates/dynamics/431?startdate=${dateStr}&enddate=${dateStr}`
                    );
                    if (response.data && response.data.length > 0) {
                        USDtoBYN = response.data[response.data.length - 1].Cur_OfficialRate;
                        rateFound = true;
                        break;
                    }
                    today.setDate(today.getDate() - 1); // Try previous day
                }

                // Fallback to current rate endpoint
                if (!rateFound) {
                    const response = await axios.get(
                        "https://www.nbrb.by/api/exrates/rates/431?parammode=2"
                    );
                    if (response.data && response.data.Cur_OfficialRate) {
                        USDtoBYN = response.data.Cur_OfficialRate;
                        rateFound = true;
                    }
                }

                if (!rateFound) {
                    throw new Error("No exchange rate data available from NBRB API");
                }

                const exchangeRate = {
                    USDtoBYN: USDtoBYN,
                    BYNtoUSD: 1 / USDtoBYN,
                };
                setExchangeRate(exchangeRate);
                sessionStorage.setItem("exchangeRate", JSON.stringify(exchangeRate));
                sessionStorage.setItem("exchangeRateTime", Date.now().toString());
            } catch (error) {
                console.error("Ошибка при получении курса валют:", {
                    message: error.message,
                    status: error.response?.status,
                    data: error.response?.data,
                });
                setToast({
                    show: true,
                    message: "Не удалось загрузить курс валют. Используется стандартный курс.",
                    type: "error",
                });
                // Fallback rate (based on recent NBRB data, October 2024)
                const fallbackRate = {
                    USDtoBYN: 3.25, // 1 USD = 3.25 BYN
                    BYNtoUSD: 1 / 3.25, // 1 BYN ≈ 0.3077 USD
                };
                setExchangeRate(fallbackRate);
                sessionStorage.setItem("exchangeRate", JSON.stringify(fallbackRate));
                sessionStorage.setItem("exchangeRateTime", Date.now().toString());
            } finally {
                setIsLoadingRate(false);
            }
        };

        fetchExchangeRate();
    }, []);

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
            const decodedToken = token ? JSON.parse(atob(token.split(".")[1])) : null;
            const email = decodedToken?.sub;

            if (!email) {
                setToast({ message: "Не удалось найти email в токене", type: "error", show: true });
                return;
            }

            await axios.delete("http://localhost:1111/api/vacancies/vacancies", {
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
                data: {
                    email: email,
                    id: vacancy.vacancies_id,
                },
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
                { vacancyId },
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                        "Content-Type": "application/json",
                    },
                }
            );

            setToast({ show: true, message: "Вы успешно откликнулись на вакансию!", type: "success" });
        } catch (error) {
            console.error("Ошибка при отправке отклика:", error.response?.data || error.message);
            setToast({
                show: true,
                message: `Ошибка при отклике: ${error.response?.data || error.message}`,
                type: "error",
            });
        }
    };

    // Convert salary based on currency
    const convertSalary = () => {
        if (!exchangeRate || isLoadingRate) return vacancy.salary;
        const salary = parseFloat(vacancy.salary);
        if (isNaN(salary)) return vacancy.salary; // Fallback if salary is invalid
        if (currency === "USD") {
            return (salary * exchangeRate.BYNtoUSD).toFixed(2);
        }
        return salary.toFixed(2); // BYN (original)
    };

    // Toggle currency
    const toggleCurrency = () => {
        setCurrency(currency === "BYN" ? "USD" : "BYN");
    };

    return (
        <div className={styles["modal-overlay"]}>
            <div className={styles["modal-content"]}>
                <button className={styles["close-btn"]} onClick={onClose}>×</button>

                <div className={styles["status-top-right"]}>
                    <span className={clsx(styles.status, statusClass)}>{vacancy.status}</span>
                </div>

                <h2 className={styles["vacancy-title"]}>{vacancy.position}</h2>

                <div className={styles["info-block"]}>
                    <p>
                        <strong>Отдел:</strong> {vacancy.department}
                    </p>
                    <p>
                        <strong>Требования:</strong> {vacancy.requirements}
                    </p>
                    <p>
                        <strong>Описание:</strong> {vacancy.description}
                    </p>
                    <div className={styles["salary-block"]}>
                        <strong>Зарплата:</strong>{" "}
                        {isLoadingRate ? "Загрузка..." : `${convertSalary()} ${currency}`}
                        <button
                            className={styles["currency-toggle"]}
                            onClick={toggleCurrency}
                            disabled={isLoadingRate}
                            title={`Переключить на ${currency === "BYN" ? "USD" : "BYN"}`}
                        >
                            {currency === "BYN" ? "USD" : "BYN"}
                        </button>
                    </div>
                </div>

                {vacancy.employee?.user && (
                    <div className={styles["creator-block"]}>
                        <h3>Создатель вакансии</h3>
                        <p>
                            <strong>Фамилия:</strong> {vacancy.employee.user.lastName || "Не указано"}
                        </p>
                        <p>
                            <strong>Имя:</strong> {vacancy.employee.user.firstName || "Не указано"}
                        </p>
                        <p>
                            <strong>Email:</strong> {vacancy.employee.user.email || "Не указано"}
                        </p>
                        <p>
                            <strong>Телефон:</strong> {vacancy.employee.user.phone || "Не уkaзано"}
                        </p>
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
                            <div
                                className={styles["icon-wrapper"]}
                                title="Редактировать"
                                onClick={handleEdit}
                            >
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

                {toast.show && (
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