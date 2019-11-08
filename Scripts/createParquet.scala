import org.george.hybridcolumnar._, bitpacking._, delta._, chunk._, column._, domain._, roaring._, util._
import scala.collection.JavaConversions._
import java.util.ArrayList
import java.util.HashMap

var distribution = ""
var numberOfRows = 1000000000; // 1 Billion rows
var numberOfPartitions = 2560;

for (distribution <- List("uniform", "normal", "exponential1", "exponential2")) {

  var cardinality = 0
  for (cardinality <- List(10, 100, 1000, 10000)) {
    
    val text = sc.textFile(raw"hdfs://gkir-1:9000/randomNumbers_size_1B_distribution_" + distribution + "_cardinality_" + cardinality + ".csv", numberOfPartitions)
    val dataset = importDataset(text, List(0), List(Datatype.INTEGER))
    val datasetSorted = sortDataset(dataset)
    datasetSorted.map(a => a.get(0).toString.toInt).toDF.write.parquet(raw"hdfs://gkir-1:9000/randomNumbers_size_1B_distribution_" + distribution + "_cardinality_" + cardinality + ".parquet")

  }
  
}
