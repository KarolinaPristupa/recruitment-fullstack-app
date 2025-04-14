import axios from 'axios';

const api = axios.create({
    baseURL: 'http://localhost:1111/api',
    headers: {
        'Content-Type': 'application/json',
    },
    timeout: 5000,
});

// Динамическое добавление токена в заголовки
api.interceptors.request.use((config) => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
}, (error) => {
    console.error('Request Interceptor Error:', error);
    return Promise.reject(error);
});

// Обработка ошибок
const handleError = (error) => {
    if (error.response) {
        console.error('Server Error:', error.response.data);
        console.error('Status Code:', error.response.status);
    } else if (error.request) {
        console.error('No Response Received:', error.request);
    } else {
        console.error('Request Setup Error:', error.message);
    }
    throw error;
};

// Интерцепторы
api.interceptors.request.use(
    (config) => {
        console.log('Request Interceptor:', config);
        return config;
    },
    (error) => {
        console.error('Request Interceptor Error:', error);
        return Promise.reject(error);
    }
);

api.interceptors.response.use(
    (response) => {
        console.log('Response Interceptor:', response);
        return response;
    },
    (error) => {
        console.error('Response Interceptor Error:', error);
        return Promise.reject(error);
    }
);
