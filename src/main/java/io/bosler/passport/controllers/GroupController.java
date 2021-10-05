package io.bosler.passport.controllers;

import io.bosler.kitab.library.models.FolderModel;
import io.bosler.passport.library.repository.GroupsRepo;
import io.bosler.passport.library.service.GroupService;
import io.bosler.passport.library.service.UserService;
import io.bosler.passport.library.models.Groups;
import io.bosler.passport.library.models.Users;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@CrossOrigin
@RestController
@RequestMapping("/passport")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Passport", description = "Authentication service endpoints")
public class GroupController {
    private final GroupService groupService;
    private final UserService userService;
    private final GroupsRepo groupsRepo;


    @Operation(summary = "It provides list of all groups")
    @GetMapping("Groups/all")
    public ResponseEntity<List<Groups>> getGroups() {
        return ResponseEntity.ok().body(groupService.getGroups());
    }


    @Operation(summary = "It provides groups related to existing user")
    @RequestMapping(value = "/me", method = RequestMethod.GET)
    @ResponseBody
    public Users getMe(Principal principal) {
        return userService.getUser(principal.getName());
    }



    @Operation(summary = "It provides Groups by Id")
    @GetMapping("/Groups/GetById/{Id}")
    public ResponseEntity<Groups> getById(@PathVariable("Id") UUID Id) {

        boolean exists = groupsRepo.existsById(Id);
        if (!exists) {
            return new ResponseEntity<>(groupsRepo.findById(Id).get(), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(groupsRepo.findById(Id).get(), HttpStatus.OK);
    }

    @Operation(summary = "It provides groups by Name")
    @GetMapping("Groups/GetByName/{name}")
    public ResponseEntity<?> getByName(@PathVariable("name") String name) {

        boolean exists = groupsRepo.existsByName(name);
        if (!exists) {
            return new ResponseEntity<>(groupsRepo.getByName(name), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(groupsRepo.getByName(name), HttpStatus.OK);
    }
}
