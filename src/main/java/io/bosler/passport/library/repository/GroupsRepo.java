package io.bosler.passport.library.repository;

import io.bosler.passport.library.models.Groups;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface GroupsRepo extends JpaRepository<Groups, UUID> {

    Groups findByName(String name);

    List<Groups> getByName(String name);

    boolean existsByName(String name);
}