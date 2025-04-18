import React, { useState, useEffect } from 'react';
import axios from 'axios';
import PersonalDataForm from '@components/personal-data-form';
import ResumeForm from '@components/resume-form';
import ResumePreview from '@components/resum-preview';

import styles from "./styles.module.scss";

const Candidate = () => {
    const [candidate, setCandidate] = useState(null);
    const [loading, setLoading] = useState(true);
    const token = sessionStorage.getItem('token');

    const fetchData = async () => {
        try {
            const response = await axios.get('http://localhost:1111/api/candidate', {
                headers: { Authorization: `Bearer ${token}` }
            });
            setCandidate(response.data);
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
            {candidate && candidate.user ? (
                <PersonalDataForm
                    user={candidate.user}
                    onUpdate={(updatedUser) =>
                        setCandidate(prev => ({
                            ...prev,
                            user: {
                                ...prev.user,
                                ...updatedUser
                            }
                        }))
                    }
                    refreshCandidate={fetchData}
                />
            ) : (
                <div>Загрузка данных...</div>
            )}

            <div className={styles.panels}>
                <div>
                    <ResumeForm
                        resume={candidate.resume || {}}
                        onUpdate={(newResume) =>
                            setCandidate(prev => ({
                                ...prev,
                                resume: { ...prev.resume, ...newResume }
                            }))
                        }
                    />
                </div>
                <div className={styles.rightPanel}>
                    <ResumePreview candidate={candidate} />
                </div>
            </div>
        </div>
    );
};

export default Candidate;
