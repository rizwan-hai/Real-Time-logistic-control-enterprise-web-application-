import { useEffect, useMemo, useState } from 'react';
import { Bell, ChevronDown, LayoutDashboard, Package, AlertTriangle, Users, Truck, FileText, ShieldCheck, LogOut, Menu, Search, UserRound, Settings } from 'lucide-react';
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { fetchNotifications } from '../services/dashboardService';

const navItems = [
  { label: 'Dashboard', to: '/dashboard', icon: LayoutDashboard, roles: ['ADMIN', 'LOGISTICS_MANAGER', 'DISPATCHER', 'VIEWER'] },
  { label: 'Shipments', to: '/shipments', icon: Package, roles: ['ADMIN', 'LOGISTICS_MANAGER', 'DISPATCHER', 'DRIVER', 'VIEWER'] },
  { label: 'Exceptions', to: '/exceptions', icon: AlertTriangle, roles: ['ADMIN', 'LOGISTICS_MANAGER', 'DISPATCHER', 'DRIVER', 'SECURITY_AUDITOR', 'VIEWER'] },
  { label: 'Customers', to: '/customers', icon: Users, roles: ['ADMIN', 'LOGISTICS_MANAGER', 'DISPATCHER', 'VIEWER'] },
  { label: 'Drivers', to: '/drivers', icon: Truck, roles: ['ADMIN', 'LOGISTICS_MANAGER', 'DISPATCHER', 'VIEWER'] },
  { label: 'Vehicles', to: '/vehicles', icon: Package, roles: ['ADMIN', 'LOGISTICS_MANAGER', 'DISPATCHER', 'VIEWER'] },
  { label: 'Reports', to: '/reports', icon: FileText, roles: ['ADMIN', 'LOGISTICS_MANAGER', 'VIEWER'] },
  { label: 'Users', to: '/users', icon: Users, roles: ['ADMIN'] },
  { label: 'Security', to: '/security', icon: ShieldCheck, roles: ['ADMIN', 'SECURITY_AUDITOR'] },
];

export default function AppLayout() {
  const { user, logout } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);

  const visibleNav = useMemo(() => {
    const roles = user?.roles || [];
    return navItems.filter((item) => item.roles.some((role) => roles.includes(role)) || !user);
  }, [user]);

  useEffect(() => {
    const loadNotifications = async () => {
      try {
        const response = await fetchNotifications();
        const list = response.content || response;
        setNotifications(list);
        setUnreadCount(list.filter((item) => !item.read).length);
      } catch (error) {
        console.warn('Notification load failed', error);
      }
    };

    if (user) loadNotifications();
  }, [user]);

  const currentTitle = useMemo(() => {
    const segment = location.pathname.split('/').filter(Boolean)[0] || 'dashboard';
    const item = navItems.find((nav) => nav.to.includes(segment));
    return item?.label || 'Dashboard';
  }, [location]);

  return (
    <div className="min-h-screen bg-slate-100 text-slate-800">
      <div className="flex min-h-screen">
        <aside className={`${sidebarOpen ? 'w-72' : 'w-24'} border-r border-slate-200 bg-slate-900 text-slate-100 transition-all duration-200`}>
          <div className="flex h-16 items-center justify-between border-b border-slate-800 px-4">
            <div className="flex items-center gap-3 overflow-hidden">
              <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-blue-600 font-bold text-white">CT</div>
              {sidebarOpen && <span className="text-lg font-semibold">Control Tower</span>}
            </div>
            <button type="button" onClick={() => setSidebarOpen((open) => !open)} className="rounded-lg p-2 text-slate-300 hover:bg-slate-800">
              <Menu size={18} />
            </button>
          </div>

          <nav className="space-y-2 p-3">
            {visibleNav.map(({ label, to, icon: Icon }) => (
              <NavLink
                key={to}
                to={to}
                className={({ isActive }) => `flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium transition ${
                  isActive ? 'bg-blue-600 text-white shadow-lg' : 'text-slate-300 hover:bg-slate-800 hover:text-white'
                }`}
              >
                <Icon size={18} />
                {sidebarOpen && <span>{label}</span>}
              </NavLink>
            ))}
          </nav>
        </aside>

        <main className="flex-1">
          <header className="flex h-16 items-center justify-between border-b border-slate-200 bg-white px-5 shadow-sm">
            <div className="flex items-center gap-4">
              <div className="relative hidden md:block">
                <Search className="absolute left-3 top-2.5 text-slate-400" size={16} />
                <input
                  className="w-72 rounded-xl border border-slate-200 bg-slate-50 py-2 pl-9 pr-3 text-sm outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
                  placeholder="Search shipments, customers..."
                />
              </div>
            </div>

            <div className="flex items-center gap-4">
              <button type="button" className="relative rounded-xl border border-slate-200 p-2 text-slate-600 hover:bg-slate-100">
                <Bell size={18} />
                {unreadCount > 0 && <span className="absolute -right-1 -top-1 flex h-5 min-w-5 items-center justify-center rounded-full bg-red-500 px-1 text-[10px] font-bold text-white">{unreadCount}</span>}
              </button>
              <div className="flex items-center gap-3 rounded-xl border border-slate-200 bg-slate-50 px-2 py-1.5">
                <div className="flex h-9 w-9 items-center justify-center rounded-full bg-blue-600 text-sm font-bold text-white">
                  {user?.displayName?.charAt(0)?.toUpperCase() || 'U'}
                </div>
                <div className="hidden sm:block">
                  <div className="text-sm font-medium text-slate-900">{user?.displayName || 'User'}</div>
                  <div className="text-[11px] text-slate-500">{user?.roles?.join(', ') || 'Viewer'}</div>
                </div>
                <ChevronDown size={16} className="text-slate-500" />
              </div>
              <button type="button" onClick={() => navigate('/profile')} className="rounded-xl p-2 text-slate-600 hover:bg-slate-100" aria-label="Profile">
                <UserRound size={18} />
              </button>
              <button type="button" onClick={logout} className="rounded-xl p-2 text-slate-600 hover:bg-slate-100" aria-label="Logout">
                <LogOut size={18} />
              </button>
            </div>
          </header>

          <div className="p-6">
            <div className="mb-6 flex items-center justify-between">
              <div>
                <h1 className="text-2xl font-semibold text-slate-900">{currentTitle}</h1>
                <p className="mt-1 text-sm text-slate-500">Operational visibility and exception management</p>
              </div>
              <div className="flex items-center gap-2 rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm text-slate-600">
                <Settings size={16} />
                <span>Live</span>
              </div>
            </div>
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  );
}
