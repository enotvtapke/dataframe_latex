import org.jetbrains.kotlinx.dataframe.AnyFrame
import org.jetbrains.kotlinx.dataframe.impl.asList
import org.jetbrains.kotlinx.dataframe.io.toJson
import java.math.BigDecimal

fun Any.asArrayAsListOrNull(): List<*>? =
    when (this) {
        is BooleanArray -> asList()
        is ByteArray -> asList()
        is ShortArray -> asList()
        is IntArray -> asList()
        is LongArray -> asList()
        is FloatArray -> asList()
        is DoubleArray -> asList()
        is CharArray -> asList()
        is UByteArray -> asList()
        is UShortArray -> asList()
        is UIntArray -> asList()
        is ULongArray -> asList()
        is Array<*> -> asList()
        else -> null
    }

fun renderValueToString(value: Any?, precision: Int = 6): String =
    when (value) {
        is AnyFrame -> value.toJson()

        is Double -> value.format(precision)

        is Float -> value.format(precision)

        is BigDecimal -> value.format(precision)

        is List<*> -> if (value.isEmpty()) "[ ]" else value.toString()

        is Array<*> -> if (value.isEmpty()) "[ ]" else value.toList().toString()

        else ->
            value?.asArrayAsListOrNull()
                ?.let { renderValueToString(it) }
                ?: value.toString()
    }

fun Any.format(precision: Int) = "%.${precision}f".format(this)
