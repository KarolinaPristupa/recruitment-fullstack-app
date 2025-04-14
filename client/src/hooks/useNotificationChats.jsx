import { useEffect, useState } from 'react';
import axios from 'axios';

const useNotificationChats = () => {
    const [chats, setChats] = useState([]);

    useEffect(() => {
        const fetch = async () => {
            const token = sessionStorage.getItem("token");
            const response = await axios.get("http://localhost:1111/api/notifications/chats", {
                headers: { Authorization: `Bearer ${token}` }
            });
            setChats(response.data);
        };

        fetch();
    }, []);

    return { chats };
};

export default useNotificationChats;
