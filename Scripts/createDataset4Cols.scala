import org.george.hybridcolumnar._, bitpacking._, delta._, chunk._, column._, domain._, roaring._, util._
import scala.collection.JavaConversions._
import java.util.ArrayList
import java.util.HashMap

var distribution = ""
var numberOfRows = 1000000000; // 1 Billion rows
var numberOfPartitions = 2560;
val r = sc.broadcast(new java.util.Random)

for (distribution <- List("uniform", "normal", "exponential1", "exponential2")) {

    val text = sc.textFile(raw"hdfs://gkir-1:9000/randomNumbers_size_1B_distribution_" + distribution + "_cardinality_" + 10 + ".csv", numberOfPartitions)
    text.map(a => (a,r.value.nextInt(10),r.value.nextInt(10),r.value.nextInt(10))).saveAsTextFile("hdfs://gkir-1:9000/randomNumbers_size_1B_4cols_distribution_" + distribution + "_cardinality_" + 10 + ".csv")
  
}
