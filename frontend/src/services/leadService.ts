import api from './api';
import { Lead, LeadPageResponse, CreateLeadRequest, LeadStatus } from '../types/lead.types';

export const getLeads = (page = 0, size = 20, status?: LeadStatus): Promise<LeadPageResponse> =>
  api.get('/api/leads', { params: { page, size, status } }).then(r => r.data);

export const getLead = (id: number): Promise<Lead> =>
  api.get(`/api/leads/${id}`).then(r => r.data);

export const searchLeads = (q: string): Promise<Lead[]> =>
  api.get('/api/leads/search', { params: { q } }).then(r => r.data);

export const createLead = (data: CreateLeadRequest): Promise<Lead> =>
  api.post('/api/leads', data).then(r => r.data);

export const updateLead = (id: number, data: Partial<CreateLeadRequest>): Promise<Lead> =>
  api.patch(`/api/leads/${id}`, data).then(r => r.data);

export const updateLeadStatus = (id: number, status: LeadStatus): Promise<Lead> =>
  api.patch(`/api/leads/${id}/status`, { status }).then(r => r.data);

export const assignLead = (id: number, assignedUserId: number): Promise<Lead> =>
  api.patch(`/api/leads/${id}/assign`, { assignedUserId }).then(r => r.data);

export const deleteLead = (id: number): Promise<void> =>
  api.delete(`/api/leads/${id}`).then(() => undefined);
