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
for (distribution <- List("uniform", "normal", "exponential1", "exponential2")) {

  var cardinality = 0
  for (cardinality <- List(10, 100, 1000, 10000)) {
    
    val text = sc.textFile(raw"D:\\randomNumbers_size_1B_distribution_" + distribution + "_cardinality_" + cardinality + ".csv", numberOfPartitions)
    val dataset = importDataset(text, List(0), List(Datatype.INTEGER))
    val datasetSorted = sortDataset(dataset)
    val encoding = null
    for (encoding <-  List(ColumnType.PLAIN, ColumnType.RLE, ColumnType.BIT_PACKING, ColumnType.DELTA, ColumnType.BITMAP, ColumnType.ROARING)) {

      if (encoding != ColumnType.BITMAP || cardinality <= 1000) {
        println("distribution_" + distribution + "_cardinality_" + cardinality + "_encoding_" + encoding)
        val compressedDataset = compressDataset(datasetSorted, null, List(encoding))
        compressedDataset.persist
        compressedDataset.count
        val size = Math.round(sc.getRDDStorageInfo(0).memSize / (1024*1024.0) * (sc.getRDDStorageInfo(0).numPartitions / (sc.getRDDStorageInfo(0).numCachedPartitions * 1.0)) * 100) / 100.0
        list.add("distribution_" + distribution + "_cardinality_" + cardinality + "_encoding_" + encoding + " size: " + size)
        compressedDataset.unpersist(true)
      }
      
    }

  }
}
sc.parallelize(list).saveAsTextFile(raw"D:\\compareDatasetMem.csv")
