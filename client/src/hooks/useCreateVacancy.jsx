import { useState } from "react";
import axios from "axios";

export const useCreateVacancy = () => {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(false);

    const createVacancy = async (formData) => {
        setLoading(true);
        setError(null);
        setSuccess(false);

        try {
            if (!formData?.email) {
                throw new Error("Email не передан в formData");
            }

            const response = await axios.post(
                "http://localhost:1111/api/vacancies/vacancies",
                formData,
                {
                    headers: {
                        "Content-Type": "application/json",
                    },
                }
            );

            setSuccess(true);
            return { success: true, data: response.data };
        } catch (err) {
            setError(err.message || "Ошибка при создании вакансии");
            return { success: false, error: err.message };
        } finally {
            setLoading(false);
        }
    };

    return { createVacancy, loading, error, success };
};
