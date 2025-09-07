
// Clase sellada que representa los estados posibles de un pedido
sealed class EstadoPedido {
    object Pendiente : EstadoPedido()
    object EnPreparacion : EstadoPedido()
    object Listo : EstadoPedido()
    data class Error(val mensaje: String) : EstadoPedido()
}
