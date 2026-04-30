import api from './api';
import { AnalyticsSummary } from '../types/analytics.types';

export const getSummary = (): Promise<AnalyticsSummary> =>
  api.get('/api/analytics/summary').then(r => r.data);

export const getRevenue = () =>
  api.get('/api/analytics/revenue').then(r => r.data);

export const getLeadConversion = () =>
  api.get('/api/analytics/leads/conversion').then(r => r.data);

export const getSalesPerformance = () =>
  api.get('/api/analytics/sales/performance').then(r => r.data);

export const getDealProbability = () =>
  api.get('/api/analytics/deals/probability').then(r => r.data);
