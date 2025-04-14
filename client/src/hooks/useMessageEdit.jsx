import { useState } from 'react';
import axios from 'axios';

const useMessageEdit = () => {
    const [editingMessage, setEditingMessage] = useState(null);

    const startEdit = (message) => {
        setEditingMessage({
            ...message,
            date: message.date || new Date().toISOString() // Заглушка, если date отсутствует
        });
    };

    const cancelEdit = () => {
        setEditingMessage(null);
    };

    const saveEdit = async (notificationId, updatedMessage, updatedDetails, updatedDate) => {
        try {
            await axios.put(
                `http://localhost:1111/api/notifications/${notificationId}`,
                {
                    message: updatedMessage,
                    details: updatedDetails,
                    date: updatedDate
                },
                {
                    headers: {
                        Authorization: `Bearer ${sessionStorage.getItem('token')}`
                    }
                }
            );
            setEditingMessage(null);
            return true;
        } catch (error) {
            console.error('Ошибка редактирования сообщения:', error.response?.data || error.message);
            return false;
        }
    };

    return { editingMessage, setEditingMessage, startEdit, cancelEdit, saveEdit };
};

export default useMessageEdit;