import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import { Lead, LeadPageResponse } from '../types/lead.types';

interface LeadState {
  leads: Lead[];
  totalElements: number;
  totalPages: number;
  page: number;
  loading: boolean;
  error: string | null;
  selected: Lead | null;
}

const initialState: LeadState = {
  leads: [],
  totalElements: 0,
  totalPages: 0,
  page: 0,
  loading: false,
  error: null,
  selected: null,
};

const leadSlice = createSlice({
  name: 'leads',
  initialState,
  reducers: {
    setLeads(state, action: PayloadAction<LeadPageResponse>) {
      state.leads = action.payload.content;
      state.totalElements = action.payload.totalElements;
      state.totalPages = action.payload.totalPages;
      state.page = action.payload.page;
    },
    setSelectedLead(state, action: PayloadAction<Lead | null>) {
      state.selected = action.payload;
    },
    setLoading(state, action: PayloadAction<boolean>) {
      state.loading = action.payload;
    },
    setError(state, action: PayloadAction<string | null>) {
      state.error = action.payload;
    },
    upsertLead(state, action: PayloadAction<Lead>) {
      const idx = state.leads.findIndex(l => l.id === action.payload.id);
      if (idx >= 0) state.leads[idx] = action.payload;
      else state.leads.unshift(action.payload);
    },
    removeLead(state, action: PayloadAction<number>) {
      state.leads = state.leads.filter(l => l.id !== action.payload);
    },
  },
});

export const { setLeads, setSelectedLead, setLoading, setError, upsertLead, removeLead } = leadSlice.actions;
export default leadSlice.reducer;
