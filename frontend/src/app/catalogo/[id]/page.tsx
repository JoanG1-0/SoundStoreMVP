interface Props {
  params: Promise<{ id: string }>;
}

export default async function ProductoDetallePage({ params }: Props) {
  const { id } = await params;
  return (
    <main>
      <h1>Detalle del producto {id}</h1>
    </main>
  );
}
