import { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { RootState } from '../../store/store';
import { setDeals, setLoading, upsertDeal } from '../../store/dealSlice';
import { getDeals, updateDealStage } from '../../services/dealService';
import { Deal, DealStage } from '../../types/deal.types';
import { DEAL_STAGES } from '../../utils/constants';
import { formatCurrency } from '../../utils/formatters';

export default function Pipeline() {
  const dispatch = useDispatch();
  const { deals, loading } = useSelector((s: RootState) => s.deals);

  useEffect(() => {
    dispatch(setLoading(true));
    getDeals(0, 100)
      .then(data => dispatch(setDeals(data)))
      .finally(() => dispatch(setLoading(false)));
  }, [dispatch]);

  const handleStageChange = async (deal: Deal, stage: DealStage) => {
    const updated = await updateDealStage(deal.id, stage);
    dispatch(upsertDeal(updated));
  };

  const byStage = (stage: string) => deals.filter(d => d.stage === stage);

  const stageTotal = (stage: string) =>
    byStage(stage).reduce((sum, d) => sum + d.value, 0);

  if (loading) return <div className="text-center py-20 text-gray-400">Loading…</div>;

  return (
    <div className="space-y-4">
      <h2 className="text-2xl font-bold text-gray-800">Sales Pipeline</h2>
      <div className="flex gap-4 overflow-x-auto pb-4">
        {DEAL_STAGES.map(stage => (
          <div key={stage} className="min-w-[220px] flex-shrink-0">
            <div className="flex items-center justify-between mb-2">
              <span className="text-xs font-semibold text-gray-600 uppercase">{stage.replace('_', ' ')}</span>
              <span className="text-xs text-gray-400">{formatCurrency(stageTotal(stage))}</span>
            </div>
            <div className="space-y-2">
              {byStage(stage).map(deal => (
                <div key={deal.id} className="bg-white rounded-lg shadow p-3 text-sm">
                  <p className="font-medium text-gray-800 truncate">{deal.title}</p>
                  <p className="text-green-600 font-semibold mt-1">{formatCurrency(deal.value)}</p>
                  <p className="text-gray-400 text-xs mt-1">{deal.probability}% probability</p>
                  <select
                    value={deal.stage}
                    onChange={e => handleStageChange(deal, e.target.value as DealStage)}
                    className="mt-2 w-full text-xs border rounded px-1 py-1 outline-none"
                  >
                    {DEAL_STAGES.map(s => <option key={s} value={s}>{s.replace('_', ' ')}</option>)}
                  </select>
                </div>
              ))}
              {byStage(stage).length === 0 && (
                <div className="text-center py-6 text-gray-300 text-xs border-2 border-dashed rounded-lg">Empty</div>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
