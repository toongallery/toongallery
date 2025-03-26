package com.example.toongallery.domain.user.repository;



import com.example.toongallery.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    List<User> findByNameIn(Collection<String> names);

    @Query("SELECT u.name FROM User u WHERE u.id IN :authorIds")
    List<String> findNamesById(@Param("authorIds") List<Long> authorIds);
}
