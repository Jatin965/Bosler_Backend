package io.bosler.kitab.controllers;


import io.bosler.fractal.library.models.GitCommit;
import io.bosler.fractal.library.repository.GitService;
import io.bosler.kitab.library.models.FolderModel;
import io.bosler.kitab.library.repository.FolderRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/kitab/repository")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Kitab", description = "This is a catalog management service.")
public class repositoryController {

    private final FolderRepository folderRepository;
    private final GitService gitServiceRepository;


    @Operation(summary = "It provides list of all repository.")
    @GetMapping("/all")
    public ResponseEntity<List<FolderModel>> getRepository() {

        List<FolderModel> all = folderRepository.getByType("repository");
        return ResponseEntity.ok().body(all);
    }

    @Operation(summary = "This can be used to create repository.")
    @PostMapping("/create")
    public ResponseEntity<?> newRepository(@Valid @RequestBody FolderModel newRepository) throws IOException, GitAPIException {
        if (!folderRepository.existsById(newRepository.parent)) {
            return new ResponseEntity<>("parent id does not exists.", HttpStatus.NOT_ACCEPTABLE);
        }


        List<FolderModel> children = folderRepository.getByParent(newRepository.parent);

        if(children.stream().anyMatch(repository -> newRepository.name.equals(repository.name))) {
            return new ResponseEntity<>("Error: Same name repository exists!", HttpStatus.NOT_ACCEPTABLE);
        }
        newRepository.type = "repository";
        newRepository.status = "active";

        FolderModel createdRepository = folderRepository.save(newRepository);

        GitCommit gitCommit = new GitCommit();
        gitCommit.setMessage("Initial Repository");
        gitCommit.setEmail("new_email@bosler.io");
        gitCommit.setUsername("userId");

        gitServiceRepository.CreateRepository(gitCommit, createdRepository.id.toString());

        return new ResponseEntity<>(createdRepository, HttpStatus.OK);
    }

    @Operation(summary = "It delete repository by ID")
    @DeleteMapping(path = "/{Id}/delete")
    public ResponseEntity<String> deleteRepository(@PathVariable("Id") UUID Id) {
        boolean exists = folderRepository.existsById(Id);

        if (!exists) {
            return new ResponseEntity<>("Id " + Id + " does not exits!", HttpStatus.NOT_FOUND);
        }
        folderRepository.deleteById(Id);

        return new ResponseEntity<>("Id " + Id + " deleted successfully!", HttpStatus.OK);
    }


    @Operation(summary = "It provides repository by Name")
    @GetMapping("/{name}/byname")
    public ResponseEntity<FolderModel> getRepositoryByName(@PathVariable("name") String name) {

        //List  = folderRepository.getByName(byname);
        //if(!exists) {
        //    return new ResponseEntity<>((FolderModel) folderRepository.getByName(byname), HttpStatus.NOT_FOUND);
        //}
        return new ResponseEntity<>(folderRepository.getByName(name), HttpStatus.OK);
    }

    @Operation(summary = "It provides repository by Id")
    @GetMapping("/{Id}")
    public ResponseEntity<FolderModel> getRepositoryById(@PathVariable("Id") UUID Id) {

        boolean exists = folderRepository.existsById(Id);
        if (!exists) {
            return new ResponseEntity<>(folderRepository.findById(Id).get(), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(folderRepository.findById(Id).get(), HttpStatus.OK);
    }

    @Operation(summary = "It rename repository by name")
    @GetMapping(path = "/{name}/{newName}/rename")
    public ResponseEntity<?> renameRepository(
            @PathVariable("name") String name,
            @PathVariable("newName") String newName) {

        FolderModel Repository = folderRepository.getByName(name);
        Repository.name = newName;
        folderRepository.save(Repository);
        // if (!folderRepository.getByname(Name)){
        return new ResponseEntity<>("Repository rename successfully", HttpStatus.OK);
        // }
    }
}

