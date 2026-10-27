export default function AccessDeniedPage() {
  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-100 px-4">
      <div className="max-w-md rounded-2xl border border-red-200 bg-white p-8 text-center shadow-lg">
        <div className="mb-4 text-5xl">⛔</div>
        <h1 className="text-2xl font-semibold text-slate-900">Access denied</h1>
        <p className="mt-3 text-sm text-slate-600">
          Your current role does not grant access to this page.
        </p>
        <a href="/dashboard" className="mt-6 inline-flex rounded-xl bg-slate-900 px-4 py-2 text-white hover:bg-slate-800">
          Return to dashboard
        </a>
      </div>
    </div>
  );
}
