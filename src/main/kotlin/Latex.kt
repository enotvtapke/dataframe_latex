import org.jetbrains.kotlinx.dataframe.AnyCol
import org.jetbrains.kotlinx.dataframe.AnyFrame
import org.jetbrains.kotlinx.dataframe.AnyRow
import org.jetbrains.kotlinx.dataframe.api.map
import org.jetbrains.kotlinx.dataframe.api.take
import org.jetbrains.kotlinx.dataframe.columns.FrameColumn
import org.jetbrains.kotlinx.dataframe.io.toJson
import kotlin.collections.map

fun AnyFrame.toLatex(rowsLimit: Int = 10, precision: Int = 1, borders: Boolean = true): String =
    buildString {
        appendLine(renderBegin(this@toLatex.columns(), borders))
        appendLine(renderBody(this@toLatex, borders, precision, rowsLimit).prependIndent("\t"))
        append(renderEnd())
    }.toString()

private fun renderBody(
    frame: AnyFrame,
    borders: Boolean,
    precision: Int,
    rowsLimit: Int,
): String = buildString {
    if (borders) appendLine(HORIZONTAL_LINE)
    appendLine("${frame.columns().map { it.name().escape() }.renderRow()} $ROWS_SEPARATOR")
    appendLine(HORIZONTAL_LINE)
    append(frame.take(rowsLimit).map { row ->
        row.values().map {
            when (it) {
                is AnyRow -> it.toJson().escape()
                is AnyFrame -> it.toLatex(borders = false, precision = precision, rowsLimit = rowsLimit)
                else -> renderValueToString(it, precision).escape()
            }
        }.renderRow()
    }.joinToString(separator = " $ROWS_SEPARATOR\n$HORIZONTAL_LINE\n"))
    append(" $ROWS_SEPARATOR")
    if (borders) append("\n" + HORIZONTAL_LINE)
}

private const val ROWS_SEPARATOR = "\\\\"
private const val HORIZONTAL_LINE = "\\hline"

private fun renderBegin(columns: List<AnyCol>, borders: Boolean): String {
    val cols = columns.joinToString(separator = "|") {
        if (it is FrameColumn<*>) "@{}c@{}" else "c"
    }
    return "\\begin{tabular}{${if (borders) "|$cols|" else cols}}"
}

private fun renderEnd(): String = "\\end{tabular}"

private fun List<String>.renderRow() = joinToString(separator = " & ")

private fun String.escape() = this.replace("_", "\\_")
