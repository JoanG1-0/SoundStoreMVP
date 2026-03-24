interface Props {
  params: Promise<{ id: string }>;
}

export default async function DetallePedidoPage({ params }: Props) {
  const { id } = await params;
  return (
    <main>
      <h1>Pedido {id}</h1>
    </main>
  );
}
