import org.jetbrains.kotlinx.dataframe.AnyFrame
import org.jetbrains.kotlinx.dataframe.AnyRow
import org.jetbrains.kotlinx.dataframe.api.map
import org.jetbrains.kotlinx.dataframe.api.take
import org.jetbrains.kotlinx.dataframe.io.toJson
import org.jetbrains.kotlinx.dataframe.size
import kotlin.collections.map

fun AnyFrame.toLatex(rowsLimit: Int = 10, precision: Int = 1): String =
    buildString {
        appendLine(renderHeader(this@toLatex.size().ncol))
        appendLine(HORIZONTAL_LINE)
        appendLine(columns().map { it.name().escape() }.renderRow())
        appendLine(HORIZONTAL_LINE)
        appendLine(this@toLatex.take(rowsLimit).map { row ->
            row.values().map {
                when (it) {
                    is AnyRow -> it.toJson().escape()
                    is AnyFrame -> it.toLatex()
                    else -> renderValueToString(it, precision).escape()
                }
            }.renderRow() + "\n"
        }.joinToString(separator = "$HORIZONTAL_LINE\n", postfix = HORIZONTAL_LINE))
        appendLine("\\end{tabular}")
    }.toString()

private const val ROWS_SEPARATOR = "\\\\"
private const val HORIZONTAL_LINE = "\\hline"

private fun renderHeader(columnsNum: Int): String = "\\begin{tabular}{${"|c".repeat(columnsNum) + "|"}}"

private fun List<String>.renderRow() = joinToString(separator = " & ", postfix = " $ROWS_SEPARATOR")

private fun String.escape() = this.replace("_", "\\_")
