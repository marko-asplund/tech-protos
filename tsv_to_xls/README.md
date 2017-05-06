# tsv_to_xls

TsvToExcel tool can read in multiple data files in TSV format and produce a single
SpreadsheetML (e.g. MS Excel 2003 and later) file with each source file in its own sheet.

Cell values will be mapped as text by default, but mapping can be customized by specifying target data type in a schema file.
In the schema mapping file each column is identified using a key formed in the following way:

`<source_filename_wo_extension>:<column_name>`

For example the column `student_count` values in `student_counts_per_class.tsv`
could be mapped to integer type with the following mapping:
```
student_counts_per_class:student_count<TAB>Int

```

Basic usage example:

```
sbt
runMain com.practicingtechie.xlsx.TsvToExcel -s data1.tsv -s data2.tsv -s data3.tsv --schema mydata.schema data123.xlsx
```
