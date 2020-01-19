import org.george.hybridcolumnar._, bitpacking._, delta._, chunk._, column._, domain._, roaring._, util._
import scala.collection.JavaConversions._
import java.util.ArrayList
import java.util.HashMap
import scala.util.Random

var distribution = ""
var numberOfRows = 1000000000; // 1 Billion rows
var numberOfPartitions = 2560;
val r = sc.broadcast(new java.util.Random)

for (distribution <- List("uniform", "normal", "exponential1", "exponential2")) {

    val text = sc.textFile(raw"hdfs://gkir-1:9000/randomNumbers_size_1B_distribution_" + distribution + "_cardinality_" + 10 + ".csv", numberOfPartitions)
    val second = text.mapPartitions(part => {val list = new ArrayList[String]; part.foreach(item => list.add(item)); Random.shuffle(list.toList).iterator})
    val third = text.mapPartitions(part => {val list = new ArrayList[String]; part.foreach(item => list.add(item)); Random.shuffle(list.toList).iterator})
    val fourth = text.mapPartitions(part => {val list = new ArrayList[String]; part.foreach(item => list.add(item)); Random.shuffle(list.toList).iterator})
    text.zip(second).zip(third).zip(fourth).map(a => (a._1._1._1, a._1._1._2, a._1._2, a._2)).saveAsTextFile("hdfs://gkir-1:9000/randomNumbers_size_1B_4cols_distribution_" + distribution + "_cardinality_" + 10 + ".csv")
  
}
