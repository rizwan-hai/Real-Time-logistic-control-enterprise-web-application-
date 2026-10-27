export const formatCurrency = (value) => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    maximumFractionDigits: 0,
  }).format(value || 0);
};

export const formatDateTime = (value) => {
  if (!value) return '—';
  return new Date(value).toLocaleString('en-US', {
    dateStyle: 'medium',
    timeStyle: 'short',
  });
};

export const formatDuration = (seconds) => {
  if (seconds == null || Number.isNaN(seconds)) return '—';
  const totalMinutes = Math.round(seconds / 60);
  const hours = Math.floor(totalMinutes / 60);
  const minutes = totalMinutes % 60;
  if (hours > 0) return `${hours}h ${minutes}m`;
  return `${minutes}m`;
};

export const statusBadgeClass = (status) => {
  const normalized = String(status || '').toUpperCase();
  const palettes = {
    CREATED: 'bg-slate-100 text-slate-700',
    READY_FOR_DISPATCH: 'bg-amber-100 text-amber-700',
    ASSIGNED: 'bg-sky-100 text-sky-700',
    IN_TRANSIT: 'bg-blue-100 text-blue-700',
    OUT_FOR_DELIVERY: 'bg-violet-100 text-violet-700',
    DELIVERED: 'bg-emerald-100 text-emerald-700',
    FAILED_DELIVERY: 'bg-red-100 text-red-700',
    CANCELLED: 'bg-slate-200 text-slate-600',
    RETURNED: 'bg-orange-100 text-orange-700',
    OPEN: 'bg-red-100 text-red-700',
    ACKNOWLEDGED: 'bg-amber-100 text-amber-700',
    IN_PROGRESS: 'bg-blue-100 text-blue-700',
    ESCALATED: 'bg-orange-100 text-orange-700',
    RESOLVED: 'bg-emerald-100 text-emerald-700',
    CLOSED: 'bg-gray-200 text-gray-700',
    LOW: 'bg-green-100 text-green-700',
    MEDIUM: 'bg-amber-100 text-amber-700',
    HIGH: 'bg-orange-100 text-orange-700',
    CRITICAL: 'bg-red-100 text-red-700',
  };

  return palettes[normalized] || 'bg-slate-100 text-slate-700';
};

export const toTitleCase = (value) =>
  String(value || '')
    .replace(/_/g, ' ')
    .toLowerCase()
    .replace(/\b\w/g, (char) => char.toUpperCase());
