import React, { useState } from "react";
import styles from "./styles.module.scss";

const HrPagination = ({ employees }) => {
    const [currentIndex, setCurrentIndex] = useState(0);

    if (!employees || employees.length === 0) {
        return <div>Нет данных о сотрудниках</div>;
    }

    const extendedEmployees = [...employees, ...employees, ...employees];

    const itemsPerPage = 3;

    const nextSlide = () => {
        setCurrentIndex((prevIndex) => {
            const newIndex = prevIndex + 1;
            if (newIndex >= employees.length) {
                return 0;
            }
            return newIndex;
        });
    };

    const prevSlide = () => {
        setCurrentIndex((prevIndex) => {
            const newIndex = prevIndex - 1;
            if (newIndex < 0) {
                return employees.length - 1;
            }
            return newIndex;
        });
    };

    const visibleEmployees = extendedEmployees.slice(
        currentIndex + employees.length,
        currentIndex + employees.length + itemsPerPage
    );

    return (
        <div className={styles.paginationContainer}>
            <button onClick={prevSlide} className={styles.arrowButton}>‹</button>
            <div className={styles.photosContainer}>
                {visibleEmployees.map((employee, index) => (
                    <div key={index} className={styles.photoWrapper}>
                        <img src={employee.photo} alt={`HR ${index + 1}`} className={styles.photo} />
                        <h3>{employee.firstName} {employee.lastName}</h3>
                        <p>{employee.position}</p>
                        <hr className={styles.divider} />
                    </div>
                ))}
            </div>
            <button onClick={nextSlide} className={styles.arrowButton}>›</button>
        </div>
    );
};

export default HrPagination;