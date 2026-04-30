import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Link } from 'react-router-dom';
import { RootState } from '../../store/store';
import { setLeads, setLoading, setError, removeLead } from '../../store/leadSlice';
import { getLeads, deleteLead, searchLeads } from '../../services/leadService';
import { useDebounce } from '../../hooks/useDebounce';
import { LEAD_STATUS_COLORS, LEAD_STATUSES } from '../../utils/constants';
import { formatDate } from '../../utils/formatters';
import { LeadStatus } from '../../types/lead.types';

export default function LeadsList() {
  const dispatch = useDispatch();
  const { leads, totalPages, page, loading } = useSelector((s: RootState) => s.leads);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState<LeadStatus | ''>('');
  const debouncedSearch = useDebounce(search);

  const load = (p = 0) => {
    dispatch(setLoading(true));
    getLeads(p, 20, statusFilter || undefined)
      .then(data => dispatch(setLeads(data)))
      .catch(() => dispatch(setError('Failed to load leads')))
      .finally(() => dispatch(setLoading(false)));
  };

  useEffect(() => {
    if (debouncedSearch.trim()) {
      searchLeads(debouncedSearch).then(results => dispatch(setLeads({ content: results, page: 0, size: results.length, totalElements: results.length, totalPages: 1 })));
    } else {
      load(0);
    }
  }, [debouncedSearch, statusFilter]);

  const handleDelete = async (id: number) => {
    if (!confirm('Delete this lead?')) return;
    await deleteLead(id);
    dispatch(removeLead(id));
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h2 className="text-2xl font-bold text-gray-800">Leads</h2>
        <Link to="/leads/new" className="bg-blue-600 text-white px-4 py-2 rounded-lg text-sm hover:bg-blue-700">
          + New Lead
        </Link>
      </div>

      <div className="flex gap-3">
        <input
          value={search}
          onChange={e => setSearch(e.target.value)}
          placeholder="Search leads…"
          className="flex-1 px-4 py-2 border rounded-lg text-sm outline-none focus:ring-2 focus:ring-blue-500"
        />
        <select
          value={statusFilter}
          onChange={e => setStatusFilter(e.target.value as LeadStatus | '')}
          className="px-3 py-2 border rounded-lg text-sm outline-none"
        >
          <option value="">All Statuses</option>
          {LEAD_STATUSES.map(s => <option key={s} value={s}>{s}</option>)}
        </select>
      </div>

      {loading ? (
        <div className="text-center py-12 text-gray-400">Loading…</div>
      ) : (
        <div className="bg-white rounded-xl shadow overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 text-gray-600 uppercase text-xs">
              <tr>
                {['Name', 'Email', 'Company', 'Status', 'Source', 'Created', ''].map(h => (
                  <th key={h} className="px-4 py-3 text-left">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {leads.map(lead => (
                <tr key={lead.id} className="hover:bg-gray-50">
                  <td className="px-4 py-3 font-medium">
                    <Link to={`/leads/${lead.id}`} className="text-blue-600 hover:underline">{lead.name}</Link>
                  </td>
                  <td className="px-4 py-3 text-gray-600">{lead.email}</td>
                  <td className="px-4 py-3 text-gray-600">{lead.company || '—'}</td>
                  <td className="px-4 py-3">
                    <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${LEAD_STATUS_COLORS[lead.status]}`}>
                      {lead.status}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-gray-600">{lead.source}</td>
                  <td className="px-4 py-3 text-gray-500">{formatDate(lead.createdAt)}</td>
                  <td className="px-4 py-3">
                    <button onClick={() => handleDelete(lead.id)} className="text-red-400 hover:text-red-600 text-xs">Delete</button>
                  </td>
                </tr>
              ))}
              {leads.length === 0 && (
                <tr><td colSpan={7} className="text-center py-10 text-gray-400">No leads found</td></tr>
              )}
            </tbody>
          </table>
        </div>
      )}

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="flex justify-center gap-2">
          {Array.from({ length: totalPages }, (_, i) => (
            <button
              key={i}
              onClick={() => load(i)}
              className={`px-3 py-1 rounded text-sm ${i === page ? 'bg-blue-600 text-white' : 'bg-white border hover:bg-gray-50'}`}
            >
              {i + 1}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
