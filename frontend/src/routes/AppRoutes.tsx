import { Navigate, Route, Routes } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { RootState } from '../store/store';
import MainLayout from '../layouts/MainLayout';
import Login from '../pages/auth/Login';
import Register from '../pages/auth/Register';
import Dashboard from '../pages/dashboard/Dashboard';
import LeadsList from '../pages/leads/LeadsList';
import LeadDetails from '../pages/leads/LeadDetails';
import Pipeline from '../pages/deals/Pipeline';
import AiReply from '../pages/email/AiReply';
import Campaigns from '../pages/email/Campaigns';
import Analytics from '../pages/analytics/Analytics';
import AiAssistant from '../pages/ai/AiAssistant';
import Notifications from '../pages/notifications/Notifications';

function PrivateRoute({ children }: { children: React.ReactNode }) {
  const isAuthenticated = useSelector((s: RootState) => s.auth.isAuthenticated);
  return isAuthenticated ? <>{children}</> : <Navigate to="/login" replace />;
}

export default function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route
        path="/"
        element={
          <PrivateRoute>
            <MainLayout />
          </PrivateRoute>
        }
      >
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="dashboard" element={<Dashboard />} />
        <Route path="leads" element={<LeadsList />} />
        <Route path="leads/:id" element={<LeadDetails />} />
        <Route path="deals" element={<Pipeline />} />
        <Route path="email/ai-reply" element={<AiReply />} />
        <Route path="email/campaigns" element={<Campaigns />} />
        <Route path="analytics" element={<Analytics />} />
        <Route path="ai" element={<AiAssistant />} />
        <Route path="notifications" element={<Notifications />} />
      </Route>
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
}
