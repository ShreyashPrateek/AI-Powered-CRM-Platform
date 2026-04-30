export type LeadStatus = 'NEW' | 'CONTACTED' | 'QUALIFIED' | 'LOST';
export type LeadSource = 'WEBSITE' | 'REFERRAL' | 'COLD_CALL' | 'EMAIL' | 'SOCIAL' | 'OTHER';
export type Industry = 'TECHNOLOGY' | 'FINANCE' | 'HEALTHCARE' | 'REAL_ESTATE' | 'MANUFACTURING' | 'MEDIA' | 'RETAIL' | 'EDUCATION' | 'OTHER';

export interface Lead {
  id: number;
  name: string;
  email: string;
  phone?: string;
  company?: string;
  industry?: Industry;
  source: LeadSource;
  status: LeadStatus;
  assignedUserId?: number;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

export interface LeadPageResponse {
  content: Lead[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface CreateLeadRequest {
  name: string;
  email: string;
  phone?: string;
  company?: string;
  industry?: Industry;
  source: LeadSource;
  assignedUserId?: number;
  notes?: string;
}
