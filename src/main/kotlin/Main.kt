@file:ImportDataSchema(
    "JetBrains",
    "jetbrains.json",
)
@file:ImportDataSchema(
    "Test",
    "test.json",
)
@file:ImportDataSchema(
    "MyTest",
    "my_test.json",
)Save

import org.jetbrains.kotlinx.dataframe.AnyFrame
import org.jetbrains.kotlinx.dataframe.AnyRow
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.annotations.ImportDataSchema
import org.jetbrains.kotlinx.dataframe.api.add
import org.jetbrains.kotlinx.dataframe.api.dropNulls
import org.jetbrains.kotlinx.dataframe.api.filter
import org.jetbrains.kotlinx.dataframe.api.map
import org.jetbrains.kotlinx.dataframe.api.take
import org.jetbrains.kotlinx.dataframe.api.toListOf
import org.jetbrains.kotlinx.dataframe.impl.asList
import org.jetbrains.kotlinx.dataframe.io.readCSV
import org.jetbrains.kotlinx.dataframe.io.readJson
import org.jetbrains.kotlinx.dataframe.io.toJson
import org.jetbrains.kotlinx.dataframe.size
import java.io.StringWriter
import java.math.BigDecimal
import kotlin.collections.map

fun main() {
    data class Passenger(
        val survived: Int,
        val homedest: String?,
        val age: Int,
        val lastName: String
    )
    val df = JetBrains.readJson("jetbrains.json")
    val df1 = MyTest.readJson("my_test.json")
    print(df1.toLatex())
    print(df1.Title[0].toString())
//    print(df.repos[0].nodeId.toLatex(10, 100))
    val passengers1 = DataFrame.readCSV("titanic.csv", delimiter=';')
//    println(passengers1.toLatex(3))
//    println(df[0].toLatex(3))
    val passengers = DataFrame.readCSV("titanic.csv", delimiter=';')
        .add(Passenger::lastName) { "name"<String>().split(",").last() }
        .dropNulls(Passenger::age)
        .filter {
            it[Passenger::survived] == 1 &&
                    it[Passenger::homedest]?.endsWith("NY") == true &&
                    it[Passenger::age] in 10..20
        }
        .toListOf<Passenger>()
//    print(passengers)
}

fun AnyFrame.toLatex(limit: Int = 10, limit2: Int = 100): String =
    StringWriter().let {
        it.appendLine("\\begin{tabular}{${"|c".repeat(this.size().ncol) + "|"}}")

        it.appendLine("\\hline")
        it.appendLine(columns().map { it.name() }.joinToString(separator = " & ", postfix = " \\\\"))
        it.appendLine("\\hline")
        it.appendLine(this.take(limit).map { row ->
            val values = row.values().take(limit2).map {
                when (it) {
                    is AnyRow -> it.toJson() // TODO
                    is AnyFrame -> it.toLatex()
                    else -> it
                }
            }.map { renderValueToString(it) }
            values.joinToString(separator = " & ", postfix = " \\\\\n")
        }.joinToString(separator = "\\hline\n", postfix = "\\hline"))
        it.appendLine("\\end{tabular}")
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