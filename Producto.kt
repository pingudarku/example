import kotlin.math.roundToInt

// Clase base sellada (sealed) para representar un producto genérico
sealed class Producto(
    val nombre: String,
    val precioBase: Int,                // CLP en pesos
    val categoria: String,
    val tiempoPreparacionMin: Int
) {
    // Validaciones iniciales: no se permiten precios ni tiempos negativos
    init {
        require(precioBase >= 0) { "Precio no puede ser negativo: $nombre" }
        require(tiempoPreparacionMin >= 0) { "Tiempo de preparación inválido: $nombre" }
    }
    // Función abstracta: cada producto calculará su precio final de forma distinta
    abstract fun precioFinal(): Int
    // Descripción genérica: puede ser sobrescrita en subclases
    open fun descripcion(): String = nombre
}
// Clase que representa una comida
data class Comida(
    val esPremium: Boolean,
    private val base: Int,
    private val prepMin: Int,
    private val cat: String = "Comida",
    val nombreComida: String // Nombre de la comida!!
) : Producto(
    nombre = "", // Se arma en init
    precioBase = base,
    categoria = cat,
    tiempoPreparacionMin = prepMin
) {
    private val titulo = if (esPremium) " (Premium)" else ""
    // En este ejemplo, el precio ya se entrega final (si es premium viene ajustado)
    override fun precioFinal(): Int {
        // En este diseño, el precioBase ya refleja si es premium o no.
        // Si quisieras recargo automático, podrías usar: (precioBase * 1.20).roundToInt()
        return precioBase
    }
    // Descripción del producto (ejemplo: "Salmón Grillado (Premium)")
    override fun descripcion(): String = "$nombre$titulo"

    // Inicializador para forzar nombre adecuado según si es premium o no
    // truco para permitir nombre en primario sin repetir lógica de require

    init { //mejorar para que acepte mas productos

        //val nombreProducto = if (esPremium) "Salmón Grillado" else "Hamburguesa Clásica" //codigo viejo
        val nombreProducto = nombreComida //codigo nuevo, solo toma el nombre y lo muestra bien
        // Hack para asignar valor al campo protegido del padre
        // Usamos reflexión mínima: asignamos por copia
        // (para conservar data class y no romper los requires del padre)
        @Suppress("LeakingThis")
        (this as Producto).apply {
            val field = this::class.java.superclass.getDeclaredField("nombre")
            field.isAccessible = true
            field.set(this, nombreProducto)
        }
    }
}
// Enumeración para tamaños de bebida y sus factores de precio
enum class TamanoBebida(val factor: Double, val etiqueta: String) {
    PEQUENO(1.00, "Pequeño"),
    MEDIANO(1.15, "Mediano"),
    GRANDE(1.30, "Grande")
}
// Clase que representa una bebida
data class Bebida(
    val marca: String,
    val tamano: TamanoBebida,
    private val base: Int,
    private val prepMin: Int = 1,
    private val cat: String = "Bebida"
) : Producto(
    nombre = marca,
    precioBase = base,
    categoria = cat,
    tiempoPreparacionMin = prepMin
) {
    // Precio final depende del tamaño (ej: mediano = base * 1.15)
    override fun precioFinal(): Int = (precioBase * tamano.factor).roundToInt()
    // Descripción con el tamaño
    override fun descripcion(): String = "$nombre (${tamano.etiqueta})"
}

/* ===== Utilidades ===== */
// Función de extensión para mostrar un Int en formato CLP
fun Int.toCLP(): String {
    val neg = this < 0
    val abs = kotlin.math.abs(this).toString()
    val conPuntos = abs.reversed().chunked(3).joinToString(".").reversed()
    return (if (neg) "-$" else "$") + conPuntos
}