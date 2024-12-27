@file:ImportDataSchema(
    "MyTest",
    "my_test.json",
)

import org.jetbrains.kotlinx.dataframe.AnyFrame
import org.jetbrains.kotlinx.dataframe.AnyRow
import org.jetbrains.kotlinx.dataframe.annotations.ImportDataSchema
import org.jetbrains.kotlinx.dataframe.api.map
import org.jetbrains.kotlinx.dataframe.api.take
import org.jetbrains.kotlinx.dataframe.impl.asList
import org.jetbrains.kotlinx.dataframe.io.toJson
import org.jetbrains.kotlinx.dataframe.size
import java.math.BigDecimal
import kotlin.collections.map

fun main() {
//    val df = JetBrains.readJson("jetbrains.json")
    val df1 = MyTest.readJson("my_test.json")
    print(df1.toLatex())
    print(df1.Title[0].toString())
}

fun String.escape() = this.replace("_", "\\_")

fun AnyFrame.toLatex(limit: Int = 10, limit2: Int = 100): String =
    buildString {
        appendLine("\\begin{tabular}{${"|c".repeat(this@toLatex.size().ncol) + "|"}}")
        appendLine("\\hline")
        appendLine(columns().map { it.name().escape() }.joinToString(separator = " & ", postfix = " \\\\"))
        appendLine("\\hline")
        appendLine(this@toLatex.take(limit).map { row ->
            val values = row.values().take(limit2).map {
                when (it) {
                    is AnyRow -> it.toJson().escape()
                    is AnyFrame -> it.toLatex()
                    else -> renderValueToString(it).escape()
                }
            }
            values.joinToString(separator = " & ", postfix = " \\\\\n")
        }.joinToString(separator = "\\hline\n", postfix = "\\hline"))
        appendLine("\\end{tabular}")
    }.toString()

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

fun renderValueToString(value: Any?): String =
    when (value) {
        is AnyFrame -> value.toLatex()

        is Double -> value.format()

        is Float -> value.format()

        is BigDecimal -> value.format()

        is List<*> -> if (value.isEmpty()) "[ ]" else value.toString()

        is Array<*> -> if (value.isEmpty()) "[ ]" else value.toList().toString()

        else ->
            value?.asArrayAsListOrNull()
                ?.let { renderValueToString(it) }
                ?: value.toString()
    }

fun Any.format(precision: Int = 6) = "%.${precision}f".format(this)