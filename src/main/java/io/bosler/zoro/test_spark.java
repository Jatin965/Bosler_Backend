//package io.bosler.zoro;
//
//import org.apache.spark.api.java.function.FilterFunction;
//import org.apache.spark.sql.Row;
//import org.apache.spark.sql.SparkSession;
//import org.apache.spark.sql.Dataset;
//
//import java.util.UUID;
//import java.lang.*;
//
//public class test_spark {
//    public String createDataset(UUID datasetId) {
//
//        String applicationName = UUID.randomUUID().toString();
//
//        SparkSession spark = SparkSession.builder()
//                .appName(applicationName)
//                .master("local[*]")
//                .config("spark.ui.showConsoleProgress", true)
//                .config("spark.executor.cores",1)
//                .config("spark.worker.cores",1)
//                .getOrCreate();
//
//
//        String csvFile = "data/zipcodes.csv"; // Should be some file on your system
//        String parquet_out = "data/" + datasetId.toString() + "/master";
//
//        Dataset<Row> df = spark.read().format("csv")
//                .option("sep", ",")
//                .option("inferSchema", "true")
//                .option("header", "true")
//                .load(csvFile);
//
//        df.write().parquet(parquet_out);
//
//        Dataset<Row> parquetFileDF = spark.read().parquet(parquet_out);
//
//        parquetFileDF.printSchema();
//
//        spark.stop();
//
//        return "Dataset created successfully";
//    }
//
//    public Dataset<Row> getDataset(UUID datasetId) {
//
//        String applicationName = UUID.randomUUID().toString();
//
//        SparkSession spark = SparkSession.builder()
//                .appName(applicationName)
//                .master("local[*]")
//                .config("spark.ui.showConsoleProgress", true)
//                .config("spark.executor.cores",1)
//                .config("spark.worker.cores",1)
//                .getOrCreate();
//
//
//        String datasetPath = "data/" + datasetId.toString() + "/master";
//
//        Dataset<Row> parquetFileDF = spark.read().parquet(datasetPath);
//
//        // parquetFileDF.printSchema();
//
//        spark.stop();
//
//        return parquetFileDF;
//    }
//}