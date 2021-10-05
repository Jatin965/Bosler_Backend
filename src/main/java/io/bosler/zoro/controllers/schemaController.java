package io.bosler.zoro.controllers;


import io.bosler.fractal.library.repository.GitService;
import io.bosler.kitab.library.repository.DatasetRepository;
import io.bosler.zoro.library.repository.ZoroRepository;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.spark.sql.AnalysisException;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.expressions.Window;
import org.apache.spark.sql.expressions.WindowSpec;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static org.apache.commons.io.FileUtils.byteCountToDisplaySize;
import static org.apache.spark.sql.functions.row_number;


@CrossOrigin

@RestController
@RequestMapping("/zoro/schema")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Zoro", description = "This is a data management service.")
public class schemaController {

    private final ZoroRepository zoroRepository;
    private final GitService gitServiceZoro;
    private final DatasetRepository datasetRepository;


    @Operation(summary = "This endpoint can be used to create / update datasets")
    @GetMapping("/{datasetId}/{branch}")
    ResponseEntity<Object> schema(@PathVariable("datasetId") UUID datasetId, @PathVariable("branch") String branch) {

        SparkSession spark = sparkSession();

        try {
            String datasetPath = gitServiceZoro.getBaseDirectory("/datasets/" + datasetId.toString() + "/" + branch);

            Dataset<Row> dfTotal = spark.read().parquet(datasetPath);
            Dataset<Row> df = dfTotal.limit(300);

            long totalRows = dfTotal.rdd().count(); // gives number of rows
            long totalColumns = dfTotal.columns().length; // gives number of rows
            long displayRows = df.rdd().count(); // gives number of rows
            long displayColumns = df.columns().length; // gives number of rows

            Map<String, Object> stats = new HashMap<>();

            stats.put("displayRows", displayRows);
            stats.put("displayColumns", displayColumns);
            stats.put("totalRows", totalRows);
            stats.put("totalColumns", totalColumns);

            String[] columns = df.columns();

            WindowSpec w = Window.orderBy(columns[0]);
            Dataset<Row> dfRow = df.withColumn("key", row_number().over(w));

            // columns dict
            List<Object> colList = new ArrayList<>();
            for (StructField element : df.schema().fields()) {
                Map<String, Object> cols = new HashMap<>();
                cols.put("title", element.name() + "\n" + element.dataType().typeName());
                cols.put("dataIndex", element.name());
                cols.put("key", element.name());

                colList.add(cols);
            }


            Map<String, Object> result = new HashMap<>();

            result.put("schema", df.schema());
//            result.put("columns", colList);
//
//            result.put("stats", stats);


            spark.stop();

            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, String> errorTemplate = new HashMap<>();
        errorTemplate.put("error", "No schema / data found for " + datasetId + "/" + branch);

        return new ResponseEntity<>(errorTemplate, HttpStatus.NOT_FOUND);
    }

    @Operation(summary = "This endpoint can be used to create / update datasets")
    @GetMapping("/{datasetId}/{branch}/columns")
    ResponseEntity<Object> columns(@PathVariable("datasetId") UUID datasetId, @PathVariable("branch") String branch) {

        SparkSession spark = sparkSession();

        try {
            String datasetPath = gitServiceZoro.getBaseDirectory("/datasets/" + datasetId.toString() + "/" + branch);

            Dataset<Row> dfTotal = spark.read().parquet(datasetPath);
            Dataset<Row> df = dfTotal.limit(300);

            long totalRows = dfTotal.rdd().count(); // gives number of rows
            long totalColumns = dfTotal.columns().length; // gives number of rows
            long displayRows = df.rdd().count(); // gives number of rows
            long displayColumns = df.columns().length; // gives number of rows

            Map<String, Object> stats = new HashMap<>();

            stats.put("displayRows", displayRows);
            stats.put("displayColumns", displayColumns);
            stats.put("totalRows", totalRows);
            stats.put("totalColumns", totalColumns);
            stats.put("datasetSize", byteCountToDisplaySize(FileUtils.sizeOfDirectory(new File(datasetPath))));

            String[] columns = df.columns();

            WindowSpec w = Window.orderBy(columns[0]);
            Dataset<Row> dfRow = df.withColumn("key", row_number().over(w));

            // columns dict
            List<Object> colList = new ArrayList<>();
            for (StructField element : df.schema().fields()) {
                Map<String, Object> cols = new HashMap<>();
                cols.put("title", element.name() + "\n" + element.dataType().typeName());
                cols.put("dataIndex", element.name());
                cols.put("key", element.name());

                colList.add(cols);
            }


            Map<String, Object> result = new HashMap<>();

//            result.put("schema", df.schema());
            result.put("columns", colList);

            result.put("stats", stats);


            spark.stop();

            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, String> errorTemplate = new HashMap<>();
        errorTemplate.put("error", "No schema / data found for " + datasetId + "/" + branch);

        return new ResponseEntity<>(errorTemplate, HttpStatus.NOT_FOUND);
    }

    @Operation(summary = "This endpoint can be used to apply schema to existing datasets")
    @PostMapping("/{datasetId}/{branch}/apply")
    StructType applySchema(@PathVariable("datasetId") UUID datasetId, @RequestBody String schema, @PathVariable("branch") String branch) throws IOException {

        SparkSession spark = sparkSession();

        String datasetPath = gitServiceZoro.getBaseDirectory("/datasets/" + datasetId.toString() + "/" + branch);
        String datasetPathNew = gitServiceZoro.getBaseDirectory("/datasets/" + datasetId.toString() + "/" + branch + "_new");

        DataType schema_struct = StructType.fromJson(schema);

        Dataset<Row> df = spark.read().schema((StructType) schema_struct).parquet(datasetPath);

        df.write().mode("overwrite").parquet(datasetPathNew);

        spark.stop();

        // Not a very good this but its a temporary fix 21/09/2021
        File oldDataset = new File(datasetPath);
        File newDataset = new File(datasetPathNew);
        FileUtils.deleteDirectory(oldDataset);

        newDataset.renameTo(oldDataset);

        return (StructType) schema_struct;
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
}

