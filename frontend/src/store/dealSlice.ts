import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import { Deal, DealPageResponse } from '../types/deal.types';

interface DealState {
  deals: Deal[];
  totalElements: number;
  totalPages: number;
  page: number;
  loading: boolean;
  error: string | null;
  selected: Deal | null;
}

const initialState: DealState = {
  deals: [],
  totalElements: 0,
  totalPages: 0,
  page: 0,
  loading: false,
  error: null,
  selected: null,
};

const dealSlice = createSlice({
  name: 'deals',
  initialState,
  reducers: {
    setDeals(state, action: PayloadAction<DealPageResponse>) {
      state.deals = action.payload.content;
      state.totalElements = action.payload.totalElements;
      state.totalPages = action.payload.totalPages;
      state.page = action.payload.page;
    },
    setSelectedDeal(state, action: PayloadAction<Deal | null>) {
      state.selected = action.payload;
    },
    setLoading(state, action: PayloadAction<boolean>) {
      state.loading = action.payload;
    },
    setError(state, action: PayloadAction<string | null>) {
      state.error = action.payload;
    },
    upsertDeal(state, action: PayloadAction<Deal>) {
      const idx = state.deals.findIndex(d => d.id === action.payload.id);
      if (idx >= 0) state.deals[idx] = action.payload;
      else state.deals.unshift(action.payload);
    },
    removeDeal(state, action: PayloadAction<number>) {
      state.deals = state.deals.filter(d => d.id !== action.payload);
    },
  },
});

export const { setDeals, setSelectedDeal, setLoading, setError, upsertDeal, removeDeal } = dealSlice.actions;
export default dealSlice.reducer;
