import { useMemo, useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Filter, Plus, Search } from 'lucide-react';
import { createShipment, fetchCustomers, fetchDrivers, fetchShipments, fetchVehicles, updateShipmentAssignment, updateShipmentStatus } from '../../services/dashboardService';
import { formatDateTime, statusBadgeClass, toTitleCase } from '../../utils/formatters';

const shipmentStatuses = ['CREATED','READY_FOR_DISPATCH','ASSIGNED','IN_TRANSIT','OUT_FOR_DELIVERY','DELIVERED','FAILED_DELIVERY','CANCELLED','RETURNED'];
const priorities = ['LOW','NORMAL','HIGH','CRITICAL'];

export default function ShipmentsPage() {
  const queryClient = useQueryClient();
  const [status, setStatus] = useState('');
  const [tracking, setTracking] = useState('');
  const [form, setForm] = useState({
    customerId: '',
    origin: '',
    destination: '',
    deliveryAddress: '',
    description: '',
    priority: 'NORMAL',
    estimatedDeliveryAt: '',
  });

  const { data, isLoading } = useQuery({
    queryKey: ['shipments', { status, tracking }],
    queryFn: () => fetchShipments({ page: 0, size: 20, status, tracking }),
  });

  const customersQuery = useQuery({ queryKey: ['customers'], queryFn: fetchCustomers });
  const driversQuery = useQuery({ queryKey: ['drivers'], queryFn: fetchDrivers });
  const vehiclesQuery = useQuery({ queryKey: ['vehicles'], queryFn: fetchVehicles });

  const createMutation = useMutation({
    mutationFn: createShipment,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['shipments'] });
      setForm({ customerId: '', origin: '', destination: '', deliveryAddress: '', description: '', priority: 'NORMAL', estimatedDeliveryAt: '' });
    },
  });

  const assignMutation = useMutation({
    mutationFn: ({ id, payload }) => updateShipmentAssignment(id, payload),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['shipments'] }),
  });

  const statusMutation = useMutation({
    mutationFn: ({ id, payload }) => updateShipmentStatus(id, payload),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['shipments'] }),
  });

  const shipments = data?.content || [];
  const customers = customersQuery.data || [];
  const drivers = driversQuery.data || [];
  const vehicles = vehiclesQuery.data || [];

  const handleCreate = (event) => {
    event.preventDefault();
    createMutation.mutate({
      ...form,
      customerId: Number(form.customerId),
      estimatedDeliveryAt: form.estimatedDeliveryAt ? new Date(form.estimatedDeliveryAt).toISOString() : null,
    });
  };

  const handleAssign = (shipment) => {
    const driverId = drivers[0]?.id || 1;
    const vehicleId = vehicles[0]?.id || 1;
    assignMutation.mutate({ id: shipment.id, payload: { driverId, vehicleId } });
  };

  const handleStatus = (shipment, nextStatus) => {
    statusMutation.mutate({ id: shipment.id, payload: { status: nextStatus, notes: `Updated by control tower` } });
  };

  return (
    <div className="space-y-6">
      <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
        <div className="mb-4 flex items-center justify-between gap-3">
          <h2 className="text-lg font-semibold text-slate-900">Shipment operations</h2>
          <button type="button" className="inline-flex items-center gap-2 rounded-xl bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700">
            <Plus size={16} /> Create shipment
          </button>
        </div>

        <form onSubmit={handleCreate} className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          <select value={form.customerId} onChange={(e) => setForm({ ...form, customerId: e.target.value })} className="rounded-xl border border-slate-200 bg-slate-50 p-2.5">
            <option value="">Select customer</option>
            {customers.map((customer) => <option key={customer.id} value={customer.id}>{customer.name}</option>)}
          </select>
          <input value={form.origin} onChange={(e) => setForm({ ...form, origin: e.target.value })} placeholder="Origin" className="rounded-xl border border-slate-200 bg-slate-50 p-2.5" />
          <input value={form.destination} onChange={(e) => setForm({ ...form, destination: e.target.value })} placeholder="Destination" className="rounded-xl border border-slate-200 bg-slate-50 p-2.5" />
          <input value={form.deliveryAddress} onChange={(e) => setForm({ ...form, deliveryAddress: e.target.value })} placeholder="Delivery address" className="rounded-xl border border-slate-200 bg-slate-50 p-2.5 md:col-span-2" />
          <input value={form.estimatedDeliveryAt} onChange={(e) => setForm({ ...form, estimatedDeliveryAt: e.target.value })} type="datetime-local" className="rounded-xl border border-slate-200 bg-slate-50 p-2.5" />
          <select value={form.priority} onChange={(e) => setForm({ ...form, priority: e.target.value })} className="rounded-xl border border-slate-200 bg-slate-50 p-2.5">
            {priorities.map((priority) => <option key={priority} value={priority}>{toTitleCase(priority)}</option>)}
          </select>
          <textarea value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} placeholder="Description" className="md:col-span-2 rounded-xl border border-slate-200 bg-slate-50 p-2.5" rows="3" />
          <div className="md:col-span-2 xl:col-span-3 flex justify-end">
            <button type="submit" className="rounded-xl bg-slate-900 px-4 py-2 text-white hover:bg-slate-800">
              Save shipment
            </button>
          </div>
        </form>
      </div>

      <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
        <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
          <div className="flex items-center gap-2 rounded-xl border border-slate-200 bg-slate-50 px-3 py-2">
            <Filter size={16} className="text-slate-500" />
            <select value={status} onChange={(e) => setStatus(e.target.value)} className="bg-transparent outline-none text-sm">
              <option value="">All statuses</option>
              {shipmentStatuses.map((option) => <option key={option} value={option}>{toTitleCase(option)}</option>)}
            </select>
          </div>
          <div className="flex items-center gap-2 rounded-xl border border-slate-200 bg-slate-50 px-3 py-2">
            <Search size={16} className="text-slate-500" />
            <input value={tracking} onChange={(e) => setTracking(e.target.value)} placeholder="Tracking number" className="bg-transparent text-sm outline-none" />
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-slate-200 text-left">
            <thead>
              <tr className="text-sm text-slate-600">
                <th className="pb-3 font-medium">Tracking</th>
                <th className="pb-3 font-medium">Route</th>
                <th className="pb-3 font-medium">Priority</th>
                <th className="pb-3 font-medium">Status</th>
                <th className="pb-3 font-medium">Updated</th>
                <th className="pb-3 font-medium">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-200 text-sm">
              {shipments.map((shipment) => (
                <tr key={shipment.id} className="align-top">
                  <td className="py-3 font-medium text-slate-900">{shipment.trackingNumber}</td>
                  <td className="py-3 text-slate-600">
                    <div>{shipment.origin}</div>
                    <div className="text-xs text-slate-400">→ {shipment.destination}</div>
                  </td>
                  <td className="py-3"><span className={`rounded-full px-2 py-1 text-xs font-medium ${statusBadgeClass(shipment.priority)}`}>{toTitleCase(shipment.priority)}</span></td>
                  <td className="py-3"><span className={`rounded-full px-2 py-1 text-xs font-medium ${statusBadgeClass(shipment.status)}`}>{toTitleCase(shipment.status)}</span></td>
                  <td className="py-3 text-slate-600">{formatDateTime(shipment.updatedAt)}</td>
                  <td className="py-3">
                    <div className="flex flex-wrap gap-2">
                      <button type="button" onClick={() => handleAssign(shipment)} className="rounded-lg bg-sky-100 px-2 py-1 text-xs text-sky-700">Assign</button>
                      <button type="button" onClick={() => handleStatus(shipment, 'IN_TRANSIT')} className="rounded-lg bg-emerald-100 px-2 py-1 text-xs text-emerald-700">In Transit</button>
                      <button type="button" onClick={() => handleStatus(shipment, 'DELIVERED')} className="rounded-lg bg-emerald-100 px-2 py-1 text-xs text-emerald-700">Delivered</button>
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
