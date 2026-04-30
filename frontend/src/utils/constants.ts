export const API_BASE = 'http://localhost:8080';

export const LEAD_STATUS_COLORS: Record<string, string> = {
  NEW: 'bg-blue-100 text-blue-800',
  CONTACTED: 'bg-yellow-100 text-yellow-800',
  QUALIFIED: 'bg-green-100 text-green-800',
  LOST: 'bg-red-100 text-red-800',
};

export const DEAL_STAGE_COLORS: Record<string, string> = {
  LEAD: 'bg-gray-100 text-gray-800',
  QUALIFIED: 'bg-blue-100 text-blue-800',
  PROPOSAL: 'bg-yellow-100 text-yellow-800',
  NEGOTIATION: 'bg-orange-100 text-orange-800',
  CLOSED_WON: 'bg-green-100 text-green-800',
  CLOSED_LOST: 'bg-red-100 text-red-800',
};

export const DEAL_STAGES = ['LEAD', 'QUALIFIED', 'PROPOSAL', 'NEGOTIATION', 'CLOSED_WON', 'CLOSED_LOST'];
export const LEAD_STATUSES = ['NEW', 'CONTACTED', 'QUALIFIED', 'LOST'];
export const LEAD_SOURCES = ['WEBSITE', 'REFERRAL', 'COLD_CALL', 'EMAIL', 'SOCIAL', 'OTHER'];
export const INDUSTRIES = ['TECHNOLOGY', 'FINANCE', 'HEALTHCARE', 'REAL_ESTATE', 'MANUFACTURING', 'MEDIA', 'RETAIL', 'EDUCATION', 'OTHER'];
