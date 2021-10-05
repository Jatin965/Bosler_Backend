package io.bosler.fractal.library.services;

import org.springframework.stereotype.Repository;


import io.bosler.fractal.library.models.FractalModel;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface FractalRepository
        extends JpaRepository<FractalModel, Long> {
}