package io.bosler.kitab.library.repository;

import io.bosler.kitab.library.models.DatasetModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DatasetRepository
        extends JpaRepository<DatasetModel, UUID> {

    List<DatasetModel> getByType(String type);
    List<DatasetModel> getByName(String name);

}