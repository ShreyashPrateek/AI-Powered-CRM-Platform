import { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import {
  BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer,
  LineChart, Line, CartesianGrid, Legend,
} from 'recharts';
import { RootState } from '../../store/store';
import { setSummary, setLoading } from '../../store/analyticsSlice';
import { getSummary } from '../../services/analyticsService';
import { formatCurrency, formatPercent } from '../../utils/formatters';

export default function Analytics() {
  const dispatch = useDispatch();
  const { summary, loading } = useSelector((s: RootState) => s.analytics);

  useEffect(() => {
    dispatch(setLoading(true));
    getSummary()
      .then(data => dispatch(setSummary(data)))
      .finally(() => dispatch(setLoading(false)));
  }, [dispatch]);

  if (loading) return <div className="text-center py-20 text-gray-400">Loading…</div>;
  if (!summary) return null;

  return (
    <div className="space-y-6">
      <h2 className="text-2xl font-bold text-gray-800">Analytics</h2>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Revenue trend */}
        <div className="bg-white rounded-xl shadow p-5">
          <h3 className="font-semibold text-gray-700 mb-4">Monthly Revenue</h3>
          <ResponsiveContainer width="100%" height={240}>
            <LineChart data={summary.revenueByMonth}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="month" tick={{ fontSize: 11 }} />
              <YAxis tick={{ fontSize: 11 }} tickFormatter={v => `$${(v / 1000).toFixed(0)}k`} />
              <Tooltip formatter={(v: number) => formatCurrency(v)} />
              <Legend />
              <Line type="monotone" dataKey="revenue" stroke="#3b82f6" strokeWidth={2} dot={false} />
            </LineChart>
          </ResponsiveContainer>
        </div>

        {/* Deals by stage */}
        <div className="bg-white rounded-xl shadow p-5">
          <h3 className="font-semibold text-gray-700 mb-4">Deals by Stage</h3>
          <ResponsiveContainer width="100%" height={240}>
            <BarChart data={summary.dealsByStage} layout="vertical">
              <XAxis type="number" tick={{ fontSize: 11 }} />
              <YAxis dataKey="stage" type="category" tick={{ fontSize: 11 }} width={90} />
              <Tooltip formatter={(v: number) => formatCurrency(v)} />
              <Bar dataKey="totalValue" fill="#10b981" radius={[0, 4, 4, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>

        {/* Lead conversion */}
        <div className="bg-white rounded-xl shadow p-5">
          <h3 className="font-semibold text-gray-700 mb-4">Lead Conversion</h3>
          <div className="space-y-3">
            {summary.leadsByStatus.map(item => (
              <div key={item.status}>
                <div className="flex justify-between text-sm mb-1">
                  <span className="text-gray-600">{item.status}</span>
                  <span className="font-medium">{item.count} ({formatPercent(item.percentage)})</span>
                </div>
                <div className="w-full bg-gray-100 rounded-full h-2">
                  <div className="bg-blue-500 h-2 rounded-full" style={{ width: `${item.percentage}%` }} />
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Deal probability */}
        <div className="bg-white rounded-xl shadow p-5">
          <h3 className="font-semibold text-gray-700 mb-4">Avg. Win Probability by Stage</h3>
          <ResponsiveContainer width="100%" height={240}>
            <BarChart data={summary.dealsByStage}>
              <XAxis dataKey="stage" tick={{ fontSize: 10 }} />
              <YAxis tick={{ fontSize: 11 }} unit="%" />
              <Tooltip formatter={(v: number) => `${v}%`} />
              <Bar dataKey="avgProbability" fill="#8b5cf6" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  );
}
