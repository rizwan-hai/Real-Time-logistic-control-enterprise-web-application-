import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { AlertTriangle, CheckCircle2, Flag, ShieldAlert } from 'lucide-react';
import { createException, fetchExceptions, updateExceptionSeverity, updateExceptionStatus } from '../../services/dashboardService';
import { formatDateTime, statusBadgeClass, toTitleCase } from '../../utils/formatters';

const exceptionStatuses = ['OPEN','ACKNOWLEDGED','IN_PROGRESS','ESCALATED','RESOLVED','CLOSED'];
const exceptionSeverities = ['LOW','MEDIUM','HIGH','CRITICAL'];

export default function ExceptionsPage() {
  const queryClient = useQueryClient();
  const [status, setStatus] = useState('');
  const [severity, setSeverity] = useState('');
  const [form, setForm] = useState({ shipmentId: '', type: 'DELIVERY_DELAY', severity: 'MEDIUM', description: '' });

  const { data, isLoading } = useQuery({
    queryKey: ['exceptions', { status, severity }],
    queryFn: () => fetchExceptions({ page: 0, size: 20, status, severity }),
  });

  const createMutation = useMutation({
    mutationFn: createException,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['exceptions'] });
      setForm({ shipmentId: '', type: 'DELIVERY_DELAY', severity: 'MEDIUM', description: '' });
    },
  });

  const statusMutation = useMutation({
    mutationFn: ({ id, payload }) => updateExceptionStatus(id, payload),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['exceptions'] }),
  });

  const severityMutation = useMutation({
    mutationFn: ({ id, payload }) => updateExceptionSeverity(id, payload),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['exceptions'] }),
  });

  const list = data?.content || [];

  const handleCreate = (event) => {
    event.preventDefault();
    createMutation.mutate({
      shipmentId: Number(form.shipmentId),
      type: form.type,
      severity: form.severity,
      description: form.description,
    });
  };

  return (
    <div className="space-y-6">
      <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
        <h2 className="mb-4 text-lg font-semibold text-slate-900">Exception intake</h2>
        <form onSubmit={handleCreate} className="grid gap-4 md:grid-cols-2">
          <input type="number" value={form.shipmentId} onChange={(e) => setForm({ ...form, shipmentId: e.target.value })} placeholder="Shipment ID" className="rounded-xl border border-slate-200 bg-slate-50 p-2.5" />
          <select value={form.type} onChange={(e) => setForm({ ...form, type: e.target.value })} className="rounded-xl border border-slate-200 bg-slate-50 p-2.5">
            {Object.keys({ DELIVERY_DELAY: 1, ADDRESS_ISSUE: 1, CUSTOMER_UNAVAILABLE: 1, VEHICLE_BREAKDOWN: 1, DAMAGED_PACKAGE: 1, LOST_SHIPMENT: 1, FAILED_DELIVERY: 1, SECURITY_INCIDENT: 1, OTHER: 1 }).map((value) => (
              <option key={value} value={value}>{toTitleCase(value)}</option>
            ))}
          </select>
          <select value={form.severity} onChange={(e) => setForm({ ...form, severity: e.target.value })} className="rounded-xl border border-slate-200 bg-slate-50 p-2.5">
            {exceptionSeverities.map((opt) => <option key={opt} value={opt}>{toTitleCase(opt)}</option>)}
          </select>
          <textarea value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} rows="3" placeholder="Describe the exception" className="md:col-span-2 rounded-xl border border-slate-200 bg-slate-50 p-2.5" />
          <div className="md:col-span-2 flex justify-end">
            <button type="submit" className="rounded-xl bg-red-600 px-4 py-2 text-white hover:bg-red-700">Create exception</button>
          </div>
        </form>
      </div>

      <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
        <div className="mb-4 flex flex-wrap gap-3">
          <select value={status} onChange={(e) => setStatus(e.target.value)} className="rounded-xl border border-slate-200 bg-slate-50 p-2.5">
            <option value="">All statuses</option>
            {exceptionStatuses.map((opt) => <option key={opt} value={opt}>{toTitleCase(opt)}</option>)}
          </select>
          <select value={severity} onChange={(e) => setSeverity(e.target.value)} className="rounded-xl border border-slate-200 bg-slate-50 p-2.5">
            <option value="">All severities</option>
            {exceptionSeverities.map((opt) => <option key={opt} value={opt}>{toTitleCase(opt)}</option>)}
          </select>
        </div>

        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-slate-200 text-left">
            <thead>
              <tr className="text-sm text-slate-600">
                <th className="pb-3 font-medium">ID</th>
                <th className="pb-3 font-medium">Type</th>
                <th className="pb-3 font-medium">Severity</th>
                <th className="pb-3 font-medium">Status</th>
                <th className="pb-3 font-medium">Created</th>
                <th className="pb-3 font-medium">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-200 text-sm">
              {list.map((exception) => (
                <tr key={exception.id}>
                  <td className="py-3 font-medium text-slate-900">#{exception.id}</td>
                  <td className="py-3 text-slate-700">{toTitleCase(exception.type)}</td>
                  <td className="py-3"><span className={`rounded-full px-2 py-1 text-xs font-medium ${statusBadgeClass(exception.severity)}`}>{toTitleCase(exception.severity)}</span></td>
                  <td className="py-3"><span className={`rounded-full px-2 py-1 text-xs font-medium ${statusBadgeClass(exception.status)}`}>{toTitleCase(exception.status)}</span></td>
                  <td className="py-3 text-slate-600">{formatDateTime(exception.createdAt)}</td>
                  <td className="py-3">
                    <div className="flex gap-2">
                      <button type="button" onClick={() => statusMutation.mutate({ id: exception.id, payload: { status: 'ACKNOWLEDGED', notes: 'Reviewed by control tower' } })} className="rounded-lg bg-amber-100 px-2 py-1 text-xs text-amber-700">Acknowledge</button>
                      <button type="button" onClick={() => severityMutation.mutate({ id: exception.id, payload: { severity: 'CRITICAL' } })} className="rounded-lg bg-red-100 px-2 py-1 text-xs text-red-700">Escalate</button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
