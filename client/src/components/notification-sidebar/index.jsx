import React from 'react';
import useNotificationChats from '@hooks/useNotificationChats';
import styles from '@pages/notifications/styles.module.scss';

const NotificationSidebar = ({ onSelectChat, selectedChat }) => {
    const { chats } = useNotificationChats();

    const interviewChat = {
        recipientId: 'interviews',
        recipientName: 'Собеседования'
    };

    const allChats = [interviewChat, ...chats];

    return (
        <div className={styles.sidebar}>
            {allChats.map(chat => (
                <div
                    key={chat.recipientId}
                    className={`${styles.chatItem} ${selectedChat?.recipientId === chat.recipientId ? styles.active : ''}`}
                    onClick={() => onSelectChat(chat)}
                >
                    {chat.recipientName}
                </div>
            ))}
        </div>
    );
};

export default NotificationSidebar;