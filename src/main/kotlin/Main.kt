@file:ImportDataSchema(
    "MyTest",
    "my_test.json",
)

import org.jetbrains.kotlinx.dataframe.annotations.ImportDataSchema

fun main() {
    val df1 = MyTest.readJson("my_test.json")
    print(df1.toLatex())
//    print(df1.Title[0].toString())
}
