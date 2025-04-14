import React from 'react';
import useNotificationChats from '@hooks/useNotificationChats';
import styles from '@pages/notifications/styles.module.scss';

const NotificationSidebar = ({ onSelectChat, selectedChat }) => {
    const { chats } = useNotificationChats();

    return (
        <div className={styles.sidebar}>
            {chats.map(chat => (
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
