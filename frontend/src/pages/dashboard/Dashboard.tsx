import { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, PieChart, Pie, Cell, Legend } from 'recharts';
import { RootState } from '../../store/store';
import { setSummary, setLoading, setError } from '../../store/analyticsSlice';
import { getSummary } from '../../services/analyticsService';
import { formatCurrency } from '../../utils/formatters';

const COLORS = ['#3b82f6', '#f59e0b', '#10b981', '#ef4444', '#8b5cf6', '#ec4899'];

export default function Dashboard() {
  const dispatch = useDispatch();
  const { summary, loading, error } = useSelector((s: RootState) => s.analytics);

  useEffect(() => {
    dispatch(setLoading(true));
    getSummary()
      .then(data => dispatch(setSummary(data)))
      .catch(() => dispatch(setError('Failed to load analytics')))
      .finally(() => dispatch(setLoading(false)));
  }, [dispatch]);

  if (loading) return <div className="flex items-center justify-center h-64 text-gray-500">Loading…</div>;
  if (error) return <div className="text-red-500 p-4">{error}</div>;
  if (!summary) return null;

  const cards = [
    { label: 'Total Revenue', value: formatCurrency(summary.totalRevenue), color: 'text-green-600' },
    { label: 'Total Leads', value: summary.totalLeads, color: 'text-blue-600' },
    { label: 'Total Deals', value: summary.totalDeals, color: 'text-purple-600' },
    { label: 'Conversion Rate', value: `${summary.conversionRate.toFixed(1)}%`, color: 'text-orange-600' },
  ];

  return (
    <div className="space-y-6">
      <h2 className="text-2xl font-bold text-gray-800">Dashboard</h2>

      {/* KPI Cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {cards.map(({ label, value, color }) => (
          <div key={label} className="bg-white rounded-xl shadow p-5">
            <p className="text-sm text-gray-500">{label}</p>
            <p className={`text-2xl font-bold mt-1 ${color}`}>{value}</p>
          </div>
        ))}
      </div>

      {/* Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white rounded-xl shadow p-5">
          <h3 className="font-semibold text-gray-700 mb-4">Revenue by Month</h3>
          <ResponsiveContainer width="100%" height={220}>
            <BarChart data={summary.revenueByMonth}>
              <XAxis dataKey="month" tick={{ fontSize: 12 }} />
              <YAxis tick={{ fontSize: 12 }} tickFormatter={v => `$${(v / 1000).toFixed(0)}k`} />
              <Tooltip formatter={(v: number) => formatCurrency(v)} />
              <Bar dataKey="revenue" fill="#3b82f6" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>

        <div className="bg-white rounded-xl shadow p-5">
          <h3 className="font-semibold text-gray-700 mb-4">Leads by Status</h3>
          <ResponsiveContainer width="100%" height={220}>
            <PieChart>
              <Pie data={summary.leadsByStatus} dataKey="count" nameKey="status" cx="50%" cy="50%" outerRadius={80} label>
                {summary.leadsByStatus.map((_, i) => (
                  <Cell key={i} fill={COLORS[i % COLORS.length]} />
                ))}
              </Pie>
              <Legend />
              <Tooltip />
            </PieChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  );
}
