import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import org.jetbrains.kotlinx.dataframe.AnyCol
import org.jetbrains.kotlinx.dataframe.AnyFrame
import org.jetbrains.kotlinx.dataframe.DataColumn
import org.jetbrains.kotlinx.dataframe.api.asColumnGroup
import org.jetbrains.kotlinx.dataframe.api.asNumbers
import org.jetbrains.kotlinx.dataframe.api.isNumber
import org.jetbrains.kotlinx.dataframe.api.take
import org.jetbrains.kotlinx.dataframe.api.toColumn
import org.jetbrains.kotlinx.dataframe.columns.ColumnKind
import org.jetbrains.kotlinx.dataframe.columns.FrameColumn
import org.jetbrains.kotlinx.dataframe.jupyter.RenderedContent
import org.jetbrains.kotlinx.dataframe.schema.ColumnSchema
import org.jetbrains.kotlinx.dataframe.schema.DataFrameSchema
import org.jetbrains.kotlinx.dataframe.size
import org.jetbrains.kotlinx.dataframe.type
import org.jetbrains.kotlinx.dataframe.typeClass
import java.math.BigDecimal
import java.net.URL
import kotlin.collections.contains
import kotlin.collections.map
import kotlin.collections.plus
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.typeOf
import kotlin.text.padEnd
import kotlin.text.padStart
import kotlin.toString

internal fun AnyFrame.renderToString(
    rowsLimit: Int = 20,
    valueLimit: Int = 40,
    borders: Boolean = false,
    alignLeft: Boolean = false,
    columnTypes: Boolean = false,
    title: Boolean = false,
    rowIndex: Boolean = true,
): String {
    val sb = StringBuilder()

    // title
    if (title) {
        sb.appendLine("Data Frame [${size()}]")
        sb.appendLine()
    }

    // data
    val rowsCount = rowsLimit.coerceAtMost(rowsCount())
    val cols = if (rowIndex) listOf((0 until rowsCount).toColumn()) + columns() else columns()
    val header = cols.mapIndexed { colIndex, col ->
        if (columnTypes && (!rowIndex || colIndex > 0)) {
            "${col.name()}:${renderType(col)}"
        } else {
            col.name()
        }
    }
    val values = cols.map {
        val top = it.take(rowsLimit)
        val precision = if (top.isNumber()) top.asNumbers().scale() else 0
        val decimalFormat =
            if (precision >= 0) RendererDecimalFormat.fromPrecision(precision) else RendererDecimalFormat.of("%e")
        top.values().map {
            renderValueForStdout(it, valueLimit, decimalFormat = decimalFormat).truncatedContent
        }
    }
    val columnLengths = values.mapIndexed { col, vals -> (vals + header[col]).map { it.length }.maxOrNull()!! + 1 }

    // top border
    if (borders) {
        sb.append("\u230C")
        for (i in 1 until columnLengths.sum() + columnLengths.size) sb.append('-')
        sb.append("\u230D")
        sb.appendLine()
        sb.append("|")
    }

    // header
    for (col in header.indices) {
        val len = columnLengths[col]
        val str = header[col]
        val padded = if (alignLeft) str.padEnd(len) else str.padStart(len)
        sb.append(padded)
        if (borders) sb.append("|")
    }
    sb.appendLine()

    // header splitter
    if (borders) {
        sb.append("|")
        for (colLength in columnLengths) {
            for (i in 1..colLength) sb.append('-')
            sb.append("|")
        }
        sb.appendLine()
    }

    // data
    for (row in 0 until rowsCount) {
        if (borders) sb.append("|")
        for (col in values.indices) {
            val len = columnLengths[col]
            val str = values[col][row]
            val padded = if (alignLeft) str.padEnd(len) else str.padStart(len)
            sb.append(padded)
            if (borders) sb.append("|")
        }
        sb.appendLine()
    }

    // footer
    if (rowsCount() > rowsLimit) {
        sb.appendLine("...")
    } else if (borders) {
        sb.append("\u230E")
        for (i in 1 until columnLengths.sum() + columnLengths.size) sb.append('-')
        sb.append("\u230F")
        sb.appendLine()
    }
    return sb.toString()
}

internal const val DEFAULT_PRECISION = 6

internal fun <T : Number> DataColumn<T?>.scale(): Int {
    if (size() == 0) return 0
    return when (typeClass) {
        Double::class -> values().maxOf { (it as? Double)?.scale() ?: 1 }
        Float::class -> values().maxOf { (it as? Float)?.scale() ?: 1 }
        BigDecimal::class -> values().maxOf { (it as? BigDecimal)?.scale() ?: 1 }
        Number::class -> values().maxOf { (it as? Number)?.scale() ?: 0 }
        else -> 0
    }.coerceAtMost(DEFAULT_PRECISION)
}

internal fun Double.scale() = if (isFinite()) toBigDecimal().scale() else 0

internal fun Float.scale() = if (isFinite()) toBigDecimal().scale() else 0

internal fun Number.scale(): Int =
    when (this) {
        is Double -> scale()
        is Float -> scale()
        is Int, is Long -> 0
        is BigDecimal -> scale()
        else -> 0
    }

internal val valueToStringLimitDefault = 1000

internal fun renderValueToString(value: Any?, decimalFormat: RendererDecimalFormat): String =
    when (value) {
        is AnyFrame -> "[${value.size()}]".let { if (value.rowsCount() == 1) it + " " + value[0].toString() else it }

        is Double -> value.format(decimalFormat)

        is Float -> value.format(decimalFormat)

        is BigDecimal -> value.format(decimalFormat)

        is List<*> -> if (value.isEmpty()) "[ ]" else value.toString()

        is Array<*> -> if (value.isEmpty()) "[ ]" else value.toList().toString()

        else ->
            value?.asArrayAsListOrNull()
                ?.let { renderValueToString(it, decimalFormat) }
                ?: value.toString()
    }

internal fun renderValueForStdout(
    value: Any?,
    limit: Int = valueToStringLimitDefault,
    decimalFormat: RendererDecimalFormat = RendererDecimalFormat.fromPrecision(DEFAULT_PRECISION),
): RenderedContent =
    renderValueToString(value, decimalFormat)
        .truncate(limit)
        .let { it.copy(truncatedContent = it.truncatedContent.escapeNewLines()) }

internal fun Double.format(decimalFormat: RendererDecimalFormat): String = decimalFormat.format.format(this)

@JvmInline
public value class RendererDecimalFormat private constructor(internal val format: String) {
    public companion object {
        public fun fromPrecision(precision: Int): RendererDecimalFormat {
            require(precision >= 0) { "precision must be >= 0. for custom format use RendererDecimalFormat.of" }
            return RendererDecimalFormat("%.${precision}f")
        }

        public fun of(format: String): RendererDecimalFormat = RendererDecimalFormat(format)

        internal val DEFAULT: RendererDecimalFormat = fromPrecision(DEFAULT_PRECISION)
    }
}

internal fun String.truncate(limit: Int): RenderedContent =
    if (limit in 1 until length) {
        if (limit < 4) {
            RenderedContent.truncatedText("...", this)
        } else {
            RenderedContent.truncatedText(substring(0, (limit - 3).coerceAtLeast(1)) + "...", this)
        }
    } else {
        RenderedContent.text(this)
    }

internal fun String.escapeNewLines() = replace("\n", "\\n").replace("\r", "\\r")


internal fun Float.format(decimalFormat: RendererDecimalFormat): String = decimalFormat.format.format(this)

internal fun BigDecimal.format(decimalFormat: RendererDecimalFormat): String = decimalFormat.format.format(this)

internal fun renderType(column: AnyCol) =
    when (column.kind()) {
        ColumnKind.Value -> renderType(column.type)

        ColumnKind.Frame -> {
            val table = column.asAnyFrameColumn()
            "[${renderSchema(table.schema.value)}]"
        }

        ColumnKind.Group -> {
            val group = column.asColumnGroup()
            "{${renderSchema(group)}}"
        }
    }

internal fun renderType(type: KType?): String {
    return when (type?.classifier) {
        null -> "*"

        Nothing::class -> "Nothing" + if (type.isMarkedNullable) "?" else ""

        else -> {
            val fullName = type.jvmErasure.qualifiedName ?: return type.toString()
            val name = when {
                // catching cases like `typeOf<Array<Int>>().jvmErasure.qualifiedName == "IntArray"`
                // https://github.com/Kotlin/dataframe/issues/678
                type.isSubtypeOf(typeOf<Array<*>>()) ->
                    "Array"

                type.classifier == URL::class ->
                    fullName.removePrefix("java.net.")

                type.classifier in listOf(LocalDateTime::class, LocalTime::class) ->
                    fullName.removePrefix("java.time.")

                fullName.startsWith("kotlin.collections.") ->
                    fullName.removePrefix("kotlin.collections.")

                fullName.startsWith("kotlin.") ->
                    fullName.removePrefix("kotlin.")

                fullName.startsWith("org.jetbrains.kotlinx.dataframe.") ->
                    fullName.removePrefix("org.jetbrains.kotlinx.dataframe.")

                else -> fullName
            }

            buildString {
                append(name)
                if (type.arguments.isNotEmpty()) {
                    val arguments = type.arguments.joinToString {
                        renderType(it.type)
                    }
                    append("<$arguments>")
                }
                if (type.isMarkedNullable) {
                    append("?")
                }
            }
        }
    }
}

internal fun renderSchema(schema: DataFrameSchema): String =
    schema.columns.map { "${it.key}:${renderType(it.value)}" }.joinToString()

internal fun renderType(column: ColumnSchema) =
    when (column) {
        is ColumnSchema.Value -> {
            renderType(column.type)
        }

        is ColumnSchema.Frame -> {
            "[${renderSchema(column.schema)}]"
        }

        is ColumnSchema.Group -> {
            "{${renderSchema(column.schema)}}"
        }

        else -> throw NotImplementedError()
    }

internal fun renderSchema(df: AnyFrame): String = df.columns().joinToString { "${it.name()}:${renderType(it)}" }

internal fun AnyCol.asAnyFrameColumn(): FrameColumn<*> = this as FrameColumn<*>
