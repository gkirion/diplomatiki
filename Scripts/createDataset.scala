import org.apache.spark.mllib.random.RandomRDDs._

var distribution = ""
var numberOfRows = 1000000000; // 1 Billion rows
var numberOfPartitions = 2560;
for (distribution <- List("uniform", "normal", "exponential1", "exponential2")) {
  var numbers = {
    if (distribution.equals("uniform")) {
      uniformRDD(sc, numberOfRows, numberOfPartitions) 
    }
    else if (distribution.equals("normal")) {
      val nums = normalRDD(sc, numberOfRows, numberOfPartitions)
      // move distribution from N(0,1) to positive
      val nmin = nums.min
      val nminBc = sc.broadcast(nmin)
      nums.map(a => a - nminBc.value)
    }
    else if (distribution.equals("exponential1")) {
      exponentialRDD(sc, 2, numberOfRows, numberOfPartitions)
    }
    else {
      exponentialRDD(sc, 0.5, numberOfRows, numberOfPartitions)
    }
  }
  var cardinality = 0
  for (cardinality <- List(10, 100, 1000, 10000)) {
    // take floats and create a list of integers
    val quantum = sc.broadcast((numbers.max - numbers.min) / cardinality)
    val inumbers = numbers.map(a => (a / quantum.value).toInt)
    inumbers.saveAsTextFile("hdfs://gkir-1:9000/randomNumbers_size_1B_distribution_" + distribution + "_cardinality_" + cardinality + ".csv")
  }
}
