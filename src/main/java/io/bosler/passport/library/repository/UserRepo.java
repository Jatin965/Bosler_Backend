package io.bosler.passport.library.repository;

import io.bosler.passport.library.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<Users, Long> {
     Users findByUsername(String username);
}
