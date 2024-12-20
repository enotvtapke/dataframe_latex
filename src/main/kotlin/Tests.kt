@file:ImportDataSchema(
    "Repository",
    "https://raw.githubusercontent.com/Kotlin/dataframe/master/data/jetbrains_repositories.csv",
)

import org.jetbrains.kotlinx.dataframe.annotations.ImportDataSchema
import org.jetbrains.kotlinx.dataframe.api.*

fun main() {
    // Repository.readCSV() has argument 'path' with default value https://raw.githubusercontent.com/Kotlin/dataframe/master/data/jetbrains_repositories.csv
    val df = Repository.readCSV()
    // Use generated properties to access data in rows
    df.maxBy { stargazersCount }.print()
    // Or to access columns in dataframe.
    print(df.fullName.count { it.contains("kotlin") })
}