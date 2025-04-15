import React, { useState, useEffect, useRef } from 'react';
import useNotifications from '@hooks/useNotifications';
import styles from '@pages/notifications/styles.module.scss';
import Toast from '@components/toast';

const NotificationChat = ({ selectedChat }) => {
    const { messages, updateMessageResponse, editMessage, deleteMessage } = useNotifications(selectedChat);
    const [openMenuIndex, setOpenMenuIndex] = useState(null);
    const [editingMessage, setEditingMessage] = useState(null);
    const [toasts, setToasts] = useState([]);
    const menuRef = useRef(null);

    const addToast = (message, type) => {
        const id = Date.now();
        setToasts(prev => [...prev, { id, message, type }]);
    };

    const removeToast = (id) => {
        setToasts(prev => prev.filter(toast => toast.id !== id));
    };

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
        setEditingMessage({ ...msg });
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
                result = await editMessage(editingMessage.notificationId, null, editingMessage.date, null);
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

    const handleResponse = async (notificationId, response) => {
        const result = await updateMessageResponse(notificationId, response);
        if (result.success) {
            addToast(result.message, 'success');
            setOpenMenuIndex(null);
        } else {
            addToast(result.error, 'error');
        }
    };

    return (
        <div className={styles.chatWindow}>
            <div className={styles.chatHeader}>{selectedChat.recipientName}</div>
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
                                            value={editingMessage.date ? new Date(editingMessage.date).toISOString().slice(0, 16) : ''}
                                            onChange={(e) => setEditingMessage({ ...editingMessage, date: e.target.value })}
                                            min={new Date().toISOString().slice(0, 16)}
                                        />
                                    ) : editingMessage.type === 'response' ? (
                                        <input
                                            type="text"
                                            value={editingMessage.vacancyName || ''}
                                            onChange={(e) => setEditingMessage({ ...editingMessage, vacancyName: e.target.value })}
                                            placeholder="Название вакансии"
                                        />
                                    ) : (
                                        <input
                                            type="text"
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
                                                <div onClick={() => handleResponse(msg.notificationId, 'Согласиться')}>
                                                    Согласиться
                                                </div>
                                                <div onClick={() => handleResponse(msg.notificationId, 'Отказаться')}>
                                                    Отказаться
                                                </div>
                                            </>
                                        )}
                                    </div>
                                )}
                            </div>
                        </div>
                    ))
                ) : (
                    <div className={styles.emptyChat}>Нет сообщений</div>
                )}
            </div>
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