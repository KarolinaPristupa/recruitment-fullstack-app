import React, { useState } from "react";
import ReactDOM from "react-dom";
import { FaTimes } from "react-icons/fa";
import ResumePreview from "@components/resum-preview";
import Toast from "@components/toast";
import styles from "./styles.module.scss";
import "react-calendar/dist/Calendar.css";
import ModalInvite from "@components/modal-invite";

const Modal_candidate = ({ candidate, onClose }) => {
    const [toast, setToast] = useState({ show: false, message: "", type: "" });
    const [showInviteModal, setShowInviteModal] = useState(false);
    const [candidateToInvite, setCandidateToInvite] = useState(null);

    const handleInviteClick = () => {
        console.log("Пригласить нажали, состояние showInviteModal: ", showInviteModal);
        setCandidateToInvite(candidate);
        setShowInviteModal(true);
    };

    const vacancyToInvite = JSON.parse(sessionStorage.getItem("vacancyToInvite") || "{}");
    console.log("vacancyToInvite:", vacancyToInvite);

    return ReactDOM.createPortal(
        <div className={styles.overlay}>
            <div className={styles.modal}>
                <button className={styles.close} onClick={onClose}>
                    <FaTimes />
                </button>

                <div className={styles.modalContent}>
                    <ResumePreview candidate={candidate} />
                </div>

                <button
                    className={styles.inviteButton}
                    onClick={handleInviteClick}
                >
                    Пригласить
                </button>

                {showInviteModal && (
                    <ModalInvite
                        candidate={candidateToInvite}
                        vacancy={vacancyToInvite}
                        onClose={() => setShowInviteModal(false)}
                    />
                )}

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

export default Modal_candidate;
