import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.io.readCSV
import org.jetbrains.kotlinx.dataframe.io.readJson
import kotlin.test.Test

internal class LatexTest {
    @Test
    fun nestedTest() {
        val df = DataFrame.readJson(getFullName("nested.json"))
        println(df.toLatex())
    }

    @Test
    fun simpleTest() {
        val df = DataFrame.readCSV(getFullName("simple.csv"))
        println(df.toLatex())
    }

    @Test
    fun floatFormattingTest() {
        val df = DataFrame.readCSV(getFullName("simple.csv"))
        println(df.toLatex(precision = 5))
    }

    @Test
    fun rowsLimitTest() {
        val df = DataFrame.readCSV(getFullName("simple.csv"))
        println(df.toLatex(rowsLimit = 2))
    }

    @Test
    fun nullsTest() {
        val df = DataFrame.readCSV(getFullName("nulls.csv"))
        println(df.toLatex())
    }

    @Test
    fun longCsvTest() {
        val df = DataFrame.readCSV(getFullName("titanic_short.csv"))
        println(df.toLatex())
    }

    private fun getFullName(fileName: String): String = "src/test/resources/$fileName"
}
