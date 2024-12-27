@file:ImportDataSchema(
    "MyTest",
    "my_test.json",
)

import org.jetbrains.kotlinx.dataframe.annotations.ImportDataSchema
import org.jetbrains.kotlinx.dataframe.io.toCsv

fun main() {
    val df1 = MyTest.readJson("my_test.json")
    print(df1.toLatex())
    print(df1.renderToString(borders = true))
    print(df1.toCsv())
//    print(df1.Title[0].toString())
//    print(df1.schema())
}
