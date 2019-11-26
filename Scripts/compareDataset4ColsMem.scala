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

  var cardinality = 10
    
    val text = sc.textFile(raw"hdfs://gkir-1:9000/randomNumbers_size_1B_4cols_distribution_" + distribution + "_cardinality_" + 10 + ".csv", numberOfPartitions)
    val dataset = importDataset(text, List(0,1,2,3), List(Datatype.INTEGER,Datatype.INTEGER,Datatype.INTEGER,Datatype.INTEGER))
    val datasetSorted = sortDataset(dataset)
    val encoding = null
    for (encoding <-  List(ColumnType.RLE, ColumnType.BIT_PACKING, ColumnType.DELTA, ColumnType.ROARING)) {

        println("distribution_" + distribution + "_cardinality_" + cardinality + "_encoding_" + encoding + " second col")
        var singleCol = datasetSorted.map(a => {val l = new RowArray; l.add(a.get(1)); l.setRunLength(a.getRunLength); l})
        var compressedDataset = compressDataset(singleCol, null, List(encoding)).repartition(256)
        compressedDataset.persist
        compressedDataset.count
        var size = Math.round(sc.getRDDStorageInfo(0).memSize / (1024*1024.0) * (sc.getRDDStorageInfo(0).numPartitions / (sc.getRDDStorageInfo(0).numCachedPartitions * 1.0)) * 100) / 100.0
        var percentCached = Math.round((sc.getRDDStorageInfo(0).numCachedPartitions / (sc.getRDDStorageInfo(0).numPartitions * 1.0)) * 100)
        list.add("distribution_" + distribution + "_cardinality_" + cardinality + "_encoding_" + encoding + " second col size: " + size + " percent cached: " + percentCached)
        compressedDataset.unpersist(true)

        println("distribution_" + distribution + "_cardinality_" + cardinality + "_encoding_" + encoding + " third col")
        singleCol = datasetSorted.map(a => {val l = new RowArray; l.add(a.get(2)); l.setRunLength(a.getRunLength); l})
        compressedDataset = compressDataset(singleCol, null, List(encoding)).repartition(256)
        compressedDataset.persist
        compressedDataset.count
        size = Math.round(sc.getRDDStorageInfo(0).memSize / (1024*1024.0) * (sc.getRDDStorageInfo(0).numPartitions / (sc.getRDDStorageInfo(0).numCachedPartitions * 1.0)) * 100) / 100.0
        percentCached = Math.round((sc.getRDDStorageInfo(0).numCachedPartitions / (sc.getRDDStorageInfo(0).numPartitions * 1.0)) * 100)
        list.add("distribution_" + distribution + "_cardinality_" + cardinality + "_encoding_" + encoding + " third col size: " + size + " percent cached: " + percentCached)
        compressedDataset.unpersist(true)

        println("distribution_" + distribution + "_cardinality_" + cardinality + "_encoding_" + encoding + " fourth col")
        singleCol = datasetSorted.map(a => {val l = new RowArray; l.add(a.get(3)); l.setRunLength(a.getRunLength); l})
        compressedDataset = compressDataset(singleCol, null, List(encoding)).repartition(256)
        compressedDataset.persist
        compressedDataset.count
        size = Math.round(sc.getRDDStorageInfo(0).memSize / (1024*1024.0) * (sc.getRDDStorageInfo(0).numPartitions / (sc.getRDDStorageInfo(0).numCachedPartitions * 1.0)) * 100) / 100.0
        percentCached = Math.round((sc.getRDDStorageInfo(0).numCachedPartitions / (sc.getRDDStorageInfo(0).numPartitions * 1.0)) * 100)
        list.add("distribution_" + distribution + "_cardinality_" + cardinality + "_encoding_" + encoding + " fourth col size: " + size + " percent cached: " + percentCached)
        compressedDataset.unpersist(true)
      
    }
    println("distribution_" + distribution + "_cardinality_" + cardinality + "_hybridcolumnar_best second col")
    var singleCol = datasetSorted.map(a => {val l = new RowArray; l.add(a.get(1)); l.setRunLength(a.getRunLength); l})
    var compressedDataset = compressDataset(singleCol).repartition(256)
    compressedDataset.persist
    compressedDataset.count
    var size = Math.round(sc.getRDDStorageInfo(0).memSize / (1024*1024.0) * (sc.getRDDStorageInfo(0).numPartitions / (sc.getRDDStorageInfo(0).numCachedPartitions * 1.0)) * 100) / 100.0
    var percentCached = Math.round((sc.getRDDStorageInfo(0).numCachedPartitions / (sc.getRDDStorageInfo(0).numPartitions * 1.0)) * 100)
    list.add("distribution_" + distribution + "_cardinality_" + cardinality + "_hybridcolumnar_best second col" + " size: " + size + " percent cached: " + percentCached)
    compressedDataset.unpersist(true)

    println("distribution_" + distribution + "_cardinality_" + cardinality + "_hybridcolumnar_best third col")
    singleCol = datasetSorted.map(a => {val l = new RowArray; l.add(a.get(2)); l.setRunLength(a.getRunLength); l})
    compressedDataset = compressDataset(singleCol).repartition(256)
    compressedDataset.persist
    compressedDataset.count
    size = Math.round(sc.getRDDStorageInfo(0).memSize / (1024*1024.0) * (sc.getRDDStorageInfo(0).numPartitions / (sc.getRDDStorageInfo(0).numCachedPartitions * 1.0)) * 100) / 100.0
    percentCached = Math.round((sc.getRDDStorageInfo(0).numCachedPartitions / (sc.getRDDStorageInfo(0).numPartitions * 1.0)) * 100)
    list.add("distribution_" + distribution + "_cardinality_" + cardinality + "_hybridcolumnar_best third col" + " size: " + size + " percent cached: " + percentCached)
    compressedDataset.unpersist(true)

    println("distribution_" + distribution + "_cardinality_" + cardinality + "_hybridcolumnar_best fourth col")
    singleCol = datasetSorted.map(a => {val l = new RowArray; l.add(a.get(3)); l.setRunLength(a.getRunLength); l})
    compressedDataset = compressDataset(singleCol).repartition(256)
    compressedDataset.persist
    compressedDataset.count
    size = Math.round(sc.getRDDStorageInfo(0).memSize / (1024*1024.0) * (sc.getRDDStorageInfo(0).numPartitions / (sc.getRDDStorageInfo(0).numCachedPartitions * 1.0)) * 100) / 100.0
    percentCached = Math.round((sc.getRDDStorageInfo(0).numCachedPartitions / (sc.getRDDStorageInfo(0).numPartitions * 1.0)) * 100)
    list.add("distribution_" + distribution + "_cardinality_" + cardinality + "_hybridcolumnar_best fourth col" + " size: " + size + " percent cached: " + percentCached)
    compressedDataset.unpersist(true)
    
}
sc.parallelize(list).saveAsTextFile(raw"hdfs://gkir-1:9000/compareDataset4ColsMem.csv")
