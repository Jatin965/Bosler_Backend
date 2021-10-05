package io.bosler.zoro.controllers;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import io.bosler.fractal.library.repository.GitService;
import io.bosler.kitab.library.repository.DatasetRepository;
import io.bosler.zoro.library.repository.ZoroRepository;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.*;

import org.apache.spark.sql.expressions.Window;
import org.apache.spark.sql.expressions.WindowSpec;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import scala.collection.JavaConverters;
import scala.collection.Seq;

import javax.management.ObjectName;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import static org.apache.commons.io.FileUtils.byteCountToDisplaySize;
import static org.apache.spark.sql.functions.row_number;


@CrossOrigin
@RestController
@RequestMapping("/zoro/dataset")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Zoro", description = "This is a data management service.")
public class dataset1Controller {

    private final ZoroRepository zoroRepository;
    private final GitService gitServiceZoro;
    private final DatasetRepository datasetRepository;

    @Hidden // not used or needed
    @Operation(summary = "This endpoint can be used to create / update datasets")
    @GetMapping("/{datasetId}/create")
    String createDataset(@PathVariable("datasetId") UUID datasetId) {

        SparkSession spark = sparkSession();

        String csvFile = gitServiceZoro.getBaseDirectory("../../Documents/Code/bosler/playground/data/zipcodes.csv");
        String parquet_out = gitServiceZoro.getBaseDirectory("/datasets/" + datasetId.toString());

        Dataset<Row> df = spark.read().format("csv")
                .option("sep", ",")
                .option("inferSchema", "true")
                .option("header", "true")
                .load(csvFile);

//        File parquetFiles = new File(parquet_out);
//
//        if(parquetFiles.exists()) {
//            parquetFiles.delete();
//        }

        df.write().parquet(parquet_out);
//        df.write().mode("overwrite").parquet(parquet_out);

        Dataset<Row> parquetFileDF = spark.read().parquet(parquet_out);

        parquetFileDF.printSchema();

        spark.stop();


        return "Dataset created successfully in " + parquet_out;
    }


    @PostMapping("/import/{datasetId}/{branch}")
    ResponseEntity<Object> uploadCSV(@RequestParam("file") MultipartFile file,
                                     @RequestParam("columnSeparator") String columnSeparator,
                                     @RequestParam("mode") String mode,
                                     @PathVariable("datasetId") UUID datasetId,
                                     @PathVariable("branch") String branch) throws Exception {

        if (!datasetRepository.existsById(datasetId)) { // check if the dataset exists in catalog
            Map<String, String> errorTemplate = new HashMap<>();
            errorTemplate.put("error", "No dataset found in catalog with id : " + datasetId.toString());
            return new ResponseEntity<>(errorTemplate, HttpStatus.NOT_FOUND);
        }

        if (!file.isEmpty()) {

            Path targetLocation = Paths.get(new StringBuilder(System.getProperty("user.home"))
                    .append(File.separator).append("bosler")
                    .append(File.separator).append("uploads")
                    .append(File.separator).append(file.getOriginalFilename())
                    .toString());

            // TODO read direct stream into data frame
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            SparkSession spark = sparkSession();

            String datasetPath = gitServiceZoro.getBaseDirectory("/datasets/" + datasetId.toString() + "/"+ branch );


            Dataset<Row> df = spark.read()
//                    .format("csv")
                    .format("com.databricks.spark.csv")
                    .option("sep", columnSeparator)
//                    .option("quote", "\"")
//                    .option("escape", "\"")
                    .option("inferSchema", "true")
                    .option("header", "true")
                    .load(targetLocation.toFile().getAbsolutePath());


            for (String column : df.columns()) { // Renaming columns because parquet do not like spaces in column names
                df = df.withColumnRenamed(column, column.trim().replaceAll(" ", "_").replaceAll("\"", ""));
            }

            df.write().mode(mode).parquet(datasetPath); // allowed modes overwrite , append : https://spark.apache.org/docs/3.0.1/api/java/org/apache/spark/sql/SaveMode.html

            Dataset<Row> parquetFileDF = spark.read().parquet(datasetPath);

//            parquetFileDF.printSchema();

            spark.stop();

            // delete uploaded file after successful upload
            Files.deleteIfExists(targetLocation);

            Map<String, String> successTemplate = new HashMap<>();
            successTemplate.put("success", "CSV File Uploaded successfully : " + datasetId.toString());
            return new ResponseEntity<>(successTemplate, HttpStatus.OK);

            //return "CSV File Uploaded successfully!";
        }
        //return "CSV File upload failed!";
        Map<String, String> errorTemplate = new HashMap<>();
        errorTemplate.put("error", "CSV File upload failed : " + datasetId.toString());
        return new ResponseEntity<>(errorTemplate, HttpStatus.BAD_REQUEST);

    }

    @Operation(summary = "This endpoint can be used to create / update datasets")
    @GetMapping(path = "/{datasetId}/{branch}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> readDataset(@PathVariable("datasetId") UUID datasetId, @PathVariable("branch") String branch) throws AnalysisException {

        if (!datasetRepository.existsById(datasetId)) { // check if the dataset exists in catalog
            Map<String, String> errorTemplate = new HashMap<>();
            errorTemplate.put("error", "No dataset and branches found in catalog for " + datasetId);
            return new ResponseEntity<>(errorTemplate, HttpStatus.NOT_FOUND);
        }

        SparkSession spark = sparkSession();

        try {
            String datasetPath = gitServiceZoro.getBaseDirectory("/datasets/" + datasetId.toString() + "/"+ branch );

            Dataset<Row> dfTotal = spark.read().parquet(datasetPath);
            Dataset<Row> df = dfTotal.limit(300);


            long totalRows = dfTotal.count(); // gives number of rows
            long totalColumns = dfTotal.columns().length; // gives number of rows
            long displayRows = df.count(); // gives number of rows
            long displayColumns = df.columns().length; // gives number of rows

            StructType schema = df.schema(); // gives schema

            Map<String, Object> stats = new HashMap<>();

            stats.put("displayRows", displayRows);
            stats.put("displayColumns", displayColumns);
            stats.put("totalRows", totalRows);
            stats.put("totalColumns", totalColumns);

            String[] columns = df.columns();

            WindowSpec w = Window.orderBy(columns[0]);

            Dataset<Row> dfRow = df.withColumn("key", row_number().over(w));

            List<Object> datasetList = new ArrayList<>();

            for(Row element : dfRow.collectAsList()) {

                String[] fieldNames = element.schema().fieldNames();
                Seq<String> fieldNamesSeq = JavaConverters.asScalaIteratorConverter(Arrays.asList(fieldNames).iterator()).asScala().toSeq();

                datasetList.add(element.getValuesMap(fieldNamesSeq));
            }


            //            spark.stop();

            // columns dict
            List<Object> colList = new ArrayList<>();
            for (StructField element : df.schema().fields()) {
                Map<String, Object> cols = new HashMap<>();
                cols.put("title", element.name() + " ( " + element.dataType().typeName() + " )");
                cols.put("dataIndex", element.name());
                cols.put("key", element.name());

                colList.add(cols);
            }


            Map<String, Object> result = new HashMap<>();

            result.put("schema", schema);
            result.put("columns", colList);
            result.put("rows", datasetList);

//            System.out.println(dfRow.collectAsList());

            result.put("stats", stats);

            return new ResponseEntity<>(dfRow.toJSON().collectAsList().toString(), HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, String> errorTemplate = new HashMap<>();
        errorTemplate.put("error", "No schema / data found for " + datasetId + "/" + branch);

        return new ResponseEntity<>(errorTemplate, HttpStatus.NOT_FOUND);

    }

    @GetMapping("/files/{datasetId}/{branch}")
    ResponseEntity<Object> datasetFiles(@PathVariable("datasetId") UUID datasetId, @PathVariable("branch") String branch) throws Exception {

        if (!datasetRepository.existsById(datasetId)) { // check if the dataset exists in catalog
            Map<String, String> errorTemplate = new HashMap<>();
            errorTemplate.put("error", "No dataset and branches found in catalog for " + datasetId);
            return new ResponseEntity<>(errorTemplate, HttpStatus.NOT_FOUND);
        }

        String datasetPath = gitServiceZoro.getBaseDirectory("/datasets/" + datasetId.toString() + "/"+ branch );

        if (!Files.exists(Paths.get(datasetPath))) {
            Map<String, String> errorTemplate = new HashMap<>();
            errorTemplate.put("error", "No dataset and branches found in filesystem for " + datasetId);
            return new ResponseEntity<>(errorTemplate, HttpStatus.NOT_FOUND);
        }

        File folder = new File(datasetPath);
        File[] listOfFiles = folder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return !file.isHidden();
            }
        });

        Arrays.sort(listOfFiles);

        List<Map<String,Object>> rows = new ArrayList<>();
        int i=1;

        for (File f : listOfFiles)
        {

            String pathAbsolute = f.getAbsolutePath();
            String pathBase = gitServiceZoro.getBaseDirectory("/datasets/" + datasetId);
            String pathRelative = new File(pathBase).toURI().relativize(new File(pathAbsolute).toURI()).getPath();

            BasicFileAttributes attr =
                    Files.readAttributes(Paths.get(pathAbsolute), BasicFileAttributes.class);

            Map<String, Object> tempMap = new HashMap<>();
            tempMap.put("key", String.valueOf(i));
            tempMap.put("path", pathRelative);
            tempMap.put("size", byteCountToDisplaySize(attr.size()));
//            tempMap.put("creationTime", attr.creationTime());
//            tempMap.put("lastModifiedTime", attr.lastModifiedTime());
            i++;

            rows.add(tempMap);



        }

        List<Map<String, Object>> columns = new ArrayList<>();

        Map<String, Object> cols = new HashMap<>();

        cols.put("title", "File Name");
        cols.put("dataIndex", "path");
        cols.put("key", "path");
        columns.add(cols);

        Map<String, Object> size = new HashMap<>();

        size.put("title", "Size");
        size.put("dataIndex", "size");
        size.put("key", "size");
        columns.add(size);


        Map<String, Object> files = new HashMap<>();

        files.put("rows", rows);
        files.put("columns", columns);

        return new ResponseEntity<>(files, HttpStatus.OK);
    }

    public SparkSession sparkSession() {
        String applicationName = UUID.randomUUID().toString();

        SparkSession spark = SparkSession.builder()
                .appName(applicationName)
                .master("local[*]")
                .config("spark.ui.showConsoleProgress", true)
                .config("spark.executor.cores", 1)
                .config("spark.worker.cores", 1)
                .config("spark.ui.enabled", false)
                .getOrCreate();

        return spark;
    }

    public static String underscoreToCamelCase(String underscoreName) {
        StringBuilder result = new StringBuilder();
        if (underscoreName != null && underscoreName.length() > 0) {
            boolean flag = false;
            for (int i = 0; i < underscoreName.length(); i++) {
                char ch = underscoreName.charAt(i);
                if ("_".charAt(0) == ch) {
                    flag = true;
                } else {
                    if (flag) {
                        result.append(Character.toUpperCase(ch));
                        flag = false;
                    } else {
                        result.append(ch);
                    }
                }
            }
        }
        return result.toString();
    }
}

