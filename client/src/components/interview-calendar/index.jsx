import React, { useState, useEffect, useRef } from 'react';
import Calendar from 'react-calendar';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import styles from './styles.module.scss';
import 'react-calendar/dist/Calendar.css';

const InterviewCalendar = ({ userRole }) => {
    const [interviews, setInterviews] = useState([]);
    const [openInterviewMenu, setOpenInterviewMenu] = useState(null);
    const [editingInterview, setEditingInterview] = useState(null);
    const menuRef = useRef(null);
    const navigate = useNavigate();

    useEffect(() => {
        const fetchInterviews = async () => {
            try {
                const res = await axios.get('http://localhost:1111/api/interviews', {
                    headers: {
                        Authorization: `Bearer ${sessionStorage.getItem('token')}`
                    }
                });
                setInterviews(res.data);
            } catch (error) {
                console.error('Ошибка загрузки собеседований:', error.response?.data || error.message);
            }
        };
        fetchInterviews();
    }, []);

    useEffect(() => {
        const handleClickOutside = (event) => {
            if (menuRef.current && !menuRef.current.contains(event.target)) {
                setOpenInterviewMenu(null);
                setEditingInterview(null);
            }
        };
        document.addEventListener('click', handleClickOutside);
        return () => {
            document.removeEventListener('click', handleClickOutside);
        };
    }, []);

    const toggleInterviewMenu = (interviewId, event) => {
        event.stopPropagation();
        setOpenInterviewMenu(openInterviewMenu === interviewId ? null : interviewId);
    };

    const handleDeleteInterview = async (interviewId) => {
        if (window.confirm('Вы уверены, что хотите удалить это собеседование?')) {
            try {
                await axios.delete(`http://localhost:1111/api/interviews/${interviewId}`, {
                    headers: {
                        Authorization: `Bearer ${sessionStorage.getItem('token')}`
                    }
                });
                setInterviews(prev => prev.filter(i => i.id !== interviewId));
                setOpenInterviewMenu(null);
            } catch (error) {
                console.error('Ошибка удаления собеседования:', error.response?.data || error.message);
            }
        }
    };

    const handleEditInterviewStart = (interview, event) => {
        event.stopPropagation();
        setEditingInterview({
            id: interview.id,
            date: new Date(interview.date).toISOString().slice(0, 16),
            position: interview.position
        });
        setOpenInterviewMenu(null);
    };

    const handleEditInterviewSave = async (interviewId) => {
        if (!editingInterview.date) {
            return;
        }
        if (!editingInterview.position) {
            return;
        }
        try {
            const response = await axios.put(`http://localhost:1111/api/interviews/${interviewId}`, {
                date: new Date(editingInterview.date).toISOString(),
                position: editingInterview.position
            }, {
                headers: {
                    Authorization: `Bearer ${sessionStorage.getItem('token')}`
                }
            });
            setInterviews(prev => prev.map(i =>
                i.id === interviewId
                    ? { ...i, date: new Date(editingInterview.date), position: editingInterview.position }
                    : i
            ));
            setEditingInterview(null);
        } catch (error) {
            console.error('Ошибка обновления собеседования:', error.response?.data || error.message);
        }
    };

    const handleEditInterviewCancel = () => {
        setEditingInterview(null);
    };

    const handleReportRedirect = (interviewId) => {
        navigate(`/report/${interviewId}`);
        setOpenInterviewMenu(null);
    };

    const isPastOrToday = (interviewDate) => {
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        const interview = new Date(interviewDate);
        interview.setHours(0, 0, 0, 0);
        return interview <= today;
    };

    const isPast = (interviewDate) => {
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        const interview = new Date(interviewDate);
        interview.setHours(0, 0, 0, 0);
        return interview < today;
    };

    const renderInterviewTile = ({ date, view }) => {
        if (view !== 'month') return null;

        const dateStr = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
        const dayInterviews = interviews.filter(interview => {
            const interviewDate = new Date(interview.date).toISOString().slice(0, 10);
            return interviewDate === dateStr;
        });

        return (
            <div className={styles.interviewTile}>
                {dayInterviews.map(interview => (
                    <div
                        key={interview.id}
                        className={styles.interviewItem}
                        onClick={(e) => userRole === 'Кандидат' && isPastOrToday(interview.date) ? null : toggleInterviewMenu(interview.id, e)}
                        ref={menuRef}
                    >
                        {editingInterview && editingInterview.id === interview.id ? (
                            <div className={styles.editForm}>
                                <input
                                    type="datetime-local"
                                    className={styles.editInput}
                                    value={editingInterview.date}
                                    onChange={(e) => setEditingInterview({ ...editingInterview, date: e.target.value })}
                                    min={new Date().toISOString().slice(0, 16)}
                                />
                                <input
                                    type="text"
                                    className={styles.editInput}
                                    value={editingInterview.position}
                                    onChange={(e) => setEditingInterview({ ...editingInterview, position: e.target.value })}
                                    placeholder="Позиция"
                                />
                                <div className={styles.editActions}>
                                    <button
                                        className={styles.saveButton}
                                        onClick={() => handleEditInterviewSave(interview.id)}
                                    >
                                        Сохранить
                                    </button>
                                    <button
                                        className={styles.cancelButton}
                                        onClick={handleEditInterviewCancel}
                                    >
                                        Отмена
                                    </button>
                                </div>
                            </div>
                        ) : openInterviewMenu === interview.id ? (
                            <div className={styles.menuButtons}>
                                {userRole === 'HR' && isPastOrToday(interview.date) ? (
                                    <div
                                        className={styles.reportButton}
                                        role="button"
                                        tabIndex={0}
                                        onClick={() => handleReportRedirect(interview.id)}
                                        onKeyDown={(e) => e.key === 'Enter' && handleReportRedirect(interview.id)}
                                    >
                                        Отчет
                                    </div>
                                ) : userRole === 'HR' && !isPastOrToday(interview.date) ? (
                                    <>
                                        <div
                                            className={styles.editButton}
                                            role="button"
                                            tabIndex={0}
                                            onClick={(e) => handleEditInterviewStart(interview, e)}
                                            onKeyDown={(e) => e.key === 'Enter' && handleEditInterviewStart(interview, e)}
                                        >
                                            Обновить
                                        </div>
                                        <div
                                            className={styles.deleteButton}
                                            role="button"
                                            tabIndex={0}
                                            onClick={() => handleDeleteInterview(interview.id)}
                                            onKeyDown={(e) => e.key === 'Enter' && handleDeleteInterview(interview.id)}
                                        >
                                            Удалить
                                        </div>
                                    </>
                                ) : (
                                    <>
                                        {!isPastOrToday(interview.date) && (
                                            <>
                                                <div
                                                    className={styles.editButton}
                                                    role="button"
                                                    tabIndex={0}
                                                    onClick={(e) => handleEditInterviewStart(interview, e)}
                                                    onKeyDown={(e) => e.key === 'Enter' && handleEditInterviewStart(interview, e)}
                                                >
                                                    Обновить
                                                </div>
                                                <div
                                                    className={styles.deleteButton}
                                                    role="button"
                                                    tabIndex={0}
                                                    onClick={() => handleDeleteInterview(interview.id)}
                                                    onKeyDown={(e) => e.key === 'Enter' && handleDeleteInterview(interview.id)}
                                                >
                                                    Удалить
                                                </div>
                                            </>
                                        )}
                                    </>
                                )}
                            </div>
                        ) : (
                            <>
                                <span className={styles.interviewPosition}>{interview.position}</span>
                                {userRole === 'Кандидат' && isPast(interview.date) ? (
                                    <span className={styles.resultText}>{interview.result || 'Ожидание'}</span>
                                ) : (
                                    <span className={styles.interviewTime}>
                                        {new Date(interview.date).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                                    </span>
                                )}
                                <span className={styles.interviewPerson}>
                                    {userRole === 'Кандидат' ? `${interview.hrFirstName} ${interview.hrLastName}` : `${interview.candidateFirstName} ${interview.candidateLastName}`}
                                </span>
                            </>
                        )}
                    </div>
                ))}
            </div>
        );
    };

    return (
        <div className={styles.interviewCalendar}>
            <Calendar
                tileContent={renderInterviewTile}
                className={styles.customCalendar}
                minDate={new Date()}
                showNeighboringMonth={false}
                locale="ru-RU"
                formatShortWeekday={(locale, date) => {
                    const weekdays = ['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс'];
                    return weekdays[date.getDay() === 0 ? 6 : date.getDay() - 1];
                }}
            />
        </div>
    );
};

export default InterviewCalendar;