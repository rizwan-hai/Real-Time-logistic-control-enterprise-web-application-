import { useQuery } from '@tanstack/react-query';
import { fetchVehicles } from '../../services/dashboardService';

export default function VehiclesPage() {
  const { data, isLoading, isError, error } = useQuery({
    queryKey: ['vehicles'],
    queryFn: fetchVehicles,
  });

  const vehicles = data || [];

  if (isLoading) return <div className="rounded-2xl border border-slate-200 bg-white p-6">Loading vehicles...</div>;
  if (isError) return <div className="rounded-2xl border border-red-200 bg-red-50 p-6 text-red-700">{error?.message || 'Unable to load vehicles.'}</div>;

  return (
    <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
      <h2 className="mb-4 text-lg font-semibold text-slate-900">Fleet</h2>
      <div className="grid gap-3 md:grid-cols-2 xl:grid-cols-3">
        {vehicles.map((vehicle) => (
          <div key={vehicle.id} className="rounded-xl border border-slate-200 bg-slate-50 p-4">
            <div className="text-lg font-semibold text-slate-900">{vehicle.registration}</div>
            <div className="mt-2 text-sm text-slate-600">{vehicle.type}</div>
            <div className="mt-2 text-sm text-slate-600">Capacity: {vehicle.capacity}</div>
            <div className="mt-3 inline-flex rounded-full bg-emerald-100 px-2 py-1 text-xs font-medium text-emerald-700">{vehicle.status}</div>
          </div>
        ))}
      </div>
    </div>
  );
}
