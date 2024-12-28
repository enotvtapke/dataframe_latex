import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.io.readCSV
import org.jetbrains.kotlinx.dataframe.io.readJson
import kotlin.io.path.Path
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertEquals

internal class LatexTest {
    @Test
    fun nestedTest() {
        val actual = DataFrame.readJson(getFullName("nested.json")).toLatex()
        val expected = Path(getFullName("outputs/nested.tex")).readText()
        assertEquals(expected, actual)
    }

    @Test
    fun simpleTest() {
        val actual = DataFrame.readCSV(getFullName("simple.csv")).toLatex()
        val expected = Path(getFullName("outputs/simple.tex")).readText()
        assertEquals(expected, actual)
    }

    @Test
    fun floatFormattingTest() {
        val actual = DataFrame.readCSV(getFullName("simple.csv")).toLatex(precision = 5)
        val expected = Path(getFullName("outputs/float_formatting.tex")).readText()
        assertEquals(expected, actual)
    }

    @Test
    fun rowsLimitTest() {
        val actual = DataFrame.readCSV(getFullName("simple.csv")).toLatex(rowsLimit = 2)
        val expected = Path(getFullName("outputs/rows_limit.tex")).readText()
        assertEquals(expected, actual)
    }

    @Test
    fun nullsTest() {
        val actual = DataFrame.readCSV(getFullName("nulls.csv")).toLatex()
        val expected = Path(getFullName("outputs/nulls.tex")).readText()
        assertEquals(expected, actual)
    }

    @Test
    fun longCsvTest() {
        val actual = DataFrame.readCSV(getFullName("titanic_short.csv")).toLatex()
        val expected = Path(getFullName("outputs/long_csv.tex")).readText()
        assertEquals(expected, actual)
    }

    private fun getFullName(fileName: String): String = "src/test/resources/$fileName"
}
