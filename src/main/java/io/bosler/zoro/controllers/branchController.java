package io.bosler.zoro.controllers;


import io.bosler.fractal.library.repository.GitService;
import io.bosler.kitab.library.repository.DatasetRepository;
import io.bosler.zoro.library.repository.ZoroRepository;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.spark.sql.AnalysisException;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.expressions.Window;
import org.apache.spark.sql.expressions.WindowSpec;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static org.apache.spark.sql.functions.row_number;


@CrossOrigin

@RestController
@RequestMapping("/zoro")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Zoro", description = "This is a data management service.")
public class branchController {

    private final ZoroRepository zoroRepository;
    private final GitService gitServiceZoro;
    private final DatasetRepository datasetRepository;


    @GetMapping("/branches/{datasetId}")
    ResponseEntity<Object> branches(@PathVariable("datasetId") UUID datasetId) throws Exception {


        if (!datasetRepository.existsById(datasetId)) { // check if the dataset exists in catalog
            Map<String, String> errorTemplate = new HashMap<>();
            errorTemplate.put("error", "No branches found in catalog for " + datasetId);
            return new ResponseEntity<>(errorTemplate, HttpStatus.NOT_FOUND);
        }

        String datasetPath = gitServiceZoro.getBaseDirectory("/datasets/" + datasetId.toString());

        if (!Files.exists(Paths.get(datasetPath))) {
            Map<String, String> errorTemplate = new HashMap<>();
            errorTemplate.put("error", "No dataset and branches found in filesystem for " + datasetId);
            return new ResponseEntity<>(errorTemplate, HttpStatus.NOT_FOUND);
        }

        File file = new File(datasetPath);
        String[] directories = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });

        return new ResponseEntity<>(directories, HttpStatus.OK);
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
