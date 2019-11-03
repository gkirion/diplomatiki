val x = sc.broadcast((10 * 1).toInt)

val start = System.currentTimeMillis
compressedDataset.map(chunk => chunk.getColumn("0").selectLessThan(x.value)).count
val end = System.currentTimeMillis
val time = (end - start) / 1000.00

val start = System.currentTimeMillis
compressedDataset.map(chunk => chunk.getColumn("0").selectLessThan(x.value).cardinality).reduce((a,b) => a + b)
val end = System.currentTimeMillis
val time = (end - start) / 1000.00

val start = System.currentTimeMillis
compressedDataset.map(chunk => {val column = chunk.getColumn("0"); val bitmap = column.selectLessThan(x.value); column.sum(bitmap)}).reduce((a,b) => a + b)
val end = System.currentTimeMillis
val time = (end - start) / 1000.00

val start = System.currentTimeMillis
compressedDataset.map(chunk => {val column = chunk.getColumn("0"); column.sum}).reduce((a,b) => a + b)
val end = System.currentTimeMillis
val time = (end - start) / 1000.00


val start = System.currentTimeMillis
compressedDataset.flatMap(chunk => chunk).map(row => (row.get("0").toString.toInt, row.getRunLength)).reduceByKey((a,b) => a + b).collect
val end = System.currentTimeMillis
val time = (end - start) / 1000.00

val selectivity = null
for (selectivity <- List(0.1, 0.25, 0.75, 1.0)) {
  val x = sc.broadcast((cardinality * selectivity).toInt)

  var start = System.currentTimeMillis
  compressedDataset.map(chunk => chunk.getColumn("0").selectLessThan(x.value)).count
  var end = System.currentTimeMillis
  var time = (end - start) / 1000.00
  timeList.add("distribution_" + distribution + "_cardinality_" + cardinality + "_encoding_" + encoding + "_selectivity_" + selectivity +  " (select) time: " + time);

  start = System.currentTimeMillis
  compressedDataset.map(chunk => chunk.getColumn("0").selectLessThan(x.value).cardinality).reduce((a,b) => a + b)
  end = System.currentTimeMillis
  time = (end - start) / 1000.00
  timeList.add("distribution_" + distribution + "_cardinality_" + cardinality + "_encoding_" + encoding + "_selectivity_" + selectivity +  " (count) time: " + time);

  start = System.currentTimeMillis
  compressedDataset.map(chunk => {val column = chunk.getColumn("0"); val bitmap = column.selectLessThan(x.value); column.sum(bitmap)}).reduce((a,b) => a + b)
  end = System.currentTimeMillis
  time = (end - start) / 1000.00
  timeList.add("distribution_" + distribution + "_cardinality_" + cardinality + "_encoding_" + encoding + "_selectivity_" + selectivity +  " (sum) time: " + time);

}

var start = System.currentTimeMillis
compressedDataset.map(chunk => {val column = chunk.getColumn("0"); column.sum}).reduce((a,b) => a + b)
var end = System.currentTimeMillis
var time = (end - start) / 1000.00
timeList.add("distribution_" + distribution + "_cardinality_" + cardinality + "_encoding_" + encoding + " (sum total) time: " + time);

start = System.currentTimeMillis
compressedDataset.flatMap(chunk => chunk).map(row => (row.get("0").toString.toInt, row.getRunLength)).reduceByKey((a,b) => a + b).collect
end = System.currentTimeMillis
time = (end - start) / 1000.00
timeList.add("distribution_" + distribution + "_cardinality_" + cardinality + "_encoding_" + encoding + " (group by) time: " + time);


val selectivity = null
for (selectivity <- List(0.1, 0.25, 0.75, 1.0)) {
  val x = sc.broadcast((cardinality * selectivity).toInt)

  var start = System.currentTimeMillis
  compressedDataset.map(chunk => chunk.getColumn("0").selectLessThan(x.value)).count
  var end = System.currentTimeMillis
  var time = (end - start) / 1000.00
  timeList.add("distribution_" + distribution + "_cardinality_" + cardinality + "_hybridcolumnar_best" + "_selectivity_" + selectivity +  " (select) time: " + time);

  start = System.currentTimeMillis
  compressedDataset.map(chunk => chunk.getColumn("0").selectLessThan(x.value).cardinality).reduce((a,b) => a + b)
  end = System.currentTimeMillis
  time = (end - start) / 1000.00
  timeList.add("distribution_" + distribution + "_cardinality_" + cardinality + "_hybridcolumnar_best" + "_selectivity_" + selectivity +  " (count) time: " + time);

  start = System.currentTimeMillis
  compressedDataset.map(chunk => {val column = chunk.getColumn("0"); val bitmap = column.selectLessThan(x.value); column.sum(bitmap)}).reduce((a,b) => a + b)
  end = System.currentTimeMillis
  time = (end - start) / 1000.00
  timeList.add("distribution_" + distribution + "_cardinality_" + cardinality + "_hybridcolumnar_best" + "_selectivity_" + selectivity +  " (sum) time: " + time);

}

var start = System.currentTimeMillis
compressedDataset.map(chunk => {val column = chunk.getColumn("0"); column.sum}).reduce((a,b) => a + b)
var end = System.currentTimeMillis
var time = (end - start) / 1000.00
timeList.add("distribution_" + distribution + "_cardinality_" + cardinality + "_hybridcolumnar_best" + " (sum total) time: " + time);

start = System.currentTimeMillis
compressedDataset.flatMap(chunk => chunk).map(row => (row.get("0").toString.toInt, row.getRunLength)).reduceByKey((a,b) => a + b).collect
end = System.currentTimeMillis
time = (end - start) / 1000.00
timeList.add("distribution_" + distribution + "_cardinality_" + cardinality + "_hybridcolumnar_best" + " (group by) time: " + time);
val timeList = new ArrayList[String]

sc.parallelize(timeList).saveAsTextFile(raw"hdfs://gkir-1:9000/compareDatasetTime.csv")
