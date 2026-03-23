package com.airmonitor.usermicroservice.repository;

import com.airmonitor.usermicroservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);
    List<User> findByRole(com.airmonitor.usermicroservice.entity.Role role);
    boolean existsByEmail(String email);
}
