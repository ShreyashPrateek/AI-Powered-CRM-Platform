export type DealStage = 'LEAD' | 'QUALIFIED' | 'PROPOSAL' | 'NEGOTIATION' | 'CLOSED_WON' | 'CLOSED_LOST';

export interface Deal {
  id: number;
  leadId: number;
  title: string;
  value: number;
  stage: DealStage;
  probability: number;
  expectedCloseDate?: string;
  ownerId?: number;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

export interface DealPageResponse {
  content: Deal[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface CreateDealRequest {
  leadId: number;
  title: string;
  value: number;
  expectedCloseDate?: string;
  ownerId?: number;
  probability?: number;
  notes?: string;
}
