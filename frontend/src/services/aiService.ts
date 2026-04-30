import api from './api';

export const scoreLeadAi = (data: {
  industry: string;
  companySize: string;
  engagementScore: number;
  pastInteractions: number;
  additionalContext?: string;
}) => api.post('/api/ai/lead-scoring/score', data).then(r => r.data);

export const analyseSentiment = (text: string) =>
  api.post('/api/ai/sentiment/analyse', { text }).then(r => r.data);

export const chatWithAssistant = (question: string, conversationHistory: { role: string; content: string }[] = []) =>
  api.post('/api/ai/chatbot/chat', { question, conversationHistory }).then(r => r.data);

export const ingestDeals = (deals: object[]) =>
  api.post('/api/ai/chatbot/ingest/deals', { deals }).then(r => r.data);
