import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import { AnalyticsSummary } from '../types/analytics.types';

interface AnalyticsState {
  summary: AnalyticsSummary | null;
  loading: boolean;
  error: string | null;
}

const initialState: AnalyticsState = {
  summary: null,
  loading: false,
  error: null,
};

const analyticsSlice = createSlice({
  name: 'analytics',
  initialState,
  reducers: {
    setSummary(state, action: PayloadAction<AnalyticsSummary>) {
      state.summary = action.payload;
    },
    setLoading(state, action: PayloadAction<boolean>) {
      state.loading = action.payload;
    },
    setError(state, action: PayloadAction<string | null>) {
      state.error = action.payload;
    },
  },
});

export const { setSummary, setLoading, setError } = analyticsSlice.actions;
export default analyticsSlice.reducer;
