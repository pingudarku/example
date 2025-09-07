// Simulación de autenticación asíncrona con corutinas y clases selladas.
// Importamos coroutines para simular estados asíncronos
import kotlinx.coroutines.*
//import kotlinx.coroutines.delay

fun main() = runBlocking {
    try {
        println("===SISTEMA FOODEXPRESS===")
        val catalogo = GestorPedidos.inicializarCatalogo()

        println("Catálogo disponible:")
        catalogo.forEachIndexed { idx, p ->
            val etiquetaExtra = when (p) {
                is Comida -> if (p.esPremium) " (Premium)" else ""
                is Bebida -> " (${p.tamano.etiqueta})"
                else -> ""
            }
            println("${idx + 1}. ${p.descripcion().replace(" (Premium)", "")}$etiquetaExtra - ${p.precioFinal().toCLP()}")
        }
        // Selección de productos
        print("\nSeleccione productos (números separados por coma): ")
        val seleccion = readlnOrNull().orEmpty()
            .split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .map { it - 1 }
            .filter { it in catalogo.indices }

        if (seleccion.isEmpty()) {
            println("No se seleccionaron productos válidos. Saliendo…")
            return@runBlocking
        }
        // Tipo de cliente
        print("Cliente tipo (regular/vip/premium): ")
        val tipo = TipoCliente.desde(readlnOrNull().orEmpty())

        println("\nProcesando pedido...")

        // Simulación asíncrona de estados
        var estadoFinal: EstadoPedido = EstadoPedido.Pendiente
        val job = launch {
            // Marcamos "En Preparación" de inmediato (simulación de progreso en paralelo)
            println("Estado: En Preparación") // Estado intermedio
            delay(900L) // simulación de preparación
            estadoFinal = EstadoPedido.Listo // Estado final
        }
        // Productos seleccionados
        val seleccionados = seleccion.map { catalogo[it] }
        val lineas = seleccionados.map { it.descripcion() to it.precioFinal() }

        // Cálculos
        val sub = GestorPedidos.subtotal(seleccionados)
        val (promoDesc, promos) = GestorPedidos.promociones(seleccionados)
        val baseDesc = sub - promoDesc
        val descCliente = GestorPedidos.descuentoPorCliente(tipo, baseDesc)
        val baseIva = baseDesc - descCliente
        val iva = GestorPedidos.iva(baseIva)
        val total = baseIva + iva

        job.join() // Esperamos a que termine el estado final

        // Mostrar resumen
        println("\n===RESUMEN DEL PEDIDO===")
        lineas.forEach { (desc, precio) ->
            println("- $desc: ${precio.toCLP()}")
        }
        println("Subtotal: ${sub.toCLP()}")

        // (Las promos no aplican al ejemplo 1,3; pero se mostrarían así si existieran)
        if (promoDesc > 0) {
            promos.forEach { println(it) }
        }

        val etiqueta = "${tipo.etiqueta.uppercase()} (${(tipo.descuento * 100).toInt()}%)"
        println("Descuento $etiqueta: -${descCliente.toCLP()}")
        println("IVA (19%): ${iva.toCLP()}")
        println("TOTAL: ${total.toCLP()}\n")

        val etiquetaEstadoFinal = when (estadoFinal) {
            is EstadoPedido.Listo -> "Listo"
            is EstadoPedido.EnPreparacion -> "En Preparación"
            is EstadoPedido.Pendiente -> "Pendiente"
            is EstadoPedido.Error -> "Error"
        }
        println("Estado final: $etiquetaEstadoFinal")

    } catch (ex: Exception) {
        // Manejo de errores inesperados
        println("Ocurrió un error y el pedido no pudo completarse: ${ex.message}")
        println("Estado final: Error")
    }
}
