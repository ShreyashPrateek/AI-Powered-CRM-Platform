export interface RevenueData {
  month: string;
  revenue: number;
  deals: number;
}

export interface LeadConversionData {
  status: string;
  count: number;
  percentage: number;
}

export interface SalesPerformanceData {
  userId: number;
  name: string;
  dealsWon: number;
  revenue: number;
  conversionRate: number;
}

export interface DealProbabilityData {
  stage: string;
  count: number;
  totalValue: number;
  avgProbability: number;
}

export interface AnalyticsSummary {
  totalRevenue: number;
  totalLeads: number;
  totalDeals: number;
  conversionRate: number;
  revenueByMonth: RevenueData[];
  leadsByStatus: LeadConversionData[];
  dealsByStage: DealProbabilityData[];
}
