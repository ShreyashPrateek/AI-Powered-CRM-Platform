import api from './api';

export const generateAiReply = (data: { recipientEmail: string; recipientName?: string; context: string; tone?: string }) =>
  api.post('/api/emails/ai-reply', data).then(r => r.data);

export const getCampaigns = (page = 0, size = 20) =>
  api.get('/api/emails/campaigns', { params: { page, size } }).then(r => r.data);

export const createCampaign = (data: { name: string; subject: string; templateId: number; recipientEmails: string[] }) =>
  api.post('/api/emails/campaigns', data).then(r => r.data);

export const getReminders = (page = 0, size = 20) =>
  api.get('/api/emails/reminders', { params: { page, size } }).then(r => r.data);
