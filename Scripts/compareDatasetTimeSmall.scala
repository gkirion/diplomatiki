import org.george.hybridcolumnar._, bitpacking._, chunk._, column._, domain._, roaring._, util._
import scala.collection.JavaConversions._
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.HashMap
import org.apache.spark.mllib.random.RandomRDDs._

var distribution = ""
var numberOfRows = 100000000; // 100 Million rows
var numberOfPartitions = 256;

for (distribution <- List("uniform", "normal", "exponential1", "exponential2")) {
  val list = new ArrayList[String]
  var cardinality = 0
  for (cardinality <- List(10, 100, 1000, 10000)) {
    
    val text = sc.textFile(raw"hdfs://gkir-1:9000/randomNumbers_size_100M_distribution_" + distribution + "_cardinality_" + cardinality + ".csv", numberOfPartitions)
    val dataset = importDataset(text, List(0), List(Datatype.INTEGER))
    val datasetSorted = sortDataset(dataset)
    val encoding = null
    for (encoding <-  List(ColumnType.PLAIN, ColumnType.RLE, ColumnType.BIT_PACKING, ColumnType.DELTA, ColumnType.BITMAP, ColumnType.ROARING)) {

      if (encoding != ColumnType.BITMAP || cardinality <= 1000) {
        println("distribution_" + distribution + "_cardinality_" + cardinality + "_encoding_" + encoding)
        val compressedDataset = compressDataset(datasetSorted, null, List(encoding))
        compressedDataset.persist
        compressedDataset.count

        var x = sc.broadcast((cardinality * 0.1).toInt)
        var start = System.currentTimeMillis
        compressedDataset.map(chunk => {val column = chunk.getColumn("0"); val bitmap = column.selectLessThan(x.value); column.sum(bitmap)}).reduce((a,b) => a + b)
        var end = System.currentTimeMillis
        var time = (end - start) / 1000.00
        list.add("distribution_" + distribution + "_cardinality_" + cardinality + "_encoding_" + encoding + " select sum selectivity 10% time: "  + time)

        x = sc.broadcast((cardinality * 0.75).toInt)
        start = System.currentTimeMillis
        compressedDataset.map(chunk => {val column = chunk.getColumn("0"); val bitmap = column.selectLessThan(x.value); column.sum(bitmap)}).reduce((a,b) => a + b)
        end = System.currentTimeMillis
        time = (end - start) / 1000.00
        list.add("distribution_" + distribution + "_cardinality_" + cardinality + "_encoding_" + encoding + " select sum selectivity 75% time: "  + time)

        start = System.currentTimeMillis
        compressedDataset.flatMap(chunk => chunk).map(row => (row.get("0").toString.toInt, row.getRunLength)).reduceByKey((a,b) => a + b).collect
        end = System.currentTimeMillis
        time = (end - start) / 1000.00
        list.add("distribution_" + distribution + "_cardinality_" + cardinality + "_encoding_" + encoding + " group by time: "  + time)

        compressedDataset.unpersist(true)
      }
      
    }
    println("distribution_" + distribution + "_cardinality_" + cardinality + "_hybridcolumnar_best")
    val compressedDataset = compressDataset(datasetSorted)
    compressedDataset.persist
    compressedDataset.count

    var x = sc.broadcast((cardinality * 0.1).toInt)
    var start = System.currentTimeMillis
    compressedDataset.map(chunk => {val column = chunk.getColumn("0"); val bitmap = column.selectLessThan(x.value); column.sum(bitmap)}).reduce((a,b) => a + b)
    var end = System.currentTimeMillis
    var time = (end - start) / 1000.00
    list.add("distribution_" + distribution + "_cardinality_" + cardinality + "_hybridcolumnar_best" + " select sum selectivity 10% time: "  + time)

    x = sc.broadcast((cardinality * 0.75).toInt)
    start = System.currentTimeMillis
    compressedDataset.map(chunk => {val column = chunk.getColumn("0"); val bitmap = column.selectLessThan(x.value); column.sum(bitmap)}).reduce((a,b) => a + b)
    end = System.currentTimeMillis
    time = (end - start) / 1000.00
    list.add("distribution_" + distribution + "_cardinality_" + cardinality + "_hybridcolumnar_best" + " select sum selectivity 75% time: "  + time)

    start = System.currentTimeMillis
    compressedDataset.flatMap(chunk => chunk).map(row => (row.get("0").toString.toInt, row.getRunLength)).reduceByKey((a,b) => a + b).collect
    end = System.currentTimeMillis
    time = (end - start) / 1000.00
    list.add("distribution_" + distribution + "_cardinality_" + cardinality + "_hybridcolumnar_best" + " group by time: "  + time)
    
    compressedDataset.unpersist(true)
    
  }
  sc.parallelize(list).saveAsTextFile(raw"hdfs://gkir-1:9000/compareDatasetTimeSmall" + "_distribution_" + distribution + ".csv")
}
