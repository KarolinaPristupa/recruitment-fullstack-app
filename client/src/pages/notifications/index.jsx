import React from 'react';
import NotificationSidebar from '@components/notification-sidebar';
import NotificationChat from '@components/notification-chat';
import styles from '@pages/notifications/styles.module.scss';

const Notifications = () => {
    const [selectedChat, setSelectedChat] = React.useState(null);

    return (
        <div className={styles.notificationsWrapper}>
            <NotificationSidebar onSelectChat={setSelectedChat} selectedChat={selectedChat} />
            <NotificationChat selectedChat={selectedChat} />
        </div>
    );
};

export default Notifications;
