import React, { useEffect, useState } from "react";
import axios from "axios";
import { FaGraduationCap, FaTools, FaEnvelope, FaPhone } from "react-icons/fa"; // Иконки
import styles from "./styles.module.scss";
import Modal_candidate from "@components/modal-candidate"; // Импортируем модальное окно

const Candidates = () => {
    const [candidates, setCandidates] = useState([]);
    const [loading, setLoading] = useState(true);
    const [selectedCandidate, setSelectedCandidate] = useState(null);
    const [error, setError] = useState(null); // Для хранения ошибки
    const [sortOrder, setSortOrder] = useState("asc"); // Порядок сортировки: asc (A-Z) или desc (Z-A)

    useEffect(() => {
        const fetchData = async () => {
            try {
                const token = sessionStorage.getItem("token");
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

    if (loading) return <div>Загрузка...</div>;
    if (error) return <div>{error}</div>; // Отображение ошибки, если она есть

    return (
        <>
            <div className={styles["candidates-header"]}>
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