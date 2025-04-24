import React, { useEffect, useState } from "react";
import axios from "axios";
import { FaGraduationCap, FaTools, FaEnvelope, FaPhone } from "react-icons/fa"; // Иконки
import { jwtDecode } from "jwt-decode"; // Для декодирования JWT
import * as XLSX from "xlsx"; // Для экспорта в Excel
import styles from "./styles.module.scss";
import Modal_candidate from "@components/modal-candidate"; // Импортируем модальное окно

const Candidates = () => {
    const [candidates, setCandidates] = useState([]);
    const [loading, setLoading] = useState(true);
    const [selectedCandidate, setSelectedCandidate] = useState(null);
    const [error, setError] = useState(null); // Для хранения ошибки
    const [sortOrder, setSortOrder] = useState("asc"); // Порядок сортировки: asc (A-Z) или desc (Z-A)
    const [userRole, setUserRole] = useState(null); // Роль пользователя

    useEffect(() => {
        const fetchData = async () => {
            try {
                const token = sessionStorage.getItem("token");
                if (!token) {
                    setError("Токен отсутствует. Пожалуйста, войдите.");
                    setLoading(false);
                    return;
                }

                // Декодируем токен для получения роли
                const decoded = jwtDecode(token);
                setUserRole(decoded.role);

                const response = await axios.get("http://localhost:1111/api/candidates", {
                    headers: { Authorization: `Bearer ${token}` }
                });
                console.log("Данные с сервера:", response.data);
                setCandidates(response.data);
            } catch (error) {
                console.error("Ошибка загрузки:", error);
                setError("Ошибка при загрузке данных. Попробуйте позже.");
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, []);

    const handleOpenModal = (candidate) => {
        setSelectedCandidate(candidate);
    };

    const handleCloseModal = () => {
        setSelectedCandidate(null);
    };

    // Сортировка кандидатов по навыкам
    const sortedCandidates = [...candidates].sort((a, b) => {
        const skillsA = a.resume?.skills || "";
        const skillsB = b.resume?.skills || "";
        return sortOrder === "asc"
            ? skillsA.localeCompare(skillsB)
            : skillsB.localeCompare(skillsA);
    });

    const handleSortChange = (event) => {
        setSortOrder(event.target.value);
    };

    // Экспорт в Excel
    const handleExportToExcel = () => {
        try {
            // Формируем данные для Excel
            const data = sortedCandidates.map(candidate => ({
                Фамилия: candidate.user?.lastName || "Не указано",
                Имя: candidate.user?.firstName || "Не указано",
                Навыки: candidate.resume?.skills || "Не указано",
                Образование: candidate.resume?.education || "Не указано",
                Компании: candidate.resume?.campaigns || "Не указано",
                Обязанности: candidate.resume?.responsibilities || "Не указано",
                Сертификаты: candidate.resume?.certifications || "Не указано",
                Языки: candidate.resume?.languages || "Не указано",
                Проекты: candidate.resume?.projects || "Не указано"
            }));

            const worksheet = XLSX.utils.json_to_sheet(data);
            worksheet['!cols'] = [
                { wch: 20 }, // Фамилия
                { wch: 20 }, // Имя
                { wch: 30 }, // Навыки
                { wch: 30 }, // Образование
                { wch: 30 }, // Компании
                { wch: 40 }, // Обязанности
                { wch: 30 }, // Сертификаты
                { wch: 20 }, // Языки
                { wch: 30 }  // Проекты
            ];

            const workbook = XLSX.utils.book_new();
            XLSX.utils.book_append_sheet(workbook, worksheet, "Кандидаты");

            const today = new Date();
            const dateStr = today.toISOString().split("T")[0]; // Формат YYYY-MM-DD
            const fileName = `candidates_${dateStr}.xlsx`;

            // Скачиваем файл
            XLSX.writeFile(workbook, fileName);
        } catch (error) {
            console.error("Ошибка при экспорте в Excel:", error);
            alert("Не удалось экспортировать данные в Excel. Пожалуйста, попробуйте снова.");
        }
    };

    if (loading) return <div>Загрузка...</div>;
    if (error) return <div>{error}</div>;

    return (
        <>
            <div className={styles["candidates-header"]}>
                <div className={styles["controls-container"]}>
                    <div className={styles["sort-container"]}>
                        <select
                            id="sortOrder"
                            value={sortOrder}
                            onChange={handleSortChange}
                            className={styles["sort-select"]}
                        >
                            <option value="asc">По алфавиту (A-Z)</option>
                            <option value="desc">По алфавиту (Z-A)</option>
                        </select>
                    </div>
                    {userRole === "HR" && (
                        <button
                            className={styles["export-button"]}
                            onClick={handleExportToExcel}
                        >
                            Сохранить в файл
                        </button>
                    )}
                </div>
            </div>

            <div className={styles["candidates-container"]}>
                {sortedCandidates.map((candidate, index) => (
                    <div
                        key={candidate.id || `${candidate.user?.firstName}-${candidate.user?.lastName}-${index}`}
                        className={styles["candidate-card"]}
                        onClick={() => handleOpenModal(candidate)}
                    >
                        <img
                            src={candidate.user?.photo || "/images/default-avatar.png"}
                            alt={`${candidate.user?.firstName} ${candidate.user?.lastName}`}
                            className={styles["candidate-photo"]}
                        />
                        <h2>{candidate.user?.lastName} {candidate.user?.firstName}</h2>
                        <p>
                            <FaTools className={styles.icon} />
                            <strong>Навыки:</strong> {candidate.resume?.skills || "Не указано"}
                        </p>
                        <p>
                            <FaGraduationCap className={styles.icon} />
                            <strong>Образование:</strong> {candidate.resume?.education || "Не указано"}
                        </p>
                    </div>
                ))}
            </div>

            {selectedCandidate && (
                <Modal_candidate candidate={selectedCandidate} onClose={handleCloseModal} />
            )}
        </>
    );
};

export default Candidates;