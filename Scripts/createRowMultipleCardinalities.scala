import org.apache.spark.mllib.random.RandomRDDs._

var distribution = ""
var numberOfRows = 1000000000; // 1 Billion rows
var numberOfPartitions = 2560;

var numbers = uniformRDD(sc, numberOfRows, numberOfPartitions) 
var cardinality = 0
// take floats and create a list of integers
val range = sc.broadcast(numbers.max - numbers.min)
val inumbers = numbers.zipWithIndex.map(a => {

    var cardinality = {
        if (a._2 / 200000000 == 0) {
            10
        } else if (a._2 / 200000000 == 1) {
            100
        } else if (a._2 / 200000000 == 2) {
            1000
        } else if (a._2 / 200000000 == 3) {
            10000
        } else {
            100000
        }
    }

    (a._1 / (range.value / cardinality)).toInt
})
inumbers.saveAsTextFile("hdfs://gkir-1:9000/randomNumbers_size_1B_multiple_cardinalities" + ".csv")
