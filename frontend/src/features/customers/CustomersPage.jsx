import { useQuery } from '@tanstack/react-query';
import { fetchCustomers } from '../../services/dashboardService';
import { formatDateTime } from '../../utils/formatters';

export default function CustomersPage() {
  const { data, isLoading, isError, error } = useQuery({
    queryKey: ['customers'],
    queryFn: fetchCustomers,
  });

  const customers = data || [];

  if (isLoading) return <div className="rounded-2xl border border-slate-200 bg-white p-6">Loading customers...</div>;
  if (isError) return <div className="rounded-2xl border border-red-200 bg-red-50 p-6 text-red-700">{error?.message || 'Unable to load customers.'}</div>;

  return (
    <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
      <h2 className="mb-4 text-lg font-semibold text-slate-900">Customers</h2>
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-slate-200 text-left">
          <thead>
            <tr className="text-sm text-slate-600">
              <th className="pb-3 font-medium">Name</th>
              <th className="pb-3 font-medium">Email</th>
              <th className="pb-3 font-medium">Phone</th>
              <th className="pb-3 font-medium">Created</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-200 text-sm">
            {customers.map((customer) => (
              <tr key={customer.id}>
                <td className="py-3 font-medium text-slate-900">{customer.name}</td>
                <td className="py-3 text-slate-600">{customer.email || '—'}</td>
                <td className="py-3 text-slate-600">{customer.phone || '—'}</td>
                <td className="py-3 text-slate-600">{formatDateTime(customer.createdAt)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
