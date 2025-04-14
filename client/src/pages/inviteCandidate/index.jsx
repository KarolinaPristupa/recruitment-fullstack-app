import React, { useEffect, useState } from "react";
import axios from "axios";
import styles from "@pages/candidates/styles.module.scss";
import Modal_candidate from "@components/Modal_candidate";
import "react-calendar/dist/Calendar.css";

const InviteCandidate = () => {
    const [candidates, setCandidates] = useState([]);
    const [selectedCandidate, setSelectedCandidate] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchData = async () => {
            try {
                const token = sessionStorage.getItem("token");
                const response = await axios.get("http://localhost:1111/api/candidates", {
                    headers: { Authorization: `Bearer ${token}` }
                });
                setCandidates(response.data);
            } catch (error) {
                setError("Ошибка загрузки кандидатов");
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

    if (loading) return <div>Загрузка...</div>;
    if (error) return <div>{error}</div>;

    return (
        <div className={styles["candidates-container"]}>
            {candidates.map((candidate, index) => (
                <div
                    key={candidate.id || index}
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
                        <strong>Навыки:</strong> {candidate.resume?.skills || "Не указано"}
                    </p>
                </div>
            ))}

            {selectedCandidate && (
                <Modal_candidate candidate={selectedCandidate} onClose={handleCloseModal} />
            )}
        </div>
    );
};

export default InviteCandidate;
