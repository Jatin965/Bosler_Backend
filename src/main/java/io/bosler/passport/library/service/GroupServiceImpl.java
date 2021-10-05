package io.bosler.passport.library.service;

import io.bosler.passport.library.models.Groups;
import io.bosler.passport.library.models.Users;
import io.bosler.passport.library.repository.GroupsRepo;
import io.bosler.passport.library.repository.TokenRepo;
import io.bosler.passport.library.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service @RequiredArgsConstructor @Transactional @Slf4j
public class GroupServiceImpl implements GroupService {
    private final UserRepo userRepo;
    private final GroupsRepo groupsRepo;
    private final TokenRepo tokenRepo;
//    private final RoleRepo roleRepo;
    private final PasswordEncoder passwordEncoder;

//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        Users user = userRepo.findByUsername(username);
//        if(user == null) {
//            log.error("User not found in the database");
//            throw new UsernameNotFoundException(("User not found in the database"));
//        }
//        else {
//            log.info("User found in the database: {}", username);
//        }
//        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
////        user.getRoles().forEach(role -> authorities.add(new SimpleGrantedAuthority(role.getName())));
//        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), authorities);
//    }
//
//    @Override
//    //saving users by saving raw passwords
//    //public AppUser saveUser(AppUser user) {
//      //  log.info("Saving new user {} to the database", user.getName());
//        //return userRepo.save(user);
//    public Users saveUser(Users user) {
//        log.info("Saving new user {} to the database", user.getName());
//        user.setPassword(passwordEncoder.encode(user.getPassword()));
//        return userRepo.save(user);
//    }

//    @Override
//    public Role saveRole(Role role) {
//        log.info("Saving new role {} to the database", role.getName());
//        return roleRepo.save(role);
//    }

//    @Override
//    public void addRoleToUser(String username, String roleName) {
//        log.info("Adding role {} to user {}", roleName, username);
//        AppUser user = userRepo.findByUsername(username);
//        Role role = roleRepo.findByName(roleName);
//        user.getRoles().add(role);
//
//    }

    public Groups saveGroup(Groups groups) {
        log.info("Saving new group {} to the database", groups.getName());
        return groupsRepo.save(groups);
    }

    @Override
    public void addUserToGroupOwners(String username, String groupName) {
        log.info("Adding user {} to group Owners {}", groupName, username);
        Users user = userRepo.findByUsername(username);
        Groups group = groupsRepo.findByName(groupName);

        group.getGroups_owners().add(user);

    }

    @Override
    public void addUserToGroupAdministrators(String username, String groupName) {
        log.info("Adding user {} to group Administrators {}", groupName, username);
        Users user = userRepo.findByUsername(username);
        Groups group = groupsRepo.findByName(groupName);

        group.getGroups_administrators().add(user);

    }

    @Override
    public void addUserToGroupMembers(String username, String groupName) {
        log.info("Adding user {} to group Members {}", groupName, username);
        Users user = userRepo.findByUsername(username);
        Groups group = groupsRepo.findByName(groupName);

        group.getGroups_members().add(user);

    }

//    @Override
//    public Users getUser(String username) {
//        log.info("Fetching user {} ", username);
//        return userRepo.findByUsername(username);
//    }
//
//    @Override
//    public List<Users> getUsers() {
//        log.info("Fetching all users");
//        return userRepo.findAll();
//    }

    @Override
    public List<Groups> getGroups() {
        log.info("Fetching all groups");
        return groupsRepo.findAll();
    }

//    @Override
//    public TokenLongLived saveLongLivedToken(TokenLongLived tokenLongLived) {
//        log.info("Saving new longlived token {} to the database", tokenLongLived.getName());
//        return tokenRepo.save(tokenLongLived);
//    }
//
//    @Override
//    public List<TokenLongLived> getMyLongLivedTokens(UUID userId) {
//        log.info("Fetching all token for the user");
//        return tokenRepo.getByUserId(userId);
//    }
}
