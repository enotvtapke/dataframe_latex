# Dataframe extension to export dataframes to LaTeX tables

Project adds a new extension function for AnyFrame called `toLatex` that export dataframe to LaTeX table.

`toLatex` returns `String` and has 3 arguments:
* `rowsLimit` - limits the number of rows that should be present in resulted LaTex table
* `precision` - determine with how many digits after point should float values be rendered 
* `borders` - `true` when returned table should have outer borders, `false` when not 
