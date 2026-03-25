export default function ProductoDetalleLoading() {
  return (
    <main className="min-h-screen bg-gray-50">
      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="h-4 w-48 bg-gray-200 rounded animate-pulse mb-6" />

        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
          <div className="grid grid-cols-1 md:grid-cols-2">
            <div className="aspect-square bg-gray-200 animate-pulse" />
            <div className="p-6 sm:p-8 flex flex-col gap-4">
              <div className="h-3 w-16 bg-gray-200 rounded animate-pulse" />
              <div className="h-8 w-3/4 bg-gray-200 rounded animate-pulse" />
              <div className="h-8 w-32 bg-gray-200 rounded animate-pulse" />
              <div className="space-y-2">
                <div className="h-4 w-full bg-gray-200 rounded animate-pulse" />
                <div className="h-4 w-full bg-gray-200 rounded animate-pulse" />
                <div className="h-4 w-2/3 bg-gray-200 rounded animate-pulse" />
              </div>
              <div className="h-4 w-28 bg-gray-200 rounded animate-pulse" />
              <div className="mt-auto h-12 w-full bg-gray-200 rounded-xl animate-pulse" />
            </div>
          </div>
        </div>
      </div>
    </main>
  );
}
