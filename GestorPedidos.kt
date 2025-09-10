// Para cálculos con redondeo
import kotlin.math.roundToInt

// Tipos de clientes y sus descuentos asociados
enum class TipoCliente(val etiqueta: String, val descuento: Double) {
    REGULAR("Regular", 0.05), //5%
    VIP("VIP", 0.10), //10%
    PREMIUM("Premium", 0.15); //15%

    companion object {
        // Convierte un string en TipoCliente (si no coincide, retorna Regular)
        fun desde(input: String): TipoCliente = when (input.trim().lowercase()) {
            "regular" -> REGULAR
            "vip" -> VIP
            "premium" -> PREMIUM
            else -> REGULAR
        }
    }
}

// Objeto que agrupa toda la lógica de negocio
object GestorPedidos {

    /* ===== Catálogo Inicial ===== */
    // Inicializa el catálogo con productos base
    fun inicializarCatalogo(): List<Producto> = listOf(
        Comida(esPremium = false, base = 8_990, prepMin = 10, nombreComida = "Hamburguesa Clásica"),     // Hamburguesa Clásica
        Comida(esPremium = true, base = 12_990, prepMin = 10, nombreComida = "Hamburguesa Tres Carnes"),
        Comida(esPremium = true,  base = 15_990, prepMin = 15, nombreComida = "Salmón Grillado"),    // Salmón Grillado (Premium)
        Comida(esPremium = true,  base = 12_990, prepMin = 18, nombreComida = "Bife a lo Pobre"),
        Comida(esPremium = false,  base = 6_990, prepMin = 4, nombreComida = "Papas Fritas con Carne Mechada"),
        Comida(esPremium = false,  base = 5_990, prepMin = 4, nombreComida = "Papas Fritas con Merluza"),
        Bebida(marca = "Coca Cola", tamano = TamanoBebida.MEDIANO, base = 1_990),
        Bebida(marca = "Coca Cola", tamano = TamanoBebida.PEQUENO, base = 990),
        Bebida(marca = "Jugo Natural (Sabores)", tamano = TamanoBebida.GRANDE, base = 2_990),
        Bebida(marca = "Jugo Natural (Sabores)", tamano = TamanoBebida.MEDIANO, base = 1_990)
    )

    /* ===== Cálculos ===== */
    // Calcula el subtotal del pedido
    fun subtotal(items: List<Producto>): Int =
        items.sumOf { it.precioFinal() }
    // Calcula el descuento según tipo de cliente
    fun descuentoPorCliente(tipo: TipoCliente, sobre: Int): Int =
        (sobre * tipo.descuento).roundToInt()
    // Aplica promociones especiales (ej: 3x2 en bebidas)
    fun promociones(items: List<Producto>): Pair<Int, List<String>> {
        var totalPromo = 0
        val descripciones = mutableListOf<String>()

        // 3x2 en bebidas (por nombre y tamaño)
        val bebidas = items.filterIsInstance<Bebida>()
        if (bebidas.size >= 3) {
            // Ordenamos por precio final ascendente y regalamos la(s) más barata(s) por cada 3
            val precioOrdenado = bebidas.map { it.precioFinal() }.sorted()
            val regalos = precioOrdenado.chunked(3).sumOf { chunk ->
                if (chunk.size == 3) chunk.first() else 0
            }
            if (regalos > 0) {
                totalPromo += regalos
                descripciones += "Promo 3x2 en bebidas: -${regalos.toCLP()}"
            }
        }

        // 5% extra si compras 3 o más ítems (no se activa en el ejemplo de 2 ítems)
        // Promo: 5% extra si hay 3 o más productos
        if (items.size >= 3) {
            val sub = subtotal(items)
            val extra = (sub * 0.05).roundToInt()
            totalPromo += extra
            descripciones += "Descuento por cantidad (5%): -${extra.toCLP()}"
        }

        return totalPromo to descripciones
    }
    // Calcula el IVA sobre un monto
    fun iva(valor: Int, tasa: Double = 0.19): Int =
        (valor * tasa).roundToInt()

    /* ===== Reporte (operaciones funcionales) ===== */
    // Clases para manejar reportes de ventas
    data class Venta(val items: List<Producto>, val total: Int)
    data class ReporteVentas(
        val totalVentas: Int,
        val totalCLP: Int,
        val porCategoria: Map<String, Int>,
        val topProductos: List<Pair<String, Int>>
    )
    // Genera reporte estadístico de ventas
    fun generarReporte(ventas: List<Venta>): ReporteVentas {
        val totalVentas = ventas.size
        val totalCLP = ventas.sumOf { it.total }
        val porCategoria = ventas
            .flatMap { it.items }
            .groupBy { it.categoria }
            .mapValues { (_, ps) -> ps.size }

        val topProductos = ventas
            .flatMap { it.items }
            .groupBy { it.descripcion() }
            .mapValues { (_, ps) -> ps.size }
            .entries
            .sortedByDescending { it.value }
            .take(5)
            .map { it.key to it.value }

        return ReporteVentas(totalVentas, totalCLP, porCategoria, topProductos)
    }
}
