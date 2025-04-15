import React, { useEffect, useState } from "react";
import axios from "axios";
import PersonalDataForm from "@components/personal-data-form/index.jsx";
import EmployeeProfile from "@components/employee-profile/index.jsx";
import styles from "@pages/candidate/styles.module.scss";

const Employee = () => {
    const [employee, setEmployee] = useState(null);
    const [loading, setLoading] = useState(true);
    const token = sessionStorage.getItem('token');

    const fetchData = async () => {
        try {
            const response = await axios.get('http://localhost:1111/api/employee', {
                headers: { Authorization: `Bearer ${token}` }
            });
            setEmployee(response.data);
        } catch (error) {
            console.error("Ошибка загрузки:", error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchData();
    }, []);

    if (loading) return <div>Загрузка...</div>;

    return (
        <div className={styles.container}>
            {employee && employee.user ? (
                <div className={styles.panels} style={{marginTop: "0rem"}}>
                    <PersonalDataForm
                        user={employee.user}
                        onUpdate={(updatedUser) =>
                            setEmployee(prev => ({
                                ...prev,
                                user: {
                                    ...prev.user,
                                    ...updatedUser
                                }
                            }))
                        }
                        refreshCandidate={fetchData}
                    />
                    <div className={styles.rightPanel}>
                        <EmployeeProfile employee={employee} />
                    </div>
                </div>
            ) : (
                <div>Загрузка данных...</div>
            )}
        </div>
    );
};

export default Employee;
