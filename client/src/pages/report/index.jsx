import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import { jsPDF } from 'jspdf';
import font from './font.jsx';
import styles from './styles.module.scss';
import Toast from '@components/toast'; // Assuming the same Toast component as in CreateVacancy

const Report = () => {
    const { interviewId } = useParams();
    const navigate = useNavigate();
    const [interview, setInterview] = useState(null);
    const [formData, setFormData] = useState({
        result: '',
        reasonForRejection: '',
        technicalScore: 1,
        communicationScore: 1,
        problemSolvingScore: 1,
        culturalFitScore: 1,
        technicalFeedback: '',
        communicationFeedback: '',
        problemSolvingFeedback: '',
        culturalFitFeedback: '',
        strengths: '',
        weaknesses: '',
        recommendation: 'Не определено',
        additionalNotes: ''
    });
    const [toast, setToast] = useState({ show: false, message: '', type: '' });
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchInterview = async () => {
            try {
                const response = await axios.get(`http://localhost:1111/api/interviews`, {
                    headers: {
                        Authorization: `Bearer ${sessionStorage.getItem('token')}`
                    }
                });
                const interviewData = response.data.find(i => i.id === parseInt(interviewId));
                if (!interviewData) {
                    throw new Error('Собеседование не найдено');
                }
                setInterview(interviewData);
                setFormData(prev => ({
                    ...prev,
                    result: interviewData.result && ['Принят', 'Отклонён'].includes(interviewData.result) ? interviewData.result : ''
                }));
                setLoading(false);
            } catch (err) {
                setToast({ show: true, message: err.message || 'Ошибка загрузки данных собеседования', type: 'error' });
                setLoading(false);
            }
        };
        fetchInterview();
    }, [interviewId]);

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleGenerateReport = async () => {
        if (!formData.result) {
            setToast({ show: true, message: 'Поле "Результат" обязательно для заполнения', type: 'error' });
            return;
        }
        if (formData.result === 'Отклонён' && !formData.reasonForRejection.trim()) {
            setToast({ show: true, message: 'Пожалуйста, укажите причину отказа', type: 'error' });
            return;
        }

        try {
            await axios.put(`http://localhost:1111/api/interviews/${interviewId}/result`, {
                result: formData.result
            }, {
                headers: {
                    Authorization: `Bearer ${sessionStorage.getItem('token')}`
                }
            });

            const doc = new jsPDF();
            doc.addFileToVFS('Times New Roman-normal.ttf', font);
            doc.addFont('Times New Roman-normal.ttf', 'Times New Roman', 'normal');
            doc.setFont('Times New Roman');
            doc.setFontSize(14);

            doc.text('ОТЧЕТ ПО СОБЕСЕДОВАНИЮ', 105, 20, { align: 'center' });
            doc.setFontSize(12);
            doc.text(`Номер документа: INT-${interview.id}`, 105, 30, { align: 'center' });
            doc.text(`Дата составления: ${new Date().toLocaleDateString('ru-RU')}`, 105, 38, { align: 'center' });

            doc.setFontSize(14);
            doc.text('1. Общая информация', 20, 50);
            doc.setLineWidth(0.5);
            doc.line(20, 52, 190, 52);
            doc.text(`ID Собеседования: ${interview.id}`, 20, 62);
            doc.text(`Кандидат: ${interview.candidateFirstName} ${interview.candidateLastName}`, 20, 70);
            doc.text(`Позиция: ${interview.position}`, 20, 78);
            doc.text(`HR: ${interview.hrFirstName} ${interview.hrLastName}`, 20, 86);
            doc.text(`Дата собеседования: ${new Date(interview.date).toLocaleString('ru-RU')}`, 20, 94);
            doc.text(`Результат: ${formData.result}`, 20, 102);

            let yOffset = 110;
            if (formData.result === 'Отклонён') {
                doc.text('Причина отказа:', 20, yOffset);
                doc.text(formData.reasonForRejection || 'Не указана', 30, yOffset + 8, { maxWidth: 160 });
                yOffset += 20;
            }

            doc.text('2. Оценки кандидата', 20, yOffset);
            doc.line(20, yOffset + 2, 190, yOffset + 2);
            yOffset += 10;
            doc.text(`Технические навыки: ${formData.technicalScore}/5`, 20, yOffset);
            doc.text(formData.technicalFeedback || 'Нет комментариев', 30, yOffset + 8, { maxWidth: 160 });
            yOffset += 20;
            doc.text(`Коммуникация: ${formData.communicationScore}/5`, 20, yOffset);
            doc.text(formData.communicationFeedback || 'Нет комментариев', 30, yOffset + 8, { maxWidth: 160 });
            yOffset += 20;
            doc.text(`Решение задач: ${formData.problemSolvingScore}/5`, 20, yOffset);
            doc.text(formData.problemSolvingFeedback || 'Нет комментариев', 30, yOffset + 8, { maxWidth: 160 });
            yOffset += 20;
            doc.text(`Культурное соответствие: ${formData.culturalFitScore}/5`, 20, yOffset);
            doc.text(formData.culturalFitFeedback || 'Нет комментариев', 30, yOffset + 8, { maxWidth: 160 });
            yOffset += 20;

            doc.text('3. Характеристика кандидата', 20, yOffset);
            doc.line(20, yOffset + 2, 190, yOffset + 2);
            yOffset += 10;
            doc.text('Сильные стороны:', 20, yOffset);
            doc.text(formData.strengths || 'Не указаны', 30, yOffset + 8, { maxWidth: 160 });
            yOffset += 20;
            doc.text('Слабые стороны:', 20, yOffset);
            doc.text(formData.weaknesses || 'Не указаны', 30, yOffset + 8, { maxWidth: 160 });
            yOffset += 20;

            doc.text('4. Рекомендация и заметки', 20, yOffset);
            doc.line(20, yOffset + 2, 190, yOffset + 2);
            yOffset += 10;
            doc.text(`Рекомендация: ${formData.recommendation}`, 20, yOffset);
            yOffset += 12;
            doc.text('Дополнительные заметки:', 20, yOffset);
            doc.text(formData.additionalNotes || 'Не указаны', 30, yOffset + 8, { maxWidth: 160 });
            yOffset += 20;

            if (yOffset > 260) {
                doc.addPage();
                yOffset = 20;
            }

            const hrInitials = `${interview.hrFirstName.charAt(0)}.${interview.hrLastName.charAt(0)}.`;
            doc.text('5. Подтверждение', 20, yOffset);
            doc.line(20, yOffset + 2, 190, yOffset + 2);
            yOffset += 10;
            doc.text(`Составил: ${interview.hrFirstName} ${interview.hrLastName} (${hrInitials})`, 20, yOffset);
            yOffset += 12;
            doc.text('Подпись: _____________________________', 20, yOffset);

            doc.save(`report_interview_${interviewId}.pdf`);
            setToast({ show: true, message: 'Отчет успешно создан', type: 'success' });
        } catch (err) {
            const errorMessage = err.response?.status === 403
                ? 'У вас нет прав для обновления результата собеседования.'
                : err.response?.data || err.message;
            setToast({ show: true, message: `Ошибка при создании отчета: ${errorMessage}`, type: 'error' });
        }
    };

    if (loading) return <div className={styles.container}>Загрузка...</div>;
    if (!interview) return <div className={styles.container}>Собеседование не найдено</div>;

    return (
        <div className={styles.container}>
            <h1>Создание отчета по собеседованию</h1>
            <form className={styles.form}>
                <div className={styles.formGroup}>
                    <label>ID Собеседования</label>
                    <input type="text" value={interview.id} readOnly />
                </div>
                <div className={styles.formGroup}>
                    <label>Кандидат</label>
                    <input
                        type="text"
                        value={`${interview.candidateFirstName} ${interview.candidateLastName}`}
                        readOnly
                    />
                </div>
                <div className={styles.formGroup}>
                    <label>Позиция</label>
                    <input type="text" value={interview.position} readOnly />
                </div>
                <div className={styles.formGroup}>
                    <label>HR</label>
                    <input
                        type="text"
                        value={`${interview.hrFirstName} ${interview.hrLastName}`}
                        readOnly
                    />
                </div>
                <div className={styles.formGroup}>
                    <label>Дата собеседования</label>
                    <input
                        type="text"
                        value={new Date(interview.date).toLocaleString('ru-RU')}
                        readOnly
                    />
                </div>
                <div className={styles.formGroup}>
                    <label>Результат *</label>
                    <select
                        name="result"
                        value={formData.result}
                        onChange={handleInputChange}
                        required
                    >
                        <option value="" disabled>Выберите результат</option>
                        <option value="Принят">Принят</option>
                        <option value="Отклонён">Отклонён</option>
                    </select>
                </div>
                {formData.result === 'Отклонён' && (
                    <div className={styles.formGroup}>
                        <label>Причина отказа *</label>
                        <textarea
                            name="reasonForRejection"
                            value={formData.reasonForRejection}
                            onChange={handleInputChange}
                            rows="4"
                            placeholder="Укажите причину отказа"
                            required
                        />
                    </div>
                )}
                <div className={styles.formGroup}>
                    <label>Технические навыки (1-5)</label>
                    <input
                        type="number"
                        name="technicalScore"
                        value={formData.technicalScore}
                        onChange={handleInputChange}
                        min="1"
                        max="5"
                    />
                    <textarea
                        name="technicalFeedback"
                        value={formData.technicalFeedback}
                        onChange={handleInputChange}
                        rows="3"
                        placeholder="Комментарии по техническим навыкам"
                    />
                </div>
                <div className={styles.formGroup}>
                    <label>Коммуникация (1-5)</label>
                    <input
                        type="number"
                        name="communicationScore"
                        value={formData.communicationScore}
                        onChange={handleInputChange}
                        min="1"
                        max="5"
                    />
                    <textarea
                        name="communicationFeedback"
                        value={formData.communicationFeedback}
                        onChange={handleInputChange}
                        rows="3"
                        placeholder="Комментарии по коммуникации"
                    />
                </div>
                <div className={styles.formGroup}>
                    <label>Решение задач (1-5)</label>
                    <input
                        type="number"
                        name="problemSolvingScore"
                        value={formData.problemSolvingScore}
                        onChange={handleInputChange}
                        min="1"
                        max="5"
                    />
                    <textarea
                        name="problemSolvingFeedback"
                        value={formData.problemSolvingFeedback}
                        onChange={handleInputChange}
                        rows="3"
                        placeholder="Комментарии по решению задач"
                    />
                </div>
                <div className={styles.formGroup}>
                    <label>Культурное соответствие (1-5)</label>
                    <input
                        type="number"
                        name="culturalFitScore"
                        value={formData.culturalFitScore}
                        onChange={handleInputChange}
                        min="1"
                        max="5"
                    />
                    <textarea
                        name="culturalFitFeedback"
                        value={formData.culturalFitFeedback}
                        onChange={handleInputChange}
                        rows="3"
                        placeholder="Комментарии по культурному соответствию"
                    />
                </div>
                <div className={styles.formGroup}>
                    <label>Сильные стороны</label>
                    <textarea
                        name="strengths"
                        value={formData.strengths}
                        onChange={handleInputChange}
                        rows="4"
                        placeholder="Опишите сильные стороны кандидата"
                    />
                </div>
                <div className={styles.formGroup}>
                    <label>Слабые стороны</label>
                    <textarea
                        name="weaknesses"
                        value={formData.weaknesses}
                        onChange={handleInputChange}
                        rows="4"
                        placeholder="Опишите слабые стороны кандидата"
                    />
                </div>
                <div className={styles.formGroup}>
                    <label>Рекомендация</label>
                    <select
                        name="recommendation"
                        value={formData.recommendation}
                        onChange={handleInputChange}
                    >
                        <option value="Принять">Принять</option>
                        <option value="Отклонить">Отклонить</option>
                        <option value="Дальнейшее рассмотрение">Дальнейшее рассмотрение</option>
                        <option value="Не определено">Не определено</option>
                    </select>
                </div>
                <div className={styles.formGroup}>
                    <label>Дополнительные заметки</label>
                    <textarea
                        name="additionalNotes"
                        value={formData.additionalNotes}
                        onChange={handleInputChange}
                        rows="4"
                        placeholder="Введите дополнительные заметки"
                    />
                </div>
                <div className={styles.formActions}>
                    <button type="button" onClick={() => navigate(-1)}>
                        Назад
                    </button>
                    <button type="button" onClick={handleGenerateReport}>
                        Создать отчет
                    </button>
                </div>
            </form>
            {toast.show && (
                <Toast
                    message={toast.message}
                    type={toast.type}
                    onClose={() => setToast({ show: false, message: '', type: '' })}
                />
            )}
        </div>
    );
};

export default Report;