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

    const fetchMessages = async () => {
        if (!selectedChat || !currentUserId) return;
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

    useEffect(() => {
        fetchMessages();
    }, [selectedChat, currentUserId]);

    const updateMessageResponse = async (notificationId, response, interviewDate, type) => {
        try {
            const payload = { response };
            if (response === 'Согласие' && type === 'response' && interviewDate) {
                const date = new Date(interviewDate);
                const formattedDate = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}T${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}:00`;
                payload.interviewDate = formattedDate;
            }
            await axios.put(
                `http://localhost:1111/api/notifications/${notificationId}/response`,
                payload,
                {
                    headers: {
                        Authorization: `Bearer ${sessionStorage.getItem('token')}`
                    }
                }
            );
            await fetchMessages();
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
            await fetchMessages();
            return { success: true, message: 'Сообщение успешно отредактировано' };
        } catch (error) {
            console.error('Ошибка редактирования сообщения:', error.response?.data || error.message);
            return { success: false, error: error.response?.data?.error || 'Ошибка при редактирования сообщения' };
        }
    };

    const deleteMessage = async (notificationId) => {
        try {
            await axios.delete(`http://localhost:1111/api/notifications/${notificationId}`, {
                headers: {
                    Authorization: `Bearer ${sessionStorage.getItem('token')}`
                }
            });
            await fetchMessages(); // Re-fetch messages to reflect server state
            return { success: true, message: 'Сообщение успешно удалено' };
        } catch (error) {
            console.error('Ошибка удаления сообщения:', error.response?.data || error.message);
            return { success: false, error: error.response?.data?.error || 'Ошибка при удалении сообщения' };
        }
    };

    return { messages, updateMessageResponse, editMessage, deleteMessage };
};

export default useNotifications;