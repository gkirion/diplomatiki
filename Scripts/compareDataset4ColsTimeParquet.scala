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

    println("distribution_" + distribution + "_cardinality_" + cardinality)
    val parquet = spark.read.parquet(raw"hdfs://gkir-1:9000/randomNumbers_size_1B_4cols_distribution_" + distribution + "_cardinality_" + cardinality + ".parquet")
    parquet.persist
    parquet.count

    var x = sc.broadcast((cardinality * 0.1).toInt)
    var start = System.currentTimeMillis
    parquet.filter(col("value") < x.value).rdd.map(a => a(0).asInstanceOf[Int]).reduce((a,b) => a + b)
    var end = System.currentTimeMillis
    var time = (end - start) / 1000.00
    list.add("distribution_" + distribution + "_cardinality_" + cardinality + "_parquet" + " second col select sum selectivity 10% time: "  + time)

    x = sc.broadcast((cardinality * 0.75).toInt)
    start = System.currentTimeMillis
    parquet.filter(col("value") < x.value).rdd.map(a => a(0).asInstanceOf[Int]).reduce((a,b) => a + b)
    end = System.currentTimeMillis
    time = (end - start) / 1000.00
    list.add("distribution_" + distribution + "_cardinality_" + cardinality + "_parquet" + " second col select sum selectivity 75% time: "  + time)

    start = System.currentTimeMillis
    parquet.rdd.map(a => (a(0).asInstanceOf[Int], 1)).reduceByKey((a,b) => a + b).collect
    end = System.currentTimeMillis
    time = (end - start) / 1000.00
    list.add("distribution_" + distribution + "_cardinality_" + cardinality + "_parquet" + " second col group by time: "  + time)

    parquet.unpersist(true)
        
    

}
sc.parallelize(list).saveAsTextFile(raw"hdfs://gkir-1:9000/compareDatasetTimeSmallMemParquet.csv")
