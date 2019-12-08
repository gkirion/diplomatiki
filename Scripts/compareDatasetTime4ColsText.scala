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

    val text = sc.textFile(raw"hdfs://gkir-1:9000/randomNumbers_size_1B_4cols_distribution_" + distribution + "_cardinality_" + cardinality + ".csv", numberOfPartitions)
    println("distribution_" + distribution + "_cardinality_" + cardinality + "_uncompressed second col")
    var dataset = text.map(a => a.stripPrefix("(").stripSuffix(")").split(",")(1)).map(a => a.toInt).repartition(256)
    dataset.persist
    dataset.count

    var size = Math.round(sc.getRDDStorageInfo(0).memSize / (1024*1024.0) * (sc.getRDDStorageInfo(0).numPartitions / (sc.getRDDStorageInfo(0).numCachedPartitions * 1.0)) * 100) / 100.0
    var percentCached = Math.round((sc.getRDDStorageInfo(0).numCachedPartitions / (sc.getRDDStorageInfo(0).numPartitions * 1.0)) * 100)
    list.add("distribution_" + distribution + "_cardinality_" + cardinality + "_uncompressed second col" + " size: " + size + " percent cached: " + percentCached)

    var x = sc.broadcast((cardinality * 0.1).toInt)
    var start = System.currentTimeMillis
    dataset.filter(a => a < x.value).map(a => a.toLong).reduce((a,b) => a + b)
    var end = System.currentTimeMillis
    var time = (end - start) / 1000.00
    list.add("distribution_" + distribution + "_cardinality_" + cardinality + "_uncompressed second col" + " select sum selectivity 10% time: "  + time)

    x = sc.broadcast((cardinality * 0.75).toInt)
    start = System.currentTimeMillis
    dataset.filter(a => a < x.value).map(a => a.toLong).reduce((a,b) => a + b)
    end = System.currentTimeMillis
    time = (end - start) / 1000.00
    list.add("distribution_" + distribution + "_cardinality_" + cardinality + "_uncompressed second col" + " select sum selectivity 75% time: "  + time)

    start = System.currentTimeMillis
    dataset.map(a => (a, 1)).reduceByKey((a,b) => a + b).collect
    end = System.currentTimeMillis
    time = (end - start) / 1000.00
    list.add("distribution_" + distribution + "_cardinality_" + cardinality + "_uncompressed second col" + " group by time: "  + time)

    dataset.unpersist(true)

    println("distribution_" + distribution + "_cardinality_" + cardinality + "_uncompressed third col")
    dataset = text.map(a => a.stripPrefix("(").stripSuffix(")").split(",")(2)).map(a => a.toInt).repartition(256)
    dataset.persist
    dataset.count

    size = Math.round(sc.getRDDStorageInfo(0).memSize / (1024*1024.0) * (sc.getRDDStorageInfo(0).numPartitions / (sc.getRDDStorageInfo(0).numCachedPartitions * 1.0)) * 100) / 100.0
    percentCached = Math.round((sc.getRDDStorageInfo(0).numCachedPartitions / (sc.getRDDStorageInfo(0).numPartitions * 1.0)) * 100)
    list.add("distribution_" + distribution + "_cardinality_" + cardinality + "_uncompressed third col" + " size: " + size + " percent cached: " + percentCached)

    x = sc.broadcast((cardinality * 0.1).toInt)
    start = System.currentTimeMillis
    dataset.filter(a => a < x.value).map(a => a.toLong).reduce((a,b) => a + b)
    end = System.currentTimeMillis
    time = (end - start) / 1000.00
    list.add("distribution_" + distribution + "_cardinality_" + cardinality + "_uncompressed third col" + " select sum selectivity 10% time: "  + time)

    x = sc.broadcast((cardinality * 0.75).toInt)
    start = System.currentTimeMillis
    dataset.filter(a => a < x.value).map(a => a.toLong).reduce((a,b) => a + b)
    end = System.currentTimeMillis
    time = (end - start) / 1000.00
    list.add("distribution_" + distribution + "_cardinality_" + cardinality + "_uncompressed third col" + " select sum selectivity 75% time: "  + time)

    start = System.currentTimeMillis
    dataset.map(a => (a, 1)).reduceByKey((a,b) => a + b).collect
    end = System.currentTimeMillis
    time = (end - start) / 1000.00
    list.add("distribution_" + distribution + "_cardinality_" + cardinality + "_uncompressed third col" + " group by time: "  + time)

    dataset.unpersist(true)

    println("distribution_" + distribution + "_cardinality_" + cardinality + "_uncompressed fourth col")
    dataset = text.map(a => a.stripPrefix("(").stripSuffix(")").split(",")(3)).map(a => a.toInt).repartition(256)
    dataset.persist
    dataset.count

    size = Math.round(sc.getRDDStorageInfo(0).memSize / (1024*1024.0) * (sc.getRDDStorageInfo(0).numPartitions / (sc.getRDDStorageInfo(0).numCachedPartitions * 1.0)) * 100) / 100.0
    percentCached = Math.round((sc.getRDDStorageInfo(0).numCachedPartitions / (sc.getRDDStorageInfo(0).numPartitions * 1.0)) * 100)
    list.add("distribution_" + distribution + "_cardinality_" + cardinality + "_uncompressed fourth col" + " size: " + size + " percent cached: " + percentCached)

    x = sc.broadcast((cardinality * 0.1).toInt)
    start = System.currentTimeMillis
    dataset.filter(a => a < x.value).map(a => a.toLong).reduce((a,b) => a + b)
    end = System.currentTimeMillis
    time = (end - start) / 1000.00
    list.add("distribution_" + distribution + "_cardinality_" + cardinality + "_uncompressed fourth col" + " select sum selectivity 10% time: "  + time)

    x = sc.broadcast((cardinality * 0.75).toInt)
    start = System.currentTimeMillis
    dataset.filter(a => a < x.value).map(a => a.toLong).reduce((a,b) => a + b)
    end = System.currentTimeMillis
    time = (end - start) / 1000.00
    list.add("distribution_" + distribution + "_cardinality_" + cardinality + "_uncompressed fourth col" + " select sum selectivity 75% time: "  + time)

    start = System.currentTimeMillis
    dataset.map(a => (a, 1)).reduceByKey((a,b) => a + b).collect
    end = System.currentTimeMillis
    time = (end - start) / 1000.00
    list.add("distribution_" + distribution + "_cardinality_" + cardinality + "_uncompressed fourth col" + " group by time: "  + time)

    dataset.unpersist(true)

}
sc.parallelize(list).saveAsTextFile(raw"hdfs://gkir-1:9000/compareDatasetTime4ColsText.csv")
