import { useQuery } from '@tanstack/react-query';
import { fetchDrivers } from '../../services/dashboardService';

export default function DriversPage() {
  const { data, isLoading, isError, error } = useQuery({
    queryKey: ['drivers'],
    queryFn: fetchDrivers,
  });

  const drivers = data || [];

  if (isLoading) return <div className="rounded-2xl border border-slate-200 bg-white p-6">Loading drivers...</div>;
  if (isError) return <div className="rounded-2xl border border-red-200 bg-red-50 p-6 text-red-700">{error?.message || 'Unable to load drivers.'}</div>;

  return (
    <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
      <h2 className="mb-4 text-lg font-semibold text-slate-900">Drivers</h2>
      <div className="grid gap-3 md:grid-cols-2 xl:grid-cols-3">
        {drivers.map((driver) => (
          <div key={driver.id} className="rounded-xl border border-slate-200 bg-slate-50 p-4">
            <div className="text-lg font-semibold text-slate-900">{driver.displayName}</div>
            <div className="mt-2 text-sm text-slate-600">{driver.email}</div>
            <div className="mt-2 text-sm text-slate-600">License: {driver.licenseNumber}</div>
            <div className="mt-3 inline-flex rounded-full bg-blue-100 px-2 py-1 text-xs font-medium text-blue-700">{driver.status}</div>
          </div>
        ))}
      </div>
    </div>
  );
}
