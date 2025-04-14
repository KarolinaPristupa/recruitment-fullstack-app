import React, { useEffect, useState } from "react";
import styles from "./styles.module.scss";

const Toast = ({ message, type, onClose }) => {
    const [visible, setVisible] = useState(false);

    useEffect(() => {
        setVisible(true);
        const timer = setTimeout(() => {
            setVisible(false);
            setTimeout(onClose, 300); // Даем время на анимацию скрытия
        }, 3000);

        return () => clearTimeout(timer);
    }, []);

    return (
        <div className={`${styles.toast} ${visible ? styles.show : styles.hide} ${styles[type]}`}>
            {message}
        </div>
    );
};

export default Toast;
