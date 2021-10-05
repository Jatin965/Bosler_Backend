package io.bosler.passport.library.service;

import io.bosler.passport.library.models.Groups;

import java.util.List;

public interface GroupService {
//    Users getUser(String username);
//    List<Users> getUsers();
//    Users saveUser(Users user);
    Groups saveGroup(Groups groups);
    void addUserToGroupOwners(String username, String groupName);
    void addUserToGroupAdministrators(String username, String groupName);
    void addUserToGroupMembers(String username, String groupName);
    List<Groups> getGroups();

//    List<TokenLongLived> getMyLongLivedTokens(UUID userId);
//    TokenLongLived saveLongLivedToken(TokenLongLived tokenLongLived);
}
