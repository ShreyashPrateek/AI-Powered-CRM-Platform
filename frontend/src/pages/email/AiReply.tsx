import { useState } from 'react';
import { generateAiReply } from '../../services/emailService';

const TONES = ['professional', 'friendly', 'assertive', 'empathetic'];

export default function AiReply() {
  const [form, setForm] = useState({ recipientEmail: '', recipientName: '', context: '', tone: 'professional' });
  const [result, setResult] = useState<{ subject: string; body: string; modelUsed: string } | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setResult(null);
    try {
      setLoading(true);
      const data = await generateAiReply(form);
      setResult(data);
    } catch {
      setError('Failed to generate reply. Make sure the AI service is running.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-2xl space-y-6">
      <h2 className="text-2xl font-bold text-gray-800">AI Email Reply Generator</h2>

      <form onSubmit={handleSubmit} className="bg-white rounded-xl shadow p-6 space-y-4">
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium mb-1">Recipient Email</label>
            <input
              type="email" required
              value={form.recipientEmail}
              onChange={e => setForm({ ...form, recipientEmail: e.target.value })}
              className="w-full px-3 py-2 border rounded-lg text-sm outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="customer@company.com"
            />
          </div>
          <div>
            <label className="block text-sm font-medium mb-1">Recipient Name</label>
            <input
              type="text"
              value={form.recipientName}
              onChange={e => setForm({ ...form, recipientName: e.target.value })}
              className="w-full px-3 py-2 border rounded-lg text-sm outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="John Doe"
            />
          </div>
        </div>

        <div>
          <label className="block text-sm font-medium mb-1">Tone</label>
          <div className="flex gap-2">
            {TONES.map(t => (
              <button
                key={t} type="button"
                onClick={() => setForm({ ...form, tone: t })}
                className={`px-3 py-1.5 rounded-lg text-xs capitalize transition ${
                  form.tone === t ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                }`}
              >
                {t}
              </button>
            ))}
          </div>
        </div>

        <div>
          <label className="block text-sm font-medium mb-1">Customer Email / Context</label>
          <textarea
            required rows={5}
            value={form.context}
            onChange={e => setForm({ ...form, context: e.target.value })}
            className="w-full px-3 py-2 border rounded-lg text-sm outline-none focus:ring-2 focus:ring-blue-500 resize-none"
            placeholder="Paste the customer's email or describe the context…"
          />
        </div>

        {error && <p className="text-red-500 text-sm">{error}</p>}

        <button
          type="submit" disabled={loading}
          className="w-full bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700 transition text-sm"
        >
          {loading ? '✨ Generating…' : '✨ Generate Reply'}
        </button>
      </form>

      {result && (
        <div className="bg-white rounded-xl shadow p-6 space-y-3">
          <div>
            <p className="text-xs text-gray-500 uppercase">Subject</p>
            <p className="font-medium text-gray-800 mt-0.5">{result.subject}</p>
          </div>
          <div>
            <p className="text-xs text-gray-500 uppercase">Body</p>
            <pre className="mt-1 text-sm text-gray-700 whitespace-pre-wrap font-sans">{result.body}</pre>
          </div>
          <p className="text-xs text-gray-400">Model: {result.modelUsed}</p>
        </div>
      )}
    </div>
  );
}
