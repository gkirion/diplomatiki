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

val text = sc.textFile(raw"hdfs://gkir-1:9000/randomNumbers_size_1B_multiple_cardinalities" + ".csv", numberOfPartitions)
var dataset = importDataset(text, List(0), List(Datatype.INTEGER))
val datasetSorted = sortDataset(dataset)
val encoding = null
for (encoding <-  List(ColumnType.RLE, ColumnType.BIT_PACKING, ColumnType.DELTA, ColumnType.ROARING)) {

    println("encoding_" + encoding)
    val compressedDataset = compressDataset(datasetSorted, null, List(encoding))
    compressedDataset.persist
    compressedDataset.count
    val size = Math.round(sc.getRDDStorageInfo(0).memSize / (1024*1024.0) * (sc.getRDDStorageInfo(0).numPartitions / (sc.getRDDStorageInfo(0).numCachedPartitions * 1.0)) * 100) / 100.0
    val percentCached = Math.round((sc.getRDDStorageInfo(0).numCachedPartitions / (sc.getRDDStorageInfo(0).numPartitions * 1.0)) * 100)
    list.add("encoding_" + encoding + " size: " + size + " percent cached: " + percentCached)
    compressedDataset.unpersist(true)  
      
}
println("hybridcolumnar_best")
val compressedDataset = compressDataset(datasetSorted)
compressedDataset.persist
compressedDataset.count
var size = Math.round(sc.getRDDStorageInfo(0).memSize / (1024*1024.0) * (sc.getRDDStorageInfo(0).numPartitions / (sc.getRDDStorageInfo(0).numCachedPartitions * 1.0)) * 100) / 100.0
var percentCached = Math.round((sc.getRDDStorageInfo(0).numCachedPartitions / (sc.getRDDStorageInfo(0).numPartitions * 1.0)) * 100)
list.add("hybridcolumnar_best" + " size: " + size + " percent cached: " + percentCached)
compressedDataset.unpersist(true)

println("parquet")
val parquet = spark.read.parquet(raw"hdfs://gkir-1:9000/randomNumbers_size_1B_multiple_cardinalities" + ".parquet")
parquet.persist
parquet.count
size = Math.round(sc.getRDDStorageInfo(0).memSize / (1024*1024.0) * (sc.getRDDStorageInfo(0).numPartitions / (sc.getRDDStorageInfo(0).numCachedPartitions * 1.0)) * 100) / 100.0
percentCached = Math.round((sc.getRDDStorageInfo(0).numCachedPartitions / (sc.getRDDStorageInfo(0).numPartitions * 1.0)) * 100)
list.add("parquet" + " size: " + size + " percent cached: " + percentCached)
parquet.unpersist(true)

println("uncompressed")
dataset = text.map(a => a.toInt)
dataset.persist
dataset.count
size = Math.round(sc.getRDDStorageInfo(0).memSize / (1024*1024.0) * (sc.getRDDStorageInfo(0).numPartitions / (sc.getRDDStorageInfo(0).numCachedPartitions * 1.0)) * 100) / 100.0
percentCached = Math.round((sc.getRDDStorageInfo(0).numCachedPartitions / (sc.getRDDStorageInfo(0).numPartitions * 1.0)) * 100)
list.add("uncompressed" + " size: " + size + " percent cached: " + percentCached)
dataset.unpersist(true)

sc.parallelize(list).saveAsTextFile(raw"hdfs://gkir-1:9000/compareRowMultipleCardinalitiesMem.csv")
