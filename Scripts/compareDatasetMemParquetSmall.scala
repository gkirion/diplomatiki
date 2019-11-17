import org.george.hybridcolumnar._, bitpacking._, chunk._, column._, domain._, roaring._, util._
import scala.collection.JavaConversions._
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.HashMap
import org.apache.spark.mllib.random.RandomRDDs._

var distribution = ""
var numberOfRows = 100000000; // 100 Million rows
var numberOfPartitions = 256;
val list = new ArrayList[String]
for (distribution <- List("uniform", "normal", "exponential1", "exponential2")) {

  var cardinality = 0
  for (cardinality <- List(10, 100, 1000, 10000)) {

    println("distribution_" + distribution + "_cardinality_" + cardinality)
    val parquet = spark.read.parquet(raw"hdfs://gkir-1:9000/randomNumbers_size_100M_distribution_" + distribution + "_cardinality_" + cardinality + ".parquet")
    parquet.persist
    parquet.count
    val size = Math.round(sc.getRDDStorageInfo(0).memSize / (1024*1024.0) * (sc.getRDDStorageInfo(0).numPartitions / (sc.getRDDStorageInfo(0).numCachedPartitions * 1.0)) * 100) / 100.0
    val percentCached = Math.round((sc.getRDDStorageInfo(0).numCachedPartitions / (sc.getRDDStorageInfo(0).numPartitions * 1.0)) * 100)
    list.add("distribution_" + distribution + "_cardinality_" + cardinality + " size: " + size + " percent cached: " + percentCached)
    parquet.unpersist(true)
      
  }

}
sc.parallelize(list).saveAsTextFile(raw"hdfs://gkir-1:9000/compareDatasetMemParquetSmall.csv")
