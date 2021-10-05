package io.bosler.kitab.controllers;


import io.bosler.kitab.library.models.DatasetModel;
import io.bosler.kitab.library.models.FolderModel;
import io.bosler.kitab.library.repository.DatasetRepository;
import io.bosler.kitab.library.repository.FolderRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.File;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/kitab/dataset")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Kitab", description = "This is a catalog management service.")
public class datasetController {

    private final FolderRepository folderRepository;
    private final DatasetRepository datasetRepository;


    @Operation(summary = "It provides list of all dataset.")
    @GetMapping("/all")
    public ResponseEntity<List<FolderModel>> getDataset() {

        List<FolderModel> all = folderRepository.getByType("dataset");
        return ResponseEntity.ok().body(all);
    }

    @Operation(summary = "It provides dataset by Name")
    @GetMapping("/{name}/byName")
    public ResponseEntity<FolderModel> DatasetByName(@PathVariable("name") String name) {

        //List  = folderRepository.getByName(byname);
        //if(!exists) {
        //    return new ResponseEntity<>((FolderModel) folderRepository.getByName(byname), HttpStatus.NOT_FOUND);
        //}
        return new ResponseEntity<>(folderRepository.getByName(name), HttpStatus.OK);
    }


    @Operation(summary = "This can be used to create dataset.")
    @PostMapping("/create")
    public ResponseEntity<?> newDataset(@Valid @RequestBody FolderModel newDataset) {
        if (!folderRepository.existsById(newDataset.parent)) {
            return new ResponseEntity<>("parent id does not exists.", HttpStatus.NOT_ACCEPTABLE);
        }


        List<FolderModel> children = folderRepository.getByParent(newDataset.parent);

        if(children.stream().anyMatch(dataset -> newDataset.name.equals(dataset.name))) {
            return new ResponseEntity<>("Error: Same name dataset exists!", HttpStatus.NOT_ACCEPTABLE);
        }

        newDataset.type = "dataset";
        newDataset.status = "active";

        FolderModel folderDataset = folderRepository.save(newDataset);

        DatasetModel dataset = new DatasetModel();
        dataset.id = folderDataset.id;
        dataset.name = folderDataset.name;
        dataset.type = null;

        datasetRepository.saveAndFlush(dataset);

        return new ResponseEntity<>(folderDataset, HttpStatus.OK);

    }

    @Operation(summary = "It delete dataset by ID")
    @DeleteMapping(path = "/{Id}/delete")
    public ResponseEntity<String> deleteDataset(@PathVariable("Id") UUID Id) {
        boolean exists = folderRepository.existsById(Id);

        if (!exists) {
            return new ResponseEntity<>("Id " + Id + " does not exits!", HttpStatus.NOT_FOUND);
        }
        folderRepository.deleteById(Id);

        return new ResponseEntity<>("Id " + Id + " deleted successfully!", HttpStatus.OK);
    }

    @Operation(summary = "It provides dataset by Id")
    @GetMapping("/{Id}")
    public ResponseEntity<FolderModel> getDatasetById(@PathVariable("Id") UUID Id) {

        boolean exists = folderRepository.existsById(Id);
        if (!exists) {
            return new ResponseEntity<>(folderRepository.findById(Id).get(), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(folderRepository.findById(Id).get(), HttpStatus.OK);
    }

    @Operation(summary = "It rename dataset by name")
    @GetMapping(path = "/{name}/{newName}/rename")
    public ResponseEntity<?> renameDataset(
            @PathVariable("name") String name,
            @PathVariable("newName") String newName) {

        FolderModel dataset = folderRepository.getByName(name);
        dataset.name = newName;
        folderRepository.save(dataset);
      // if (!folderRepository.getByname(Name)){
           return new ResponseEntity<>("Dataset rename successfully", HttpStatus.OK);
      // }
   }
    @Operation(summary = "It provides dataset by path")
    @GetMapping("/Dataset/{Id}/getPath")
    public void getPath(@PathVariable("Id") UUID Id) throws Exception {

        List<FolderModel> exists = folderRepository.getByParent(Id);
        try {
            File file = new File(String.valueOf(folderRepository.getByParent(Id)));
            String path = file.getPath();
            System.out.println("File Path :" + path);
        } catch (Exception e) {

            System.err.println(e.getMessage());
        }
    }

}

