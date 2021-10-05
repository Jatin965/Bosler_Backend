package io.bosler.passport.library.repository;

import io.bosler.passport.library.models.TokenLongLived;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TokenRepo extends JpaRepository<TokenLongLived, Long> {
    TokenLongLived findByName(String name);
    List<TokenLongLived> getByUserId(UUID userId);
}
