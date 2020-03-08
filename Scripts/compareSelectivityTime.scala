import org.george.hybridcolumnar._, bitpacking._, chunk._, column._, domain._, roaring._, util._
import scala.collection.JavaConversions._
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.HashMap
import org.apache.spark.mllib.random.RandomRDDs._

var distribution = ""
var numberOfRows = 1000000000; // 1 Billion rows
var numberOfPartitions = 2560;
val list = new ArrayList[String]

val text = sc.textFile(raw"hdfs://gkir-1:9000/randomNumbers_size_1B_uniform_cardinality_100" + ".csv", numberOfPartitions)
var dataset = importDataset(text, List(0), List(Datatype.INTEGER))
val datasetSorted = sortDataset(dataset)
val encoding = null
for (encoding <-  List(ColumnType.RLE, ColumnType.BIT_PACKING, ColumnType.DELTA, ColumnType.ROARING)) {

    println("encoding_" + encoding)
    val compressedDataset = compressDataset(datasetSorted, null, List(encoding)).repartition(256)
    compressedDataset.persist
    compressedDataset.count
    for (selectivity <- List(0.1, 0.25, 0.50, 0.75, 1.0)) {
        println("selectivity: " + selectivity)
        var x = sc.broadcast((100 * selectivity).toInt)
        var start = System.currentTimeMillis
        compressedDataset.map(chunk => {val column = chunk.getColumn("0"); val bitmap = column.selectLessThan(x.value); column.sum(bitmap)}).reduce((a,b) => a + b)
        var end = System.currentTimeMillis
        var time = (end - start) / 1000.00
        list.add("encoding_" + encoding + " select sum selectivity " + selectivity * 100 +"% time: "  + time)
    }
    compressedDataset.unpersist(true)  
      
}

println("hybridcolumnar_best")
val compressedDataset = compressDataset(datasetSorted).repartition(256)
compressedDataset.persist
compressedDataset.count
for (selectivity <- List(0.1, 0.25, 0.50, 0.75, 1.0)) {
    println("selectivity: " + selectivity)
    var x = sc.broadcast((100 * selectivity).toInt)
    var start = System.currentTimeMillis
    compressedDataset.map(chunk => {val column = chunk.getColumn("0"); val bitmap = column.selectLessThan(x.value); column.sum(bitmap)}).reduce((a,b) => a + b)
    var end = System.currentTimeMillis
    var time = (end - start) / 1000.00
    list.add("hybridcolumnar_best select sum selectivity " + selectivity * 100 +"% time: "  + time)
}
compressedDataset.unpersist(true)

println("parquet")
val parquet = spark.read.parquet(raw"hdfs://gkir-1:9000/randomNumbers_size_1B_uniform_cardinality_100" + ".parquet")
parquet.persist
parquet.count
for (selectivity <- List(0.1, 0.25, 0.50, 0.75, 1.0)) {
    println("selectivity: " + selectivity)
    var x = sc.broadcast((100 * selectivity).toInt)
    var start = System.currentTimeMillis
    parquet.filter(col("value") < x.value).rdd.map(a => a(0).asInstanceOf[Int]).reduce((a,b) => a + b)
    var end = System.currentTimeMillis
    var time = (end - start) / 1000.00
    list.add("parquet select sum selectivity " + selectivity * 100 +"% time: "  + time)
}
parquet.unpersist(true)

println("uncompressed")
val uncompressedDataset = text.map(a => a.toInt).repartition(256)
uncompressedDataset.persist
uncompressedDataset.count
for (selectivity <- List(0.1, 0.25, 0.50, 0.75, 1.0)) {
    println("selectivity: " + selectivity)
    var x = sc.broadcast((100 * selectivity).toInt)
    var start = System.currentTimeMillis
    uncompressedDataset.filter(a => a < x.value).map(a => a.toLong).reduce((a,b) => a + b)
    var end = System.currentTimeMillis
    var time = (end - start) / 1000.00
    list.add("uncompressed select sum selectivity " + selectivity * 100 +"% time: "  + time)
}
uncompressedDataset.unpersist(true)

sc.parallelize(list).saveAsTextFile(raw"hdfs://gkir-1:9000/compareSelectivityTime.csv")
