import org.george.hybridcolumnar._, bitpacking._, delta._, chunk._, column._, domain._, roaring._, util._
import scala.collection.JavaConversions._
import java.util.ArrayList
import java.util.HashMap

def importDataset(dataset: org.apache.spark.rdd.RDD[String], fields: List[Int], datatypes: List[Datatype] = null, hasHeaders: Boolean = false): org.apache.spark.rdd.RDD[RowArray] = {
    val fieldsBc = sc.broadcast(fields);
    var datasetWithoutHeaders = dataset
    if (hasHeaders) {
        datasetWithoutHeaders = dataset.zipWithIndex.filter(a => a._2 != 0).map(a => a._1)
    }
    var types = datatypes
    if (datatypes == null) {
        types = Array.fill(fieldsBc.value.length)(Datatype.STRING).toList
    }
    datasetWithoutHeaders.
        map(a => {
            val tokens = a.stripPrefix("(").stripSuffix(")").split(","); val row = new RowArray
            for (field <- fieldsBc.value.zipWithIndex) {
                row.add(Input.parse(types(field._2), tokens(field._1)))
            } 
            row.setRunLength(1)
            row
        })
}

def importDatasetByNames(dataset: org.apache.spark.rdd.RDD[String], fields: List[String], datatypes: List[Datatype] = null, headers: List[String]): org.apache.spark.rdd.RDD[RowArray] = {
    val indexes = new ArrayList[Int];
    for (field <- fields) {
        for (header <- headers.zipWithIndex) {
            if (header._1 == field) {
                indexes.add(header._2);
            }
        }
    }
    importDataset(dataset, indexes.toList, datatypes, true)
}

def sortDataset(dataset: org.apache.spark.rdd.RDD[RowArray]): org.apache.spark.rdd.RDD[RowArray] = {
    dataset.mapPartitions(part => { 
        val list = new ArrayList[RowArray]
        part.foreach(a => {
            val row = new RowArray
            a.foreach(e => row.add(e))
            row.setRunLength(a.getRunLength())
            list.add(row)
        })
        list.sort(new RowArrayComparator(RowArrayAnalyzer.getColumnsOrderedByCardinality(list)))
        list.iterator
    })
}

def compressDataset(dataset: org.apache.spark.rdd.RDD[RowArray], fields: List[String] = null, encodings: List[ColumnType] = null): org.apache.spark.rdd.RDD[Chunk] = {
    dataset.mapPartitions(part => {
        val chunk = new Chunk
        Encoder.splitIntoColumns(part.toList).foreach(kv => {
            val bestEnc = {
                if (encodings == null) {
                    Encoder.findBestEncoding(kv._2)
                } else {
                    encodings(kv._1)
                }
            }
            if (fields == null) {
                chunk.addColumn(kv._1.toString, Encoder.compressColumn(kv._2, bestEnc))
            } else {
                chunk.addColumn(fields(kv._1), Encoder.compressColumn(kv._2, bestEnc))
            }
        })
        Array(chunk).iterator
    })
}
