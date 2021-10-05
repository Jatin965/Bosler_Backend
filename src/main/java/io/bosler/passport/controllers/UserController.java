package io.bosler.passport.controllers;

import io.bosler.passport.library.models.Users;
import io.bosler.passport.library.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@CrossOrigin("http://localhost:3000")
@RestController
@RequestMapping("/passport")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Passport", description = "Authentication service endpoints")
public class UserController {
    private final UserService userService;

    @Operation(summary = "It provides list of all users")
    @GetMapping("/users")
    public ResponseEntity<List<Users>> getUsers() {
        return ResponseEntity.ok().body(userService.getUsers());
    }

    @Operation(summary = "To add new users")
    @PostMapping("/users/add")
    public ResponseEntity<Users> saveUser(@RequestBody Users user) {
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/passport/user/save").toUriString());
        return ResponseEntity.created(uri).body(userService.saveUser(user));
    }


//    @Operation(summary = "It provides list of all groups")
//    @GetMapping("/groups")
//    public ResponseEntity<List<Groups>> getGroups() {
//        return ResponseEntity.ok().body(userService.getGroups());
//    }
//
//
//    @RequestMapping(value = "/me", method = RequestMethod.GET)
//    @ResponseBody
//    public Users getMe(Principal principal) {
//        return userService.getUser(principal.getName());
//    }

//    @GetMapping("/ssoAttributes")
//    public Map<String, Object> user(@AuthenticationPrincipal OAuth2User principal) {
//        return principal.getAttributes();
//        return Collections.singletonMap("ssoAttributes", principal.getAttributes());
//    }
}
