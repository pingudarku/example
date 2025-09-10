// Simulación de autenticación asíncrona con corutinas y clases selladas.
// Importamos coroutines para simular estados asíncronos
import kotlinx.coroutines.*
//import kotlinx.coroutines.delay

fun main() = runBlocking {
    try {
        println("===SISTEMA FOODEXPRESS===")
        val catalogo = GestorPedidos.inicializarCatalogo()

        println("Catálogo disponible:")

        catalogo.forEachIndexed { idx, p -> //catalogo -> cosas (muchas). forEach es para cada cosa y tambien existe forEach"Indexed" -> cada cosa del catalogo este enumerada
            //0.hamburguesa
            //1.salmon
            //2.bebida (grande)
            //3.jugo (mediano)
            //4.asdasdasd
            val etiquetaExtra = when (p) {
                is Comida -> if (p.esPremium) " (Premium)" else "" //para saber si la comida es premium
                is Bebida -> " (${p.tamano.etiqueta})" //para saber si la bebida es pequeña, mediana o grande
            }
            println("${idx + 1}. ${p.descripcion().replace(" (Premium)", "")}$etiquetaExtra - ${p.precioFinal().toCLP()}")
        }

        // Selección de productos
        print("\nSeleccione productos (números separados por coma): ")
        val seleccion = readlnOrNull().orEmpty()
            .split(",") //delimitador para cada opcion, ignora las comas y permite multiples entradas
            .mapNotNull { it.trim().toIntOrNull() } //pone en su lugar cualquier tipografia o error
            .map { it - 1 } //Le quita 1 a cada valor de la seleccion
            .filter { it in catalogo.indices } //filtra las opciones entre los indices del catalogo

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
        val trabajo = launch { //simulación de progreso en paralelo
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
        val subDesc = sub - promoDesc
        val descCliente = GestorPedidos.descuentoPorCliente(tipo, subDesc)
        val baseIva = subDesc - descCliente
        val iva = GestorPedidos.iva(baseIva)
        val total = baseIva + iva

        trabajo.join() // Esperamos a que termine el estado final

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

    } catch (ex: Exception) { //Fin del codigo main e inicio de Catch para
                              // Manejo de errores inesperados
        println("Ocurrió un error y el pedido no pudo completarse: ${ex.message}")
        println("Estado final: Error")
    }
}
