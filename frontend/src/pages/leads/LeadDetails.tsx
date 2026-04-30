import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getLead, updateLeadStatus, deleteLead } from '../../services/leadService';
import { Lead, LeadStatus } from '../../types/lead.types';
import { LEAD_STATUS_COLORS, LEAD_STATUSES } from '../../utils/constants';
import { formatDate } from '../../utils/formatters';

export default function LeadDetails() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [lead, setLead] = useState<Lead | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getLead(Number(id))
      .then(setLead)
      .finally(() => setLoading(false));
  }, [id]);

  const handleStatusChange = async (status: LeadStatus) => {
    const updated = await updateLeadStatus(Number(id), status);
    setLead(updated);
  };

  const handleDelete = async () => {
    if (!confirm('Delete this lead?')) return;
    await deleteLead(Number(id));
    navigate('/leads');
  };

  if (loading) return <div className="text-center py-20 text-gray-400">Loading…</div>;
  if (!lead) return <div className="text-center py-20 text-red-400">Lead not found</div>;

  const fields = [
    { label: 'Email', value: lead.email },
    { label: 'Phone', value: lead.phone || '—' },
    { label: 'Company', value: lead.company || '—' },
    { label: 'Industry', value: lead.industry || '—' },
    { label: 'Source', value: lead.source },
    { label: 'Assigned User ID', value: lead.assignedUserId?.toString() || '—' },
    { label: 'Created', value: formatDate(lead.createdAt) },
    { label: 'Updated', value: formatDate(lead.updatedAt) },
  ];

  return (
    <div className="max-w-2xl space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-gray-800">{lead.name}</h2>
          <span className={`mt-1 inline-block px-2 py-0.5 rounded-full text-xs font-medium ${LEAD_STATUS_COLORS[lead.status]}`}>
            {lead.status}
          </span>
        </div>
        <button onClick={handleDelete} className="text-sm text-red-500 hover:text-red-700">Delete Lead</button>
      </div>

      <div className="bg-white rounded-xl shadow p-6 grid grid-cols-2 gap-4">
        {fields.map(({ label, value }) => (
          <div key={label}>
            <p className="text-xs text-gray-500 uppercase">{label}</p>
            <p className="text-sm font-medium text-gray-800 mt-0.5">{value}</p>
          </div>
        ))}
      </div>

      {lead.notes && (
        <div className="bg-white rounded-xl shadow p-6">
          <p className="text-xs text-gray-500 uppercase mb-1">Notes</p>
          <p className="text-sm text-gray-700">{lead.notes}</p>
        </div>
      )}

      <div className="bg-white rounded-xl shadow p-6">
        <p className="text-sm font-medium text-gray-700 mb-3">Update Status</p>
        <div className="flex gap-2 flex-wrap">
          {LEAD_STATUSES.map(s => (
            <button
              key={s}
              onClick={() => handleStatusChange(s as LeadStatus)}
              disabled={lead.status === s}
              className={`px-3 py-1.5 rounded-lg text-xs font-medium transition ${
                lead.status === s ? 'bg-gray-200 text-gray-400 cursor-not-allowed' : 'bg-blue-50 text-blue-700 hover:bg-blue-100'
              }`}
            >
              {s}
            </button>
          ))}
        </div>
      </div>
    </div>
  );
}
