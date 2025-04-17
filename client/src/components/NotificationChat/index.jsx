import React, { useState, useEffect, useRef } from 'react';
import useNotifications from '@hooks/useNotifications';
import styles from '@pages/notifications/styles.module.scss';
import Toast from '@components/Toast';
import Calendar from 'react-calendar';
import axios from 'axios';
import { jwtDecode } from 'jwt-decode';
import 'react-calendar/dist/Calendar.css';

const NotificationChat = ({ selectedChat }) => {
    const { messages, updateMessageResponse, editMessage, deleteMessage } = useNotifications(selectedChat?.recipientId !== 'interviews' ? selectedChat : null);
    const [openMenuIndex, setOpenMenuIndex] = useState(null);
    const [editingMessage, setEditingMessage] = useState(null);
    const [showCalendar, setShowCalendar] = useState(null);
    const [interviewDate, setInterviewDate] = useState('');
    const [toasts, setToasts] = useState([]);
    const [interviews, setInterviews] = useState([]);
    const [userRole, setUserRole] = useState(null);
    const menuRef = useRef(null);

    const addToast = (message, type) => {
        const id = Date.now();
        setToasts(prev => [...prev, { id, message, type }]);
    };

    const removeToast = (id) => {
        setToasts(prev => prev.filter(toast => toast.id !== id));
    };

    useEffect(() => {
        const token = sessionStorage.getItem('token');
        if (token) {
            try {
                const decoded = jwtDecode(token);
                setUserRole(decoded.role);
            } catch (error) {
                console.error('Ошибка декодирования токена:', error);
                addToast('Ошибка авторизации', 'error');
            }
        }
    }, []);

    useEffect(() => {
        if (selectedChat?.recipientId === 'interviews') {
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
                    addToast('Не удалось загрузить собеседования', 'error');
                }
            };
            fetchInterviews();
        }
    }, [selectedChat]);

    useEffect(() => {
        const handleClickOutside = (event) => {
            if (menuRef.current && !menuRef.current.contains(event.target)) {
                setOpenMenuIndex(null);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, []);

    if (!selectedChat) {
        return <div className={styles.chatWindow}></div>;
    }

    const toggleMenu = (idx, event) => {
        event.stopPropagation();
        setOpenMenuIndex(openMenuIndex === idx ? null : idx);
    };

    const handleEditStart = (msg) => {
        setEditingMessage({ ...msg, date: msg.date ? new Date(msg.date).toISOString().slice(0, 16) : '' });
        setOpenMenuIndex(null);
    };

    const handleEditSave = async () => {
        if (editingMessage) {
            let result;
            if (editingMessage.type === 'invite') {
                if (!editingMessage.date) {
                    addToast('Укажите дату и время', 'error');
                    return;
                }
                const date = new Date(editingMessage.date);
                const formattedDate = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}T${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}:00`;
                result = await editMessage(editingMessage.notificationId, null, formattedDate, null);
            } else if (editingMessage.type === 'response') {
                if (!editingMessage.vacancyName) {
                    addToast('Укажите название вакансии', 'error');
                    return;
                }
                result = await editMessage(editingMessage.notificationId, null, null, editingMessage.vacancyName);
            } else {
                if (!editingMessage.details) {
                    addToast('Укажите детали', 'error');
                    return;
                }
                result = await editMessage(editingMessage.notificationId, editingMessage.details, null, null);
            }

            if (result.success) {
                addToast(result.message, 'success');
                setEditingMessage(null);
            } else {
                addToast(result.error, 'error');
            }
        }
    };

    const handleEditCancel = () => {
        setEditingMessage(null);
    };

    const handleDelete = async (notificationId) => {
        const result = await deleteMessage(notificationId);
        if (result.success) {
            addToast(result.message, 'success');
            setOpenMenuIndex(null);
        } else {
            addToast(result.error, 'error');
        }
    };

    const handleResponse = async (notificationId, response, msg) => {
        if (response === 'Согласие' && msg.type === 'response') {
            setShowCalendar(notificationId);
            setOpenMenuIndex(null);
        } else {
            const result = await updateMessageResponse(notificationId, response, null, msg.type);
            if (result.success) {
                addToast(result.message, 'success');
                setOpenMenuIndex(null);
            } else {
                addToast(result.error, 'error');
            }
        }
    };

    const handleCalendarSubmit = async (notificationId) => {
        if (!interviewDate) {
            addToast('Укажите дату и время', 'error');
            return;
        }
        const result = await updateMessageResponse(notificationId, 'Согласие', new Date(interviewDate), 'response');
        if (result.success) {
            addToast(result.message, 'success');
            setShowCalendar(null);
            setInterviewDate('');
        } else {
            addToast(result.error, 'error');
        }
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
                    <div key={interview.id} className={styles.interviewItem}>
                        <span className={styles.interviewPosition}>{interview.position}</span>
                        <span className={styles.interviewTime}>
                            {new Date(interview.date).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                        </span>
                        <span className={styles.interviewPerson}>
                            {userRole === 'CANDIDATE' ? `${interview.hrFirstName} ${interview.hrLastName}` : `${interview.candidateFirstName} ${interview.candidateLastName}`}
                        </span>
                    </div>
                ))}
            </div>
        );
    };

    return (
        <div className={styles.chatWindow}>
            <div className={styles.chatHeader}>
                {selectedChat.recipientId === 'interviews' ? 'Собеседования' : selectedChat.recipientName}
            </div>
            {selectedChat.recipientId === 'interviews' ? (
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
            ) : (
                <div className={styles.messages}>
                    {Array.isArray(messages) && messages.length > 0 ? (
                        messages.map((msg, idx) => (
                            <div
                                key={msg.notificationId}
                                className={`${styles.message} ${msg.sentByMe ? styles.sent : styles.received}`}
                                onClick={(e) => toggleMenu(idx, e)}
                            >
                                {editingMessage && editingMessage.notificationId === msg.notificationId ? (
                                    <div className={styles.messageContent}>
                                        <div className={styles.messageText}>{msg.message}</div>
                                        {editingMessage.type === 'invite' ? (
                                            <input
                                                type="datetime-local"
                                                className={styles.calendarInput}
                                                value={editingMessage.date}
                                                onChange={(e) => setEditingMessage({ ...editingMessage, date: e.target.value })}
                                                min={new Date().toISOString().slice(0, 16)}
                                            />
                                        ) : editingMessage.type === 'response' ? (
                                            <input
                                                type="text"
                                                className={styles.textInput}
                                                value={editingMessage.vacancyName || ''}
                                                onChange={(e) => setEditingMessage({ ...editingMessage, vacancyName: e.target.value })}
                                                placeholder="Название вакансии"
                                            />
                                        ) : (
                                            <input
                                                type="text"
                                                className={styles.textInput}
                                                value={editingMessage.details || ''}
                                                onChange={(e) => setEditingMessage({ ...editingMessage, details: e.target.value })}
                                                placeholder="Детали"
                                            />
                                        )}
                                        <div className={styles.editActions}>
                                            <button onClick={handleEditSave}>Сохранить</button>
                                            <button onClick={handleEditCancel}>Отмена</button>
                                        </div>
                                    </div>
                                ) : (
                                    <div className={styles.messageContent}>
                                        <div className={styles.messageText}>{msg.message}</div>
                                        <div className={styles.messageDetails}>
                                            {msg.type === 'invite' && msg.date
                                                ? new Date(msg.date).toLocaleString()
                                                : msg.type === 'response' && msg.vacancyName
                                                    ? msg.vacancyName
                                                    : msg.details}
                                        </div>
                                    </div>
                                )}
                                <div className={styles.messageMeta}>
                                    {msg.response && <span className={styles.response}>{msg.response}</span>}
                                    {openMenuIndex === idx && (
                                        <div
                                            className={`${styles.dropdownMenu} ${msg.sentByMe ? styles.sentMenu : styles.receivedMenu}`}
                                            ref={menuRef}
                                        >
                                            {msg.sentByMe ? (
                                                <>
                                                    <div onClick={() => handleEditStart(msg)}>Редактировать</div>
                                                    <div onClick={() => handleDelete(msg.notificationId)}>Удалить</div>
                                                </>
                                            ) : (
                                                <>
                                                    <div onClick={() => handleResponse(msg.notificationId, 'Согласие', msg)}>
                                                        Согласиться
                                                    </div>
                                                    <div onClick={() => handleResponse(msg.notificationId, 'Отказ', msg)}>
                                                        Отказаться
                                                    </div>
                                                </>
                                            )}
                                        </div>
                                    )}
                                    {showCalendar === msg.notificationId && (
                                        <div className={styles.calendarPopup}>
                                            <input
                                                type="datetime-local"
                                                className={styles.calendarInput}
                                                value={interviewDate}
                                                onChange={(e) => setInterviewDate(e.target.value)}
                                                min={new Date().toISOString().slice(0, 16)}
                                            />
                                            <div className={styles.calendarActions}>
                                                <button onClick={() => handleCalendarSubmit(msg.notificationId)}>Подтвердить</button>
                                                <button onClick={() => setShowCalendar(null)}>Отмена</button>
                                            </div>
                                        </div>
                                    )}
                                </div>
                            </div>
                        ))
                    ) : (
                        <div className={styles.emptyChat}>Нет сообщений</div>
                    )}
                </div>
            )}
            <div className={styles.toastContainer}>
                {toasts.map(toast => (
                    <Toast
                        key={toast.id}
                        message={toast.message}
                        type={toast.type}
                        onClose={() => removeToast(toast.id)}
                    />
                ))}
            </div>
        </div>
    );
};

export default NotificationChat;