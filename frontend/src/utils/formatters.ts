export const formatCurrency = (value: number): string =>
  new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD', maximumFractionDigits: 0 }).format(value);

export const formatDate = (iso: string): string =>
  new Date(iso).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });

export const formatPercent = (value: number): string => `${Math.round(value)}%`;

export const truncate = (str: string, max = 40): string =>
  str.length > max ? str.slice(0, max) + '…' : str;
