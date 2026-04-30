import { useEffect, useState } from 'react';
import { getCampaigns } from '../../services/emailService';
import { formatDate } from '../../utils/formatters';

interface Campaign {
  id: number;
  name: string;
  subject: string;
  status: string;
  recipientCount: number;
  sentCount: number;
  createdAt: string;
}

const STATUS_COLORS: Record<string, string> = {
  DRAFT: 'bg-gray-100 text-gray-700',
  SCHEDULED: 'bg-yellow-100 text-yellow-700',
  SENDING: 'bg-blue-100 text-blue-700',
  SENT: 'bg-green-100 text-green-700',
  FAILED: 'bg-red-100 text-red-700',
};

export default function Campaigns() {
  const [campaigns, setCampaigns] = useState<Campaign[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getCampaigns()
      .then(data => setCampaigns(data.content ?? data))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h2 className="text-2xl font-bold text-gray-800">Email Campaigns</h2>
      </div>

      {loading ? (
        <div className="text-center py-20 text-gray-400">Loading…</div>
      ) : (
        <div className="bg-white rounded-xl shadow overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 text-gray-600 uppercase text-xs">
              <tr>
                {['Name', 'Subject', 'Status', 'Recipients', 'Sent', 'Created'].map(h => (
                  <th key={h} className="px-4 py-3 text-left">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {campaigns.map(c => (
                <tr key={c.id} className="hover:bg-gray-50">
                  <td className="px-4 py-3 font-medium text-gray-800">{c.name}</td>
                  <td className="px-4 py-3 text-gray-600">{c.subject}</td>
                  <td className="px-4 py-3">
                    <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${STATUS_COLORS[c.status] ?? 'bg-gray-100 text-gray-700'}`}>
                      {c.status}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-gray-600">{c.recipientCount}</td>
                  <td className="px-4 py-3 text-gray-600">{c.sentCount}</td>
                  <td className="px-4 py-3 text-gray-500">{formatDate(c.createdAt)}</td>
                </tr>
              ))}
              {campaigns.length === 0 && (
                <tr><td colSpan={6} className="text-center py-10 text-gray-400">No campaigns yet</td></tr>
              )}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
