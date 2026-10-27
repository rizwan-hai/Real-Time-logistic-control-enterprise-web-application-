import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { fetchCurrentUser, updateCurrentUser, changePassword } from '../../services/dashboardService';
import { useAuth } from '../../context/AuthContext';

export default function ProfilePage() {
  const queryClient = useQueryClient();
  const { user } = useAuth();
  const [displayName, setDisplayName] = useState(user?.displayName || '');
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');

  const { data } = useQuery({
    queryKey: ['current-user'],
    queryFn: fetchCurrentUser,
    enabled: Boolean(user),
  });

  const updateMutation = useMutation({
    mutationFn: updateCurrentUser,
    onSuccess: (response) => {
      queryClient.setQueryData(['current-user'], response);
    },
  });

  const passwordMutation = useMutation({
    mutationFn: changePassword,
  });

  const currentProfile = data || user;

  return (
    <div className="grid gap-6 xl:grid-cols-2">
      <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
        <h2 className="text-lg font-semibold text-slate-900">Profile</h2>
        <div className="mt-4 space-y-4">
          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">Display name</label>
            <input value={displayName} onChange={(e) => setDisplayName(e.target.value)} className="w-full rounded-xl border border-slate-200 bg-slate-50 p-2.5" />
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">Email</label>
            <input value={currentProfile?.email || ''} disabled className="w-full rounded-xl border border-slate-200 bg-slate-100 p-2.5 text-slate-500" />
          </div>
          <button type="button" onClick={() => updateMutation.mutate({ displayName })} className="rounded-xl bg-slate-900 px-4 py-2 text-white hover:bg-slate-800">
            Update profile
          </button>
        </div>
      </div>

      <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
        <h2 className="text-lg font-semibold text-slate-900">Change password</h2>
        <div className="mt-4 space-y-4">
          <input type="password" value={currentPassword} onChange={(e) => setCurrentPassword(e.target.value)} placeholder="Current password" className="w-full rounded-xl border border-slate-200 bg-slate-50 p-2.5" />
          <input type="password" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} placeholder="New password" className="w-full rounded-xl border border-slate-200 bg-slate-50 p-2.5" />
          <button type="button" onClick={() => passwordMutation.mutate({ currentPassword, newPassword })} className="rounded-xl bg-blue-600 px-4 py-2 text-white hover:bg-blue-700">
            Change password
          </button>
        </div>
      </div>
    </div>
  );
}
