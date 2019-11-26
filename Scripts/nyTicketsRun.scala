import org.george.hybridcolumnar._, bitpacking._, chunk._, column._, domain._, roaring._, util._
import scala.collection.JavaConversions._
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.HashMap

val text = sc.textFile("hdfs://gkir-1:9000/Parking_Violations_Issued_-_Fiscal_Year_2017.csv")
val headers = text.first.split(",")
val dataset = importDatasetByNames(text, List("Registration State", "Plate Type", "Issue Date", "Vehicle Body Type", "Vehicle Make"), List(Datatype.STRING, Datatype.STRING, Datatype.DATE, Datatype.STRING, Datatype.STRING), headers.toList)
val filteredDataset = dataset.filter(a => !(a.get(0) == null || a.get(1) == null || a.get(2) == null || a.get(3) == null || a.get(4) == null))
val datasetSorted = sortDataset(filteredDataset)
val compressedDataset = compressDataset(datasetSorted, List("Registration State", "Plate Type", "Issue Date", "Vehicle Body Type", "Vehicle Make"))
compressedDataset.persist
compressedDataset.count

val list = new ArrayList[String]
var size = Math.round(sc.getRDDStorageInfo(0).memSize / (1024*1024.0) * (sc.getRDDStorageInfo(0).numPartitions / (sc.getRDDStorageInfo(0).numCachedPartitions * 1.0)) * 100) / 100.0
var percentCached = Math.round((sc.getRDDStorageInfo(0).numCachedPartitions / (sc.getRDDStorageInfo(0).numPartitions * 1.0)) * 100)
list.add("hybrid columnar size: " + size + " percent cached: " + percentCached)

var start = System.currentTimeMillis
filteredDataset.filter(a => a.get(0).equals("NY")).count
var end = System.currentTimeMillis
var time = (end - start) / 1000.00
list.add("map reduce count state NY time: "  + time)

start = System.currentTimeMillis
filteredDataset.filter(a => a.get(0).equals("CA")).count
end = System.currentTimeMillis
time = (end - start) / 1000.00
list.add("map reduce count state CA time: "  + time)

start = System.currentTimeMillis
filteredDataset.map(a => (a.get(0).toString, 1)).reduceByKey((a,b) => a + b).sortBy(a => a._2, false).take(10)
end = System.currentTimeMillis
time = (end - start) / 1000.00
list.add("map reduce group by state time: "  + time)

start = System.currentTimeMillis
filteredDataset.map(a => (a.get(4).toString, 1)).reduceByKey((a,b) => a + b).sortBy(a => a._2, false).take(10)
end = System.currentTimeMillis
time = (end - start) / 1000.00
list.add("map reduce group by vehicle make time: "  + time)

start = System.currentTimeMillis
filteredDataset.map(a => (a.get(1).toString, 1)).reduceByKey((a,b) => a + b).sortBy(a => a._2, false).take(10)
end = System.currentTimeMillis
time = (end - start) / 1000.00
list.add("map reduce group by plate type time: "  + time)

start = System.currentTimeMillis
filteredDataset.map(a => (a.get(3).toString, 1)).reduceByKey((a,b) => a + b).sortBy(a => a._2, false).take(10)
end = System.currentTimeMillis
time = (end - start) / 1000.00
list.add("map reduce group by vehicle body type time: "  + time)

start = System.currentTimeMillis
filteredDataset.map(a => (a.get(2).toString, 1)).reduceByKey((a,b) => a + b).sortBy(a => a._2, false).take(10)
end = System.currentTimeMillis
time = (end - start) / 1000.00
list.add("map reduce group by issue date time: "  + time)

start = System.currentTimeMillis
compressedDataset.map(chunk => chunk.getColumn("Registration State").selectEquals("NY").cardinality).reduce((a,b) => a + b)
end = System.currentTimeMillis
time = (end - start) / 1000.00
list.add("hybrid columnar count state NY time: "  + time)

start = System.currentTimeMillis
compressedDataset.map(chunk => chunk.getColumn("Registration State").selectEquals("CA").cardinality).reduce((a,b) => a + b)
end = System.currentTimeMillis
time = (end - start) / 1000.00
list.add("hybrid columnar count state CA time: "  + time)

start = System.currentTimeMillis
compressedDataset.flatMap(chunk => chunk.getColumn("Registration State")).map(a => (a.getFirst.toString, a.getSecond)).reduceByKey((a,b) => a + b).sortBy(a => a._2, false).take(10)
end = System.currentTimeMillis
time = (end - start) / 1000.00
list.add("hybrid columnar group by state time: "  + time)

start = System.currentTimeMillis
compressedDataset.flatMap(chunk => chunk.getColumn("Vehicle Make")).map(a => (a.getFirst.toString, a.getSecond)).reduceByKey((a,b) => a + b).sortBy(a => a._2, false).take(10)
end = System.currentTimeMillis
time = (end - start) / 1000.00
list.add("hybrid columnar group by vehicle make time: "  + time)

start = System.currentTimeMillis
compressedDataset.flatMap(chunk => chunk.getColumn("Plate Type")).map(a => (a.getFirst.toString, a.getSecond)).reduceByKey((a,b) => a + b).sortBy(a => a._2, false).take(10)
end = System.currentTimeMillis
time = (end - start) / 1000.00
list.add("hybrid columnar group by plate type time: "  + time)

start = System.currentTimeMillis
compressedDataset.flatMap(chunk => chunk.getColumn("Vehicle Body Type")).map(a => (a.getFirst.toString, a.getSecond)).reduceByKey((a,b) => a + b).sortBy(a => a._2, false).take(10)
end = System.currentTimeMillis
time = (end - start) / 1000.00
list.add("hybrid columnar group by vehicle body type time: "  + time)

start = System.currentTimeMillis
compressedDataset.flatMap(chunk => chunk.getColumn("Issue Date")).map(a => (a.getFirst.toString, a.getSecond)).reduceByKey((a,b) => a + b).sortBy(a => a._2, false).take(10)
end = System.currentTimeMillis
time = (end - start) / 1000.00
list.add("hybrid columnar group by issue date time: "  + time)

compressedDataset.unpersist(true)

datasetSorted.map(a => (a.get(0).toString, a.get(1).toString, a.get(2).toString, a.get(3).toString, a.get(4).toString)).toDF.write.parquet("hdfs://gkir-1:9000/Parking_Violations_Issued_-_Fiscal_Year_2017.parquet")
val parquet = spark.read.parquet("hdfs://gkir-1:9000/Parking_Violations_Issued_-_Fiscal_Year_2017.parquet")

parquet.persist
parquet.count

size = Math.round(sc.getRDDStorageInfo(0).memSize / (1024*1024.0) * (sc.getRDDStorageInfo(0).numPartitions / (sc.getRDDStorageInfo(0).numCachedPartitions * 1.0)) * 100) / 100.0
percentCached = Math.round((sc.getRDDStorageInfo(0).numCachedPartitions / (sc.getRDDStorageInfo(0).numPartitions * 1.0)) * 100)
list.add("parquet size: " + size + " percent cached: " + percentCached)

start = System.currentTimeMillis
parquet.filter(col("_1").equalTo("NY")).count
end = System.currentTimeMillis
time = (end - start) / 1000.00
list.add("parquet count state NY time: "  + time)

start = System.currentTimeMillis
parquet.filter(col("_1").equalTo("CA")).count
end = System.currentTimeMillis
time = (end - start) / 1000.00
list.add("parquet count state CA time: "  + time)

start = System.currentTimeMillis
parquet.rdd.map(a => (a(0), 1)).reduceByKey((a,b) => a + b).sortBy(a => a._2, false).take(10)
end = System.currentTimeMillis
time = (end - start) / 1000.00
list.add("parquet group by state time: "  + time)

start = System.currentTimeMillis
parquet.rdd.map(a => (a(1), 1)).reduceByKey((a,b) => a + b).sortBy(a => a._2, false).take(10)
end = System.currentTimeMillis
time = (end - start) / 1000.00
list.add("parquet group by plate type time: "  + time)

start = System.currentTimeMillis
parquet.rdd.map(a => (a(2), 1)).reduceByKey((a,b) => a + b).sortBy(a => a._2, false).take(10)
end = System.currentTimeMillis
time = (end - start) / 1000.00
list.add("parquet group by issue date time: "  + time)

start = System.currentTimeMillis
parquet.rdd.map(a => (a(3), 1)).reduceByKey((a,b) => a + b).sortBy(a => a._2, false).take(10)
end = System.currentTimeMillis
time = (end - start) / 1000.00
list.add("parquet group by vehicle body type time: "  + time)

start = System.currentTimeMillis
parquet.rdd.map(a => (a(4), 1)).reduceByKey((a,b) => a + b).sortBy(a => a._2, false).take(10)
end = System.currentTimeMillis
time = (end - start) / 1000.00
list.add("parquet group by vehicle make time: "  + time)

parquet.unpersist(true)

filteredDataset.persist
filteredDataset.count

size = Math.round(sc.getRDDStorageInfo(0).memSize / (1024*1024.0) * (sc.getRDDStorageInfo(0).numPartitions / (sc.getRDDStorageInfo(0).numCachedPartitions * 1.0)) * 100) / 100.0
percentCached = Math.round((sc.getRDDStorageInfo(0).numCachedPartitions / (sc.getRDDStorageInfo(0).numPartitions * 1.0)) * 100)
list.add("map reduce size: " + size + " percent cached: " + percentCached)

start = System.currentTimeMillis
filteredDataset.filter(a => a.get(0).equals("NY")).count
end = System.currentTimeMillis
time = (end - start) / 1000.00
list.add("map reduce count state NY time: "  + time)

start = System.currentTimeMillis
filteredDataset.filter(a => a.get(0).equals("CA")).count
end = System.currentTimeMillis
time = (end - start) / 1000.00
list.add("map reduce count state CA time: "  + time)

start = System.currentTimeMillis
filteredDataset.map(a => (a.get(0).toString, 1)).reduceByKey((a,b) => a + b).sortBy(a => a._2, false).take(10)
end = System.currentTimeMillis
time = (end - start) / 1000.00
list.add("map reduce group by state time: "  + time)

start = System.currentTimeMillis
filteredDataset.map(a => (a.get(4).toString, 1)).reduceByKey((a,b) => a + b).sortBy(a => a._2, false).take(10)
end = System.currentTimeMillis
time = (end - start) / 1000.00
list.add("map reduce group by vehicle make time: "  + time)

start = System.currentTimeMillis
filteredDataset.map(a => (a.get(1).toString, 1)).reduceByKey((a,b) => a + b).sortBy(a => a._2, false).take(10)
end = System.currentTimeMillis
time = (end - start) / 1000.00
list.add("map reduce group by plate type time: "  + time)

start = System.currentTimeMillis
filteredDataset.map(a => (a.get(3).toString, 1)).reduceByKey((a,b) => a + b).sortBy(a => a._2, false).take(10)
end = System.currentTimeMillis
time = (end - start) / 1000.00
list.add("map reduce group by vehicle body type time: "  + time)

start = System.currentTimeMillis
filteredDataset.map(a => (a.get(2).toString, 1)).reduceByKey((a,b) => a + b).sortBy(a => a._2, false).take(10)
end = System.currentTimeMillis
time = (end - start) / 1000.00
list.add("map reduce group by issue date time: "  + time)
filteredDataset.unpersist(true)

sc.parallelize(list).saveAsTextFile(raw"hdfs://gkir-1:9000/nyTicketsRun.csv")

//compressedDataset.map(chunk => (chunk.getColumn("Issue Date").`type`, 1)).reduceByKey((a,b) => a + b).sortBy(a => a._2, false).take(10)
