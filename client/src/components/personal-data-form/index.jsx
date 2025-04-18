import React, { useState, useEffect } from "react";
import styles from "@pages/candidate/styles.module.scss";
import Toast from "@components/toast/index.jsx";

const PersonalDataForm = ({ user, onUpdate, refreshCandidate }) => {
    const [photoPreview, setPhotoPreview] = useState(user?.photo || "/default-avatar.png");
    const [selectedFile, setSelectedFile] = useState(null);
    const [updatedUser, setUpdatedUser] = useState(user);
    const token = sessionStorage.getItem("token");
    const [toast, setToast] = useState(null);

    useEffect(() => {
        setUpdatedUser(user);
        setPhotoPreview(user?.photo || "/default-avatar.png");
    }, [user]);

    const handleChange = (field) => (e) => {
        setUpdatedUser({ ...updatedUser, [field]: e.target.value });
    };

    const handleFileSelect = (e) => {
        const file = e.target.files[0];
        if (file) {
            setSelectedFile(file);
            setPhotoPreview(URL.createObjectURL(file)); // Показываем превью сразу
        }
    };

    const handleSave = async () => {
        if (!token) {
            setToast({ message: "Ошибка: вы не авторизованы!", type: "error" });
            return;
        }

        try {
            let photoUrl = updatedUser.photo;

            if (selectedFile) {
                const formData = new FormData();
                formData.append("photo", selectedFile);

                const uploadResponse = await fetch("http://localhost:1111/api/candidate/upload-photo", {
                    method: "POST",
                    headers: { "Authorization": `Bearer ${token}` },
                    body: formData,
                });

                if (!uploadResponse.ok) throw new Error("Ошибка загрузки фото");
                const uploadData = await uploadResponse.json();
                photoUrl = `http://localhost:1111/images/${uploadData.photoUrl}`;
            }

            const response = await fetch("http://localhost:1111/api/candidate/update", {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${token}`,
                },
                body: JSON.stringify({ ...updatedUser, photo: photoUrl }),
            });

            if (!response.ok) throw new Error("Ошибка обновления данных");

            setToast({ message: "Данные успешно обновлены!", type: "success" }); // Уведомление об успехе
            refreshCandidate();
        } catch (error) {
            console.error("Ошибка:", error);
            setToast({ message: "Ошибка при обновлении данных!", type: "error" }); // Уведомление об ошибке
        }
    };

    return (
        <>
            {toast && <Toast message={toast.message} type={toast.type} onClose={() => setToast(null)} />}

            <div className={styles.section}>
                <h2>Личные данные</h2>
                <div className={styles.formContainer}>
                    <form className={styles.form}>
                        <div className={styles.formGroup}>
                            <input
                                type="text"
                                value={updatedUser.firstName || ''}
                                onChange={handleChange('firstName')}
                                placeholder="Имя"
                            />
                        </div>

                        <div className={styles.formGroup}>
                            <input
                                type="text"
                                value={updatedUser.lastName || ''}
                                onChange={handleChange('lastName')}
                                placeholder="Фамилия"
                            />
                        </div>

                        <div className={styles.formGroup}>
                            <input
                                type="email"
                                value={updatedUser.email || ''}
                                disabled
                                placeholder="Email"
                            />
                        </div>

                        <div className={styles.formGroup}>
                            <input
                                type="tel"
                                value={updatedUser.phone || ''}
                                onChange={handleChange('phone')}
                                placeholder="Телефон"
                            />
                        </div>

                        <button type="button" className={styles.button} onClick={handleSave}>
                            Сохранить изменения
                        </button>


                    </form>
                    <div className={styles.photoUpload}>
                        <img src={photoPreview} alt="Фото профиля" className={styles.photoChange} />
                        <input type="file" accept="image/*" onChange={handleFileSelect} />
                    </div>

                </div>
            </div>
        </>

    );
};

export default PersonalDataForm;
