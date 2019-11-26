import org.george.hybridcolumnar._, bitpacking._, chunk._, column._, domain._, roaring._, util._
import scala.collection.JavaConversions._
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.HashMap
import org.apache.spark.mllib.random.RandomRDDs._

var distribution = ""
var numberOfRows = 1000000000; // 1 Billion rows
var numberOfPartitions = 2560;

for (distribution <- List("uniform", "normal", "exponential1", "exponential2")) {
    val list = new ArrayList[String]
    var cardinality = 10

    val text = sc.textFile(raw"hdfs://gkir-1:9000/randomNumbers_size_1B_4cols_distribution_" + distribution + "_cardinality_" + cardinality + ".csv", numberOfPartitions)
    val dataset = importDataset(text, List(0,1,2,3), List(Datatype.INTEGER,Datatype.INTEGER,Datatype.INTEGER,Datatype.INTEGER))
    val datasetSorted = sortDataset(dataset)
    val encoding = null
    for (encoding <-  List(ColumnType.RLE, ColumnType.BIT_PACKING, ColumnType.DELTA, ColumnType.ROARING)) {

        if (encoding != ColumnType.BITMAP || cardinality <= 1000) {

            println("distribution_" + distribution + "_cardinality_" + cardinality + "_encoding_" + encoding + " second col")
            var singleCol = datasetSorted.map(a => {val l = new RowArray; l.add(a.get(1)); l.setRunLength(a.getRunLength); l})
            var compressedDataset = compressDataset(singleCol, null, List(encoding)).repartition(256)
            compressedDataset.persist
            compressedDataset.count
            var size = Math.round(sc.getRDDStorageInfo(0).memSize / (1024*1024.0) * (sc.getRDDStorageInfo(0).numPartitions / (sc.getRDDStorageInfo(0).numCachedPartitions * 1.0)) * 100) / 100.0
            var percentCached = Math.round((sc.getRDDStorageInfo(0).numCachedPartitions / (sc.getRDDStorageInfo(0).numPartitions * 1.0)) * 100)
            list.add("distribution_" + distribution + "_cardinality_" + cardinality + "_encoding_" + encoding + " second col size: " + size + " percent cached: " + percentCached)
            
            var x = sc.broadcast((cardinality * 0.1).toInt)
            var start = System.currentTimeMillis
            compressedDataset.map(chunk => {val column = chunk.getColumn("0"); val bitmap = column.selectLessThan(x.value); column.sum(bitmap)}).reduce((a,b) => a + b)
            var end = System.currentTimeMillis
            var time = (end - start) / 1000.00
            list.add("distribution_" + distribution + "_cardinality_" + cardinality + "_encoding_" + encoding + " second col select sum selectivity 10% time: "  + time)

            x = sc.broadcast((cardinality * 0.75).toInt)
            start = System.currentTimeMillis
            compressedDataset.map(chunk => {val column = chunk.getColumn("0"); val bitmap = column.selectLessThan(x.value); column.sum(bitmap)}).reduce((a,b) => a + b)
            end = System.currentTimeMillis
            time = (end - start) / 1000.00
            list.add("distribution_" + distribution + "_cardinality_" + cardinality + "_encoding_" + encoding + " second col select sum selectivity 75% time: "  + time)

            start = System.currentTimeMillis
            compressedDataset.flatMap(chunk => chunk).map(row => (row.get("0").toString.toInt, row.getRunLength)).reduceByKey((a,b) => a + b).collect
            end = System.currentTimeMillis
            time = (end - start) / 1000.00
            list.add("distribution_" + distribution + "_cardinality_" + cardinality + "_encoding_" + encoding + " second col group by time: "  + time)
            compressedDataset.unpersist(true)

            println("distribution_" + distribution + "_cardinality_" + cardinality + "_encoding_" + encoding + " third col")
            singleCol = datasetSorted.map(a => {val l = new RowArray; l.add(a.get(2)); l.setRunLength(a.getRunLength); l})
            compressedDataset = compressDataset(singleCol, null, List(encoding)).repartition(256)
            compressedDataset.persist
            compressedDataset.count
            size = Math.round(sc.getRDDStorageInfo(0).memSize / (1024*1024.0) * (sc.getRDDStorageInfo(0).numPartitions / (sc.getRDDStorageInfo(0).numCachedPartitions * 1.0)) * 100) / 100.0
            percentCached = Math.round((sc.getRDDStorageInfo(0).numCachedPartitions / (sc.getRDDStorageInfo(0).numPartitions * 1.0)) * 100)
            list.add("distribution_" + distribution + "_cardinality_" + cardinality + "_encoding_" + encoding + " third col size: " + size + " percent cached: " + percentCached)
            
            x = sc.broadcast((cardinality * 0.1).toInt)
            start = System.currentTimeMillis
            compressedDataset.map(chunk => {val column = chunk.getColumn("0"); val bitmap = column.selectLessThan(x.value); column.sum(bitmap)}).reduce((a,b) => a + b)
            end = System.currentTimeMillis
            time = (end - start) / 1000.00
            list.add("distribution_" + distribution + "_cardinality_" + cardinality + "_encoding_" + encoding + " third col select sum selectivity 10% time: "  + time)
            
            x = sc.broadcast((cardinality * 0.75).toInt)
            start = System.currentTimeMillis
            compressedDataset.map(chunk => {val column = chunk.getColumn("0"); val bitmap = column.selectLessThan(x.value); column.sum(bitmap)}).reduce((a,b) => a + b)
            end = System.currentTimeMillis
            time = (end - start) / 1000.00
            list.add("distribution_" + distribution + "_cardinality_" + cardinality + "_encoding_" + encoding + " third col select sum selectivity 75% time: "  + time)

            start = System.currentTimeMillis
            compressedDataset.flatMap(chunk => chunk).map(row => (row.get("0").toString.toInt, row.getRunLength)).reduceByKey((a,b) => a + b).collect
            end = System.currentTimeMillis
            time = (end - start) / 1000.00
            list.add("distribution_" + distribution + "_cardinality_" + cardinality + "_encoding_" + encoding + " third col group by time: "  + time)
            compressedDataset.unpersist(true)

            println("distribution_" + distribution + "_cardinality_" + cardinality + "_encoding_" + encoding + " fourth col")
            singleCol = datasetSorted.map(a => {val l = new RowArray; l.add(a.get(3)); l.setRunLength(a.getRunLength); l})
            compressedDataset = compressDataset(singleCol, null, List(encoding)).repartition(256)
            compressedDataset.persist
            compressedDataset.count
            size = Math.round(sc.getRDDStorageInfo(0).memSize / (1024*1024.0) * (sc.getRDDStorageInfo(0).numPartitions / (sc.getRDDStorageInfo(0).numCachedPartitions * 1.0)) * 100) / 100.0
            percentCached = Math.round((sc.getRDDStorageInfo(0).numCachedPartitions / (sc.getRDDStorageInfo(0).numPartitions * 1.0)) * 100)
            list.add("distribution_" + distribution + "_cardinality_" + cardinality + "_encoding_" + encoding + " fourth col size: " + size + " percent cached: " + percentCached)
            
            x = sc.broadcast((cardinality * 0.1).toInt)
            start = System.currentTimeMillis
            compressedDataset.map(chunk => {val column = chunk.getColumn("0"); val bitmap = column.selectLessThan(x.value); column.sum(bitmap)}).reduce((a,b) => a + b)
            end = System.currentTimeMillis
            time = (end - start) / 1000.00
            list.add("distribution_" + distribution + "_cardinality_" + cardinality + "_encoding_" + encoding + " fourth col select sum selectivity 10% time: "  + time)
            
            x = sc.broadcast((cardinality * 0.75).toInt)
            start = System.currentTimeMillis
            compressedDataset.map(chunk => {val column = chunk.getColumn("0"); val bitmap = column.selectLessThan(x.value); column.sum(bitmap)}).reduce((a,b) => a + b)
            end = System.currentTimeMillis
            time = (end - start) / 1000.00
            list.add("distribution_" + distribution + "_cardinality_" + cardinality + "_encoding_" + encoding + " fourth col select sum selectivity 75% time: "  + time)

            start = System.currentTimeMillis
            compressedDataset.flatMap(chunk => chunk).map(row => (row.get("0").toString.toInt, row.getRunLength)).reduceByKey((a,b) => a + b).collect
            end = System.currentTimeMillis
            time = (end - start) / 1000.00
            list.add("distribution_" + distribution + "_cardinality_" + cardinality + "_encoding_" + encoding + " fourth col group by time: "  + time)
            compressedDataset.unpersist(true)
            
        }
        
    }
    println("distribution_" + distribution + "_cardinality_" + cardinality + "_hybridcolumnar_best" + " second col")
    var singleCol = datasetSorted.map(a => {val l = new RowArray; l.add(a.get(1)); l.setRunLength(a.getRunLength); l})
    var compressedDataset = compressDataset(singleCol).repartition(256)
    compressedDataset.persist
    compressedDataset.count
    var size = Math.round(sc.getRDDStorageInfo(0).memSize / (1024*1024.0) * (sc.getRDDStorageInfo(0).numPartitions / (sc.getRDDStorageInfo(0).numCachedPartitions * 1.0)) * 100) / 100.0
    var percentCached = Math.round((sc.getRDDStorageInfo(0).numCachedPartitions / (sc.getRDDStorageInfo(0).numPartitions * 1.0)) * 100)
    list.add("distribution_" + distribution + "_cardinality_" + cardinality + "_hybridcolumnar_best" + " second col size: " + size + " percent cached: " + percentCached)
    
    var x = sc.broadcast((cardinality * 0.1).toInt)
    var start = System.currentTimeMillis
    compressedDataset.map(chunk => {val column = chunk.getColumn("0"); val bitmap = column.selectLessThan(x.value); column.sum(bitmap)}).reduce((a,b) => a + b)
    var end = System.currentTimeMillis
    var time = (end - start) / 1000.00
    list.add("distribution_" + distribution + "_cardinality_" + cardinality + "_hybridcolumnar_best" + " second col select sum selectivity 10% time: "  + time)
    
    x = sc.broadcast((cardinality * 0.75).toInt)
    start = System.currentTimeMillis
    compressedDataset.map(chunk => {val column = chunk.getColumn("0"); val bitmap = column.selectLessThan(x.value); column.sum(bitmap)}).reduce((a,b) => a + b)
    end = System.currentTimeMillis
    time = (end - start) / 1000.00
    list.add("distribution_" + distribution + "_cardinality_" + cardinality + "_hybridcolumnar_best" + " second col select sum selectivity 75% time: "  + time)

    start = System.currentTimeMillis
    compressedDataset.flatMap(chunk => chunk).map(row => (row.get("0").toString.toInt, row.getRunLength)).reduceByKey((a,b) => a + b).collect
    end = System.currentTimeMillis
    time = (end - start) / 1000.00
    list.add("distribution_" + distribution + "_cardinality_" + cardinality + "_hybridcolumnar_best" + " second col group by time: "  + time)
    compressedDataset.unpersist(true)

    println("distribution_" + distribution + "_cardinality_" + cardinality + "_hybridcolumnar_best" + " third col")
    singleCol = datasetSorted.map(a => {val l = new RowArray; l.add(a.get(2)); l.setRunLength(a.getRunLength); l})
    compressedDataset = compressDataset(singleCol).repartition(256)
    compressedDataset.persist
    compressedDataset.count
    size = Math.round(sc.getRDDStorageInfo(0).memSize / (1024*1024.0) * (sc.getRDDStorageInfo(0).numPartitions / (sc.getRDDStorageInfo(0).numCachedPartitions * 1.0)) * 100) / 100.0
    percentCached = Math.round((sc.getRDDStorageInfo(0).numCachedPartitions / (sc.getRDDStorageInfo(0).numPartitions * 1.0)) * 100)
    list.add("distribution_" + distribution + "_cardinality_" + cardinality + "_hybridcolumnar_best" + " third col size: " + size + " percent cached: " + percentCached)
    
    x = sc.broadcast((cardinality * 0.1).toInt)
    start = System.currentTimeMillis
    compressedDataset.map(chunk => {val column = chunk.getColumn("0"); val bitmap = column.selectLessThan(x.value); column.sum(bitmap)}).reduce((a,b) => a + b)
    end = System.currentTimeMillis
    time = (end - start) / 1000.00
    list.add("distribution_" + distribution + "_cardinality_" + cardinality + "_hybridcolumnar_best" + " third col select sum selectivity 10% time: "  + time)

    x = sc.broadcast((cardinality * 0.75).toInt)
    start = System.currentTimeMillis
    compressedDataset.map(chunk => {val column = chunk.getColumn("0"); val bitmap = column.selectLessThan(x.value); column.sum(bitmap)}).reduce((a,b) => a + b)
    end = System.currentTimeMillis
    time = (end - start) / 1000.00
    list.add("distribution_" + distribution + "_cardinality_" + cardinality + "_hybridcolumnar_best" + " third col select sum selectivity 75% time: "  + time)

    start = System.currentTimeMillis
    compressedDataset.flatMap(chunk => chunk).map(row => (row.get("0").toString.toInt, row.getRunLength)).reduceByKey((a,b) => a + b).collect
    end = System.currentTimeMillis
    time = (end - start) / 1000.00
    list.add("distribution_" + distribution + "_cardinality_" + cardinality + "_hybridcolumnar_best" + " third col group by time: "  + time)
    compressedDataset.unpersist(true)
    
    println("distribution_" + distribution + "_cardinality_" + cardinality + "_hybridcolumnar_best" + " fourth col")
    singleCol = datasetSorted.map(a => {val l = new RowArray; l.add(a.get(3)); l.setRunLength(a.getRunLength); l})
    compressedDataset = compressDataset(singleCol).repartition(256)
    compressedDataset.persist
    compressedDataset.count
    size = Math.round(sc.getRDDStorageInfo(0).memSize / (1024*1024.0) * (sc.getRDDStorageInfo(0).numPartitions / (sc.getRDDStorageInfo(0).numCachedPartitions * 1.0)) * 100) / 100.0
    percentCached = Math.round((sc.getRDDStorageInfo(0).numCachedPartitions / (sc.getRDDStorageInfo(0).numPartitions * 1.0)) * 100)
    list.add("distribution_" + distribution + "_cardinality_" + cardinality + "_hybridcolumnar_best" + " fourth col size: " + size + " percent cached: " + percentCached)

    x = sc.broadcast((cardinality * 0.1).toInt)
    start = System.currentTimeMillis
    compressedDataset.map(chunk => {val column = chunk.getColumn("0"); val bitmap = column.selectLessThan(x.value); column.sum(bitmap)}).reduce((a,b) => a + b)
    end = System.currentTimeMillis
    time = (end - start) / 1000.00
    list.add("distribution_" + distribution + "_cardinality_" + cardinality + "_hybridcolumnar_best" + " fourth col select sum selectivity 10% time: "  + time)
    
    x = sc.broadcast((cardinality * 0.75).toInt)
    start = System.currentTimeMillis
    compressedDataset.map(chunk => {val column = chunk.getColumn("0"); val bitmap = column.selectLessThan(x.value); column.sum(bitmap)}).reduce((a,b) => a + b)
    end = System.currentTimeMillis
    time = (end - start) / 1000.00
    list.add("distribution_" + distribution + "_cardinality_" + cardinality + "_hybridcolumnar_best" + " fourth col select sum selectivity 75% time: "  + time)

    start = System.currentTimeMillis
    compressedDataset.flatMap(chunk => chunk).map(row => (row.get("0").toString.toInt, row.getRunLength)).reduceByKey((a,b) => a + b).collect
    end = System.currentTimeMillis
    time = (end - start) / 1000.00
    list.add("distribution_" + distribution + "_cardinality_" + cardinality + "_hybridcolumnar_best" + " fourth col group by time: "  + time)
    
    compressedDataset.unpersist(true)
    
    sc.parallelize(list).saveAsTextFile(raw"hdfs://gkir-1:9000/compareDatasetTime4Cols" + "_distribution_" + distribution + ".csv")
}
