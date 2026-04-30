import { useState, useRef, useEffect } from 'react';
import { chatWithAssistant } from '../../services/aiService';

interface Message {
  role: 'user' | 'assistant';
  content: string;
}

export default function AiAssistant() {
  const [messages, setMessages] = useState<Message[]>([
    { role: 'assistant', content: 'Hi! I\'m your AI CRM assistant. Ask me anything about your deals, leads, or sales pipeline.' },
  ]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const bottomRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const send = async () => {
    const question = input.trim();
    if (!question || loading) return;
    setInput('');
    const history = messages.map(m => ({ role: m.role, content: m.content }));
    setMessages(prev => [...prev, { role: 'user', content: question }]);
    setLoading(true);
    try {
      const data = await chatWithAssistant(question, history);
      setMessages(prev => [...prev, { role: 'assistant', content: data.answer }]);
    } catch {
      setMessages(prev => [...prev, { role: 'assistant', content: 'Sorry, I could not reach the AI service. Please try again.' }]);
    } finally {
      setLoading(false);
    }
  };

  const handleKey = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); send(); }
  };

  return (
    <div className="flex flex-col h-[calc(100vh-6rem)] max-w-2xl">
      <h2 className="text-2xl font-bold text-gray-800 mb-4">AI CRM Assistant</h2>

      <div className="flex-1 overflow-y-auto bg-white rounded-xl shadow p-4 space-y-4">
        {messages.map((m, i) => (
          <div key={i} className={`flex ${m.role === 'user' ? 'justify-end' : 'justify-start'}`}>
            <div className={`max-w-[80%] px-4 py-2.5 rounded-2xl text-sm whitespace-pre-wrap ${
              m.role === 'user'
                ? 'bg-blue-600 text-white rounded-br-sm'
                : 'bg-gray-100 text-gray-800 rounded-bl-sm'
            }`}>
              {m.content}
            </div>
          </div>
        ))}
        {loading && (
          <div className="flex justify-start">
            <div className="bg-gray-100 text-gray-500 px-4 py-2.5 rounded-2xl rounded-bl-sm text-sm">
              ✨ Thinking…
            </div>
          </div>
        )}
        <div ref={bottomRef} />
      </div>

      <div className="mt-3 flex gap-2">
        <textarea
          value={input}
          onChange={e => setInput(e.target.value)}
          onKeyDown={handleKey}
          rows={2}
          placeholder="Ask about your deals, leads, pipeline… (Enter to send)"
          className="flex-1 px-4 py-2 border rounded-xl text-sm outline-none focus:ring-2 focus:ring-blue-500 resize-none"
        />
        <button
          onClick={send}
          disabled={loading || !input.trim()}
          className="px-5 bg-blue-600 text-white rounded-xl hover:bg-blue-700 disabled:opacity-50 transition text-sm"
        >
          Send
        </button>
      </div>
    </div>
  );
}
