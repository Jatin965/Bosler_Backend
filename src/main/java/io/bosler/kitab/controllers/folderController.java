package io.bosler.kitab.controllers;


import io.bosler.kitab.library.models.DatasetModel;
import io.bosler.kitab.library.models.FolderModel;
import io.bosler.kitab.library.repository.DatasetRepository;
import io.bosler.kitab.library.repository.FolderRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/kitab/folder")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Kitab", description = "This is a catalog management service.")
public class folderController {

    private final FolderRepository folderRepository;

    public folderController(FolderRepository folderRepository) {
        this.folderRepository = folderRepository;
    }

    @Operation(summary = "It provides list of all folders.")
    @GetMapping("/all")
    public ResponseEntity<List<FolderModel>> getFolders() {

        List<FolderModel> all = folderRepository.getByType("folder");
        return ResponseEntity.ok().body(all);
    }

    @Operation(summary = "It provides folder by Id")
    @GetMapping("/{Id}")
    public ResponseEntity<FolderModel> getFolderById(@PathVariable("Id") UUID Id) {

        boolean exists = folderRepository.existsById(Id);
        if (!exists) {
            return new ResponseEntity<>(folderRepository.findById(Id).get(), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(folderRepository.findById(Id).get(), HttpStatus.OK);
    }

    @Operation(summary = "This can be used to create folder.")
    @PostMapping("/create")
    public ResponseEntity<?> newFolder(@Valid @RequestBody FolderModel newFolder) {
        if (!folderRepository.existsById(newFolder.parent)) {
            return new ResponseEntity<>("parent id does not exists.", HttpStatus.NOT_ACCEPTABLE);
        }


        List<FolderModel> children = folderRepository.getByParent(newFolder.parent);

        if(children.stream().anyMatch(folder -> newFolder.name.equals(folder.name))) {
            return new ResponseEntity<>("Error: Same name folder exists!", HttpStatus.NOT_ACCEPTABLE);
        }
        newFolder.type = "folder";
        newFolder.status = "active";

        return new ResponseEntity<>(folderRepository.save(newFolder), HttpStatus.OK);
    }

    @Operation(summary = "It delete folder by ID")
    @DeleteMapping(path = "/{Id}/delete")
    public ResponseEntity<String> deleteFolder(@PathVariable("Id") UUID Id) {
        boolean exists = folderRepository.existsById(Id);

        if (!exists) {
            return new ResponseEntity<>("Id " + Id + " does not exits!", HttpStatus.NOT_FOUND);
        }
        folderRepository.deleteById(Id);

        return new ResponseEntity<>("FolderId " + Id + " deleted successfully!", HttpStatus.OK);
    }


    @Operation(summary = "Get list of all children by parentId")
    @GetMapping("/children/{Id}")
    public ResponseEntity<List<FolderModel>> getByParent(@PathVariable("Id") UUID Id) {

        List<FolderModel> allFolders = folderRepository.getByParent(Id);
        return ResponseEntity.ok().body(allFolders);
    }

    @Operation(summary = "It provides folder by Name")
    @GetMapping("/{id}/byname")
    public ResponseEntity<FolderModel> getFolderByName(@PathVariable("byname") String byname) {

        //List  = folderRepository.getByName(byname);
        //if(!exists) {
        //    return new ResponseEntity<>((FolderModel) folderRepository.getByName(byname), HttpStatus.NOT_FOUND);
        //}
        return new ResponseEntity<>(folderRepository.getByName(byname), HttpStatus.OK);
    }

    @Operation(summary = "It rename folder by name")
    @GetMapping(path = "/{name}/{newName}/rename")
    public ResponseEntity<?> renameFolder(
            @PathVariable("name") String name,
            @PathVariable("newName") String newName) {

        FolderModel folder = folderRepository.getByName(name);
        folder.name = newName;
        folderRepository.save(folder);
        // if (!folderRepository.getByname(Name)){
        return new ResponseEntity<>("Folder rename successfully", HttpStatus.OK);
        // }
    }

}

