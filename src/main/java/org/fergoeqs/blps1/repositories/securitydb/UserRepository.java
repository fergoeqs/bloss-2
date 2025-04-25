package org.fergoeqs.blps1.repositories.securitydb;


import org.fergoeqs.blps1.models.securitydb.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}