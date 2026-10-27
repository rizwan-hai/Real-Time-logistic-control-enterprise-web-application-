import { useQuery } from '@tanstack/react-query';
import { fetchUsers } from '../../services/dashboardService';

export default function UsersPage() {
  const { data, isLoading, isError, error } = useQuery({
    queryKey: ['users'],
    queryFn: fetchUsers,
  });

  const users = data || [];

  if (isLoading) return <div className="rounded-2xl border border-slate-200 bg-white p-6">Loading users...</div>;
  if (isError) return <div className="rounded-2xl border border-red-200 bg-red-50 p-6 text-red-700">{error?.message || 'Unable to load users.'}</div>;

  return (
    <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
      <h2 className="mb-4 text-lg font-semibold text-slate-900">User administration</h2>
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-slate-200 text-left">
          <thead>
            <tr className="text-sm text-slate-600">
              <th className="pb-3 font-medium">Email</th>
              <th className="pb-3 font-medium">Name</th>
              <th className="pb-3 font-medium">Roles</th>
              <th className="pb-3 font-medium">Status</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-200 text-sm">
            {users.map((user) => (
              <tr key={user.id}>
                <td className="py-3 text-slate-800">{user.email}</td>
                <td className="py-3 text-slate-700">{user.displayName}</td>
                <td className="py-3 text-slate-600">{user.roles?.join(', ') || '—'}</td>
                <td className="py-3"><span className={`rounded-full px-2 py-1 text-xs font-medium ${user.active ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-200 text-slate-600'}`}>{user.active ? 'Active' : 'Inactive'}</span></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
