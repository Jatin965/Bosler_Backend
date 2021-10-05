package io.bosler.kitab.library.repository;

import io.bosler.kitab.library.models.FolderModel;
import io.bosler.passport.library.models.Groups;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FolderRepository
        extends JpaRepository<FolderModel, UUID> {

    List<FolderModel> getByType(String type);
    List<FolderModel> findAllByName(String name);
    List<FolderModel> getByParent(UUID parent);
    FolderModel getByName(String name);
//    boolean getByname(String name);

}
