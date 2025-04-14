import { useEffect, useState } from 'react';
import axios from 'axios';
import { jwtDecode } from 'jwt-decode';

const useNotifications = (selectedChat) => {
    const [messages, setMessages] = useState([]);
    const [currentUserId, setCurrentUserId] = useState(null);

    useEffect(() => {
        const token = sessionStorage.getItem('token');
        if (token) {
            try {
                const decoded = jwtDecode(token);
                setCurrentUserId(decoded.sub);
                console.log("Current user ID (email):", decoded.sub);
            } catch (error) {
                console.error('Ошибка декодирования токена:', error);
                return { success: false, error: 'Ошибка авторизации' };
            }
        }
    }, []);

    useEffect(() => {
        if (!selectedChat || !currentUserId) return;

        const fetchMessages = async () => {
            try {
                const res = await axios.get(`http://localhost:1111/api/notifications/chat/${selectedChat.recipientId}`, {
                    headers: {
                        Authorization: `Bearer ${sessionStorage.getItem('token')}`
                    }
                });
                console.log("Ответ API:", res.data);
                const transformedMessages = res.data.map(msg => ({
                    notificationId: msg.messageId,
                    message: msg.message,
                    details: msg.details,
                    sentByMe: msg.sentByMe,
                    response: msg.response || null,
                    type: msg.type || 'message',
                    date: msg.date || null,
                    vacancyName: msg.vacancyName || null
                }));
                setMessages(transformedMessages);
            } catch (error) {
                console.error('Ошибка загрузки сообщений:', error.response?.data || error.message);
                return { success: false, error: 'Не удалось загрузить сообщения' };
            }
        };

        fetchMessages();
    }, [selectedChat, currentUserId]);

    const updateMessageResponse = async (notificationId, response) => {
        try {
            await axios.put(
                `http://localhost:1111/api/notifications/${notificationId}/response`,
                { response },
                {
                    headers: {
                        Authorization: `Bearer ${sessionStorage.getItem('token')}`
                    }
                }
            );
            setMessages(messages.map(msg =>
                msg.notificationId === notificationId ? { ...msg, response } : msg
            ));
            return { success: true, message: `Ответ "${response}" успешно отправлен` };
        } catch (error) {
            console.error('Ошибка обновления ответа:', error.response?.data || error.message);
            return { success: false, error: error.response?.data?.error || 'Ошибка при отправке ответа' };
        }
    };

    const editMessage = async (notificationId, details, date, vacancyName) => {
        try {
            const payload = {};
            if (details) payload.details = details;
            if (date) payload.date = date;
            if (vacancyName) payload.vacancyName = vacancyName;

            await axios.put(
                `http://localhost:1111/api/notifications/${notificationId}`,
                payload,
                {
                    headers: {
                        Authorization: `Bearer ${sessionStorage.getItem('token')}`
                    }
                }
            );
            setMessages(messages.map(msg =>
                msg.notificationId === notificationId ? { ...msg, details, date, vacancyName } : msg
            ));
            return { success: true, message: 'Сообщение успешно отредактировано' };
        } catch (error) {
            console.error('Ошибка редактирования сообщения:', error.response?.data || error.message);
            return { success: false, error: error.response?.data?.error || 'Ошибка при редактировании сообщения' };
        }
    };

    const deleteMessage = async (notificationId) => {
        try {
            await axios.delete(`http://localhost:1111/api/notifications/${notificationId}`, {
                headers: {
                    Authorization: `Bearer ${sessionStorage.getItem('token')}`
                }
            });
            setMessages(messages.filter(msg => msg.notificationId !== notificationId));
            return { success: true, message: 'Сообщение успешно удалено' };
        } catch (error) {
            console.error('Ошибка удаления сообщения:', error.response?.data || error.message);
            return { success: false, error: error.response?.data?.error || 'Ошибка при удалении сообщения' };
        }
    };

    return { messages, updateMessageResponse, editMessage, deleteMessage };
};

export default useNotifications;