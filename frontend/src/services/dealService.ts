import api from './api';
import { Deal, DealPageResponse, CreateDealRequest, DealStage } from '../types/deal.types';

export const getDeals = (page = 0, size = 20, stage?: DealStage): Promise<DealPageResponse> =>
  api.get('/api/deals', { params: { page, size, stage } }).then(r => r.data);

export const getDeal = (id: number): Promise<Deal> =>
  api.get(`/api/deals/${id}`).then(r => r.data);

export const createDeal = (data: CreateDealRequest): Promise<Deal> =>
  api.post('/api/deals', data).then(r => r.data);

export const updateDeal = (id: number, data: Partial<CreateDealRequest>): Promise<Deal> =>
  api.patch(`/api/deals/${id}`, data).then(r => r.data);

export const updateDealStage = (id: number, stage: DealStage): Promise<Deal> =>
  api.patch(`/api/deals/${id}/stage`, { stage }).then(r => r.data);

export const deleteDeal = (id: number): Promise<void> =>
  api.delete(`/api/deals/${id}`).then(() => undefined);
