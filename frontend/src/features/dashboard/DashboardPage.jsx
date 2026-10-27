import { useQuery } from '@tanstack/react-query';
import { AlertTriangle, ArrowRight, CheckCircle2, Clock3, PackageCheck, RefreshCw, Truck, Users } from 'lucide-react';
import { Link } from 'react-router-dom';
import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { fetchDashboardSummary } from '../../services/dashboardService';
import { formatDateTime, formatDuration, toTitleCase } from '../../utils/formatters';

const statusOrder = [
  'CREATED',
  'READY_FOR_DISPATCH',
  'ASSIGNED',
  'IN_TRANSIT',
  'OUT_FOR_DELIVERY',
  'DELIVERED',
  'FAILED_DELIVERY',
  'CANCELLED',
  'RETURNED',
];

const numberFormatter = new Intl.NumberFormat();

export default function DashboardPage() {
  const {
    data,
    isLoading,
    isError,
    error,
    isFetching,
    dataUpdatedAt,
    refetch,
  } = useQuery({
    queryKey: ['dashboard-summary'],
    queryFn: fetchDashboardSummary,
    staleTime: 30_000,
    refetchOnWindowFocus: true,
  });

  const summary = data || {};
  const values = summary.shipmentsByStatus || {};
  const shipmentStatusChart = statusOrder
    .map((status) => ({
      name: toTitleCase(status),
      value: values[status] || 0,
    }))
    .filter((item) => item.value > 0);

  if (isLoading) {
    return (
      <div className="space-y-6" aria-live="polite" aria-busy="true">
        <div className="h-20 animate-pulse rounded-2xl bg-slate-200" />
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
          {Array.from({ length: 4 }, (_, index) => (
            <div key={index} className="h-36 animate-pulse rounded-2xl bg-white shadow-sm" />
          ))}
        </div>
        <div className="grid gap-6 xl:grid-cols-2">
          <div className="h-96 animate-pulse rounded-2xl bg-white shadow-sm" />
          <div className="h-96 animate-pulse rounded-2xl bg-white shadow-sm" />
        </div>
        <p className="sr-only">Loading operational summary...</p>
      </div>
    );
  }

  if (isError && !data) {
    return (
      <div className="rounded-2xl border border-red-200 bg-red-50 p-6 text-red-800" role="alert">
        <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h2 className="font-semibold">Dashboard data could not be loaded</h2>
            <p className="mt-1 text-sm">{error?.message || 'Please check your connection and try again.'}</p>
          </div>
          <button
            type="button"
            onClick={() => refetch()}
            className="inline-flex items-center justify-center gap-2 rounded-xl border border-red-300 bg-white px-4 py-2 text-sm font-medium hover:bg-red-100 focus:outline-none focus:ring-2 focus:ring-red-400"
          >
            <RefreshCw size={16} />
            Try again
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <section className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-slate-950 via-slate-900 to-blue-950 px-6 py-7 text-white shadow-lg sm:px-8">
        <div className="pointer-events-none absolute -right-12 -top-24 h-64 w-64 rounded-full border border-white/10" />
        <div className="pointer-events-none absolute -right-2 -top-14 h-44 w-44 rounded-full border border-white/10" />
        <div className="relative flex flex-col gap-6 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <div className="mb-3 inline-flex items-center gap-2 rounded-full border border-emerald-300/20 bg-emerald-300/10 px-3 py-1 text-xs font-medium text-emerald-200">
              <span className={`h-2 w-2 rounded-full ${isFetching ? 'animate-pulse bg-amber-300' : 'bg-emerald-300'}`} />
              {isFetching ? 'Updating operations' : 'Operations overview'}
            </div>
            <h2 className="text-2xl font-semibold tracking-tight sm:text-3xl">Your logistics, at a glance.</h2>
            <p className="mt-2 max-w-xl text-sm leading-6 text-slate-300">
              Monitor shipment progress, delivery performance, and the resources keeping your operation moving.
            </p>
          </div>
          <div className="flex shrink-0 flex-wrap items-center gap-3">
            <p className="text-xs text-slate-300" aria-live="polite">
              {dataUpdatedAt ? `Last updated ${formatDateTime(new Date(dataUpdatedAt).toISOString())}` : 'Waiting for first update'}
            </p>
          <button
            type="button"
            onClick={() => refetch()}
            disabled={isFetching}
            className="inline-flex items-center justify-center gap-2 rounded-xl border border-white/15 bg-white/10 px-4 py-2.5 text-sm font-medium text-white shadow-sm backdrop-blur transition hover:bg-white/15 focus:outline-none focus:ring-2 focus:ring-white/70 disabled:cursor-wait disabled:opacity-60"
          >
            <RefreshCw size={16} className={isFetching ? 'animate-spin' : ''} />
            {isFetching ? 'Refreshing...' : 'Refresh'}
          </button>
          </div>
        </div>
      </section>

      {isError && (
        <div className="rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-800" role="status">
          The latest refresh failed. Showing the last successfully loaded data.
        </div>
      )}

      <div className="grid gap-4 sm:grid-cols-2 2xl:grid-cols-4">
        <StatCard
          title="Total shipments"
          value={summary.totalShipments}
          icon={<PackageCheck className="text-blue-600" />}
          detail="Across all statuses"
          to="/shipments"
          tone="blue"
        />
        <StatCard
          title="Awaiting dispatch"
          value={summary.shipmentsByStatus?.READY_FOR_DISPATCH}
          icon={<Clock3 className="text-amber-600" />}
          detail="Ready for assignment"
          to="/shipments"
          tone="amber"
        />
        <StatCard
          title="In transit"
          value={summary.shipmentsByStatus?.IN_TRANSIT}
          icon={<Truck className="text-violet-600" />}
          detail="Currently on the road"
          to="/shipments"
          tone="violet"
        />
        <StatCard
          title="Delivered"
          value={summary.shipmentsByStatus?.DELIVERED}
          icon={<CheckCircle2 className="text-emerald-600" />}
          detail="Successfully completed"
          to="/shipments"
          tone="emerald"
        />
      </div>

      <div className="grid gap-5 2xl:grid-cols-[minmax(0,1.65fr)_minmax(320px,1fr)]">
        <Panel title="Shipment status" description="A live breakdown of your current shipment pipeline" eyebrow="OVERVIEW">
          {shipmentStatusChart.length ? (
            <div className="h-[340px] min-w-0">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={shipmentStatusChart} layout="vertical" margin={{ top: 4, right: 20, left: 4, bottom: 4 }}>
                  <CartesianGrid stroke="#e9eef5" strokeDasharray="4 4" horizontal={false} />
                  <XAxis type="number" allowDecimals={false} axisLine={false} tickLine={false} tick={{ fill: '#94a3b8', fontSize: 11 }} />
                  <YAxis type="category" dataKey="name" width={126} axisLine={false} tickLine={false} tick={{ fill: '#475569', fontSize: 11 }} />
                  <Tooltip
                    cursor={{ fill: '#f1f5f9', radius: 6 }}
                    contentStyle={{ border: '1px solid #e2e8f0', borderRadius: 12, boxShadow: '0 8px 24px rgba(15, 23, 42, 0.08)' }}
                    formatter={(value) => [numberFormatter.format(value), 'Shipments']}
                  />
                  <Bar dataKey="value" name="Shipments" fill="#3b82f6" background={{ fill: '#f1f5f9', radius: 5 }} radius={[0, 5, 5, 0]} maxBarSize={25} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          ) : (
            <EmptyState message="No shipments to display yet." />
          )}
        </Panel>

        <Panel title="Needs attention" description="Prioritize active issues across your operation" eyebrow="ACTION REQUIRED">
          <ul className="space-y-2.5">
            <AlertItem label="Overdue exceptions" detail="Past their target resolution time" value={summary.overdueExceptions} color="amber" to="/exceptions" />
            <AlertItem label="Critical exceptions" detail="High- or critical-severity cases" value={summary.openHighOrCriticalExceptions} color="red" to="/exceptions" />
            <AlertItem label="Delayed shipments" detail="Active shipments past expected time" value={summary.delayedShipments} color="blue" to="/shipments" />
            <AlertItem label="Failed deliveries" detail="Shipments needing follow-up" value={summary.failedShipments} color="orange" to="/shipments" />
          </ul>
          <Link
            to="/exceptions"
            className="mt-4 inline-flex items-center gap-2 rounded-lg py-1 text-sm font-semibold text-blue-700 hover:text-blue-800 focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            Open exception center <ArrowRight size={16} />
          </Link>
        </Panel>
      </div>

      <div className="grid gap-5 lg:grid-cols-3">
        <Panel title="Delivery performance" description="Successful outcomes among completed shipments" eyebrow="PERFORMANCE">
          <PerformanceMetric value={summary.deliverySuccessRatePercent} />
          <div className="mt-5 border-t border-slate-100 pt-4">
            <ActivityItem label="Average delivery time" value={formatDuration(summary.averageDeliverySeconds)} />
          </div>
        </Panel>

        <Panel title="Available fleet" description="Resources currently ready for assignment" eyebrow="FLEET READINESS">
          <div className="space-y-3">
            <KpiLink label="Available drivers" value={summary.availableDrivers} icon={<Users className="text-sky-600" size={18} />} to="/drivers" />
            <KpiLink label="Available vehicles" value={summary.availableVehicles} icon={<Truck className="text-emerald-600" size={18} />} to="/vehicles" />
          </div>
        </Panel>

        <Panel title="Exception overview" description="Open cases requiring visibility" eyebrow="EXCEPTIONS">
          <div className="flex items-center justify-between rounded-xl bg-slate-50 p-4">
            <div>
              <p className="text-sm font-medium text-slate-600">Open exceptions</p>
              <p className="mt-1 text-3xl font-semibold tracking-tight text-slate-900">{formatCount(summary.openExceptions)}</p>
            </div>
            <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-amber-100 text-amber-700">
              <AlertTriangle size={20} />
            </div>
          </div>
          <div className="mt-3 flex items-center justify-between text-sm">
            <span className="text-slate-500">Overdue cases</span>
            <span className="font-semibold text-amber-700">{formatCount(summary.overdueExceptions)}</span>
          </div>
          <Link to="/exceptions" className="mt-4 inline-flex items-center gap-2 text-sm font-semibold text-blue-700 hover:text-blue-800 focus:outline-none focus:ring-2 focus:ring-blue-500">
            Manage exceptions <ArrowRight size={16} />
          </Link>
        </Panel>
      </div>
    </div>
  );
}

function StatCard({ title, value, icon, detail, to, tone }) {
  const tones = {
    blue: 'bg-blue-50',
    amber: 'bg-amber-50',
    violet: 'bg-violet-50',
    emerald: 'bg-emerald-50',
  };

  return (
    <Link
      to={to}
      className="group relative overflow-hidden rounded-2xl border border-slate-200/80 bg-white p-5 shadow-sm transition duration-200 hover:-translate-y-0.5 hover:border-slate-300 hover:shadow-md focus:outline-none focus:ring-2 focus:ring-blue-500"
    >
      <div className="absolute inset-x-0 top-0 h-1 bg-gradient-to-r from-blue-500/70 to-cyan-400/70 opacity-0 transition group-hover:opacity-100" />
      <div className="mb-4 flex items-center justify-between">
        <div className={`flex h-11 w-11 items-center justify-center rounded-xl ${tones[tone]}`}>{icon}</div>
        <span className="flex h-8 w-8 items-center justify-center rounded-full text-slate-400 transition group-hover:bg-blue-50 group-hover:text-blue-700">
          <ArrowRight size={16} />
        </span>
      </div>
      <div className="text-3xl font-semibold tracking-tight text-slate-900">{formatCount(value)}</div>
      <div className="mt-2 text-sm font-semibold text-slate-700">{title}</div>
      <div className="mt-1 text-xs text-slate-500">{detail}</div>
    </Link>
  );
}

function Panel({ title, description, eyebrow, children }) {
  return (
    <section className="rounded-2xl border border-slate-200/80 bg-white p-5 shadow-sm sm:p-6">
      <div className="mb-5 border-b border-slate-100 pb-4">
        {eyebrow && <p className="mb-1 text-[10px] font-bold tracking-[0.16em] text-slate-400">{eyebrow}</p>}
        <h3 className="text-base font-semibold tracking-tight text-slate-900">{title}</h3>
        {description && <p className="mt-1 text-sm leading-5 text-slate-500">{description}</p>}
      </div>
      {children}
    </section>
  );
}

function PerformanceMetric({ value }) {
  const rate = Math.min(100, Math.max(0, Number(value) || 0));

  return (
    <div>
      <div className="flex items-end justify-between gap-3">
        <p className="text-4xl font-semibold tracking-tight text-slate-900">{rate.toFixed(1)}<span className="ml-0.5 text-2xl text-slate-500">%</span></p>
        <span className="mb-1 rounded-full bg-emerald-50 px-2.5 py-1 text-xs font-semibold text-emerald-700">Success rate</span>
      </div>
      <div className="mt-4 h-2 overflow-hidden rounded-full bg-slate-100" role="progressbar" aria-label="Delivery success rate" aria-valuemin={0} aria-valuemax={100} aria-valuenow={rate}>
        <div className="h-full rounded-full bg-gradient-to-r from-emerald-500 to-teal-400 transition-[width] duration-500" style={{ width: `${rate}%` }} />
      </div>
      <p className="mt-2 text-xs text-slate-500">Based on completed deliveries</p>
    </div>
  );
}

function EmptyState({ message }) {
  return (
    <div className="flex h-72 flex-col items-center justify-center rounded-xl border border-dashed border-slate-200 bg-slate-50 text-center">
      <PackageCheck className="mb-3 text-slate-400" size={28} />
      <p className="text-sm text-slate-500">{message}</p>
    </div>
  );
}

function AlertItem({ label, detail, value, color, to }) {
  const palette = {
    red: 'bg-red-50 text-red-700',
    amber: 'bg-amber-50 text-amber-700',
    blue: 'bg-blue-50 text-blue-700',
    orange: 'bg-orange-50 text-orange-700',
  };

  return (
    <li>
      <Link to={to} className="group flex items-center justify-between gap-3 rounded-xl border border-slate-100 px-3 py-3 transition hover:border-slate-200 hover:bg-slate-50 focus:outline-none focus:ring-2 focus:ring-blue-500">
        <span className={`flex h-9 w-9 shrink-0 items-center justify-center rounded-lg ${palette[color]}`}>
          <AlertTriangle size={16} />
        </span>
        <span className="min-w-0 flex-1">
          <span className="block text-sm font-semibold text-slate-800">{label}</span>
          <span className="mt-0.5 block text-xs leading-4 text-slate-500">{detail}</span>
        </span>
        <span className="flex items-center gap-2">
          <span className={`min-w-8 rounded-full px-2 py-1 text-center text-xs font-bold ${palette[color]}`}>{formatCount(value)}</span>
          <ArrowRight size={14} className="text-slate-300 transition group-hover:translate-x-0.5 group-hover:text-slate-500" />
        </span>
      </Link>
    </li>
  );
}

function KpiLink({ label, value, icon, to }) {
  return (
    <Link to={to} className="group flex items-center justify-between rounded-xl border border-slate-100 px-3 py-3 transition hover:border-slate-200 hover:bg-slate-50 focus:outline-none focus:ring-2 focus:ring-blue-500">
      <span className="flex items-center gap-3 text-sm font-medium text-slate-700">
        <span className="flex h-9 w-9 items-center justify-center rounded-lg bg-slate-50">{icon}</span>
        {label}
      </span>
      <span className="flex items-center gap-2">
        <span className="text-lg font-semibold tabular-nums text-slate-900">{formatCount(value)}</span>
        <ArrowRight size={14} className="text-slate-300 transition group-hover:translate-x-0.5 group-hover:text-slate-500" />
      </span>
    </Link>
  );
}

function ActivityItem({ label, value }) {
  return (
    <div className="flex items-center justify-between rounded-xl bg-slate-50 px-3 py-3">
      <span className="text-sm text-slate-600">{label}</span>
      <span className="text-sm font-semibold text-slate-900">{typeof value === 'number' ? formatCount(value) : value}</span>
    </div>
  );
}

function formatCount(value) {
  return numberFormatter.format(Number(value) || 0);
}
