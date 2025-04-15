import React, { useEffect, useState } from "react";
import axios from "axios";
import { jwtDecode } from "jwt-decode";
import styles from "./styles.module.scss";
import Modal_employee from "@components/Modal_vacancy";
import { AiFillHeart, AiOutlineHeart } from "react-icons/ai";
import { useNavigate } from "react-router-dom";
import Toast from "@components/Toast/index.jsx";
const Vacancies = () => {
    const [vacancies, setVacancies] = useState([]);
    const [filteredVacancies, setFilteredVacancies] = useState([]);
    const [loading, setLoading] = useState(true);
    const [selectedVacancy, setSelectedVacancy] = useState(null);
    const [currentUser, setCurrentUser] = useState(null);
    const [searchQuery, setSearchQuery] = useState("");
    const [statusFilter, setStatusFilter] = useState("Все");
    const [favorites, setFavorites] = useState([]);
    const navigate = useNavigate();
    const [toast, setToast] = useState(null);

    useEffect(() => {
        const token = sessionStorage.getItem("token");
        if (!token) return console.error("Токен отсутствует");

        const decoded = jwtDecode(token);
        setCurrentUser({ email: decoded.sub, role: decoded.role });

        const fetchData = async () => {
            try {
                const response = await axios.get("http://localhost:1111/api/vacancies", {
                    headers: { Authorization: `Bearer ${token}` },
                });
                const enrichedData = response.data.map(v => ({
                    ...v,
                    creatorEmail: v.employee?.user?.email || null
                }));
                setVacancies(enrichedData);
                setFilteredVacancies(enrichedData);
            } catch (error) {
                console.error("Ошибка загрузки:", error);
            } finally {
                setLoading(false);
            }
        };


        fetchData();

        const savedFavorites = JSON.parse(localStorage.getItem("favoriteVacancies")) || [];
        setFavorites(savedFavorites);
    }, []);

    useEffect(() => {
        let filtered = [...vacancies];

        if (searchQuery) {
            const query = searchQuery.toLowerCase();
            filtered = filtered.filter(v => {
                return (
                    v.position?.toLowerCase().includes(query) ||
                    v.requirements?.toLowerCase().includes(query) ||
                    v.description?.toLowerCase().includes(query) ||
                    v.department?.toLowerCase().includes(query) ||
                    v.status?.toLowerCase().includes(query)
                );
            });
        }

        if (statusFilter === "Мои" && currentUser) {
            filtered = filtered.filter(v => v.creatorEmail === currentUser.email);
        } else if (statusFilter !== "Все" && statusFilter !== "Мои") {
            filtered = filtered.filter(v => v.status === statusFilter);
        }

        setFilteredVacancies(filtered);
    }, [searchQuery, statusFilter, vacancies, currentUser]);

    const toggleFavorite = (id) => {
        let updatedFavorites;
        if (favorites.includes(id)) {
            updatedFavorites = favorites.filter(favId => favId !== id);
        } else {
            updatedFavorites = [...favorites, id];
        }

        setFavorites(updatedFavorites);
        localStorage.setItem("favoriteVacancies", JSON.stringify(updatedFavorites));
    };

    const handleOpenModal = (vacancy) => setSelectedVacancy(vacancy);
    const handleCloseModal = () => setSelectedVacancy(null);

    if (loading) return <div>Загрузка...</div>;

    return (
        <>
            <div className={styles["top-controls"]}>
                {currentUser?.role === "HR" && (
                    <button
                        className={styles["create-button"]}
                        onClick={() => navigate("/vacancies/create")}
                    >
                        Создать вакансию
                    </button>
                )}

                <input
                    type="text"
                    placeholder="Поиск..."
                    className={styles["search-input"]}
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                />

                <select
                    className={styles["status-filter"]}
                    value={statusFilter}
                    onChange={(e) => setStatusFilter(e.target.value)}
                >
                    <option value="Все">Все</option>
                    <option value="Активно">Активно</option>
                    <option value="Неактивно">Неактивно</option>
                    <option value="Заморожено">Заморожено</option>
                    {currentUser?.role === "HR" && (
                        <option value="Мои">Мои</option>
                    )}
                </select>
            </div>

            <div className={styles["vacancies-container"]}>
                {filteredVacancies.map((vacancy) => (
                    <div
                        key={vacancy.vacancies_id}
                        className={styles["vacancy-card"]}
                        onClick={() => handleOpenModal(vacancy)}
                    >
                        <div className={styles["card-header"]}>
                            <h2>{vacancy.position}</h2>
                            {currentUser?.role === "Candidate" && (
                                <div
                                    className={styles["heart-icon"]}
                                    onClick={(e) => {
                                        e.stopPropagation();
                                        toggleFavorite(vacancy.vacancies_id);
                                    }}
                                >
                                    {favorites.includes(vacancy.vacancies_id) ? (
                                        <AiFillHeart color="red" size={24} />
                                    ) : (
                                        <AiOutlineHeart size={24} />
                                    )}
                                </div>
                            )}
                        </div>
                        <p><strong>Требования:</strong> {vacancy.requirements}</p>
                        <p><strong>Зарплата:</strong> {vacancy.salary} USD</p>
                        <p><strong>Статус:</strong> {vacancy.status}</p>
                    </div>
                ))}
            </div>

            {selectedVacancy && (
                <Modal_employee
                    vacancy={selectedVacancy}
                    currentUser={currentUser}
                    onClose={handleCloseModal}
                    onDelete={(id) => {
                        setVacancies(prev => prev.filter(v => v.vacancies_id !== id));
                    }}
                    toast={toast}
                    setToast={setToast}
                />

            )}

            {toast && (
                <Toast
                    message={toast.message}
                    type={toast.type}
                    onClose={() => setToast(null)}
                />
            )}
        </>
    );
};

export default Vacancies;
