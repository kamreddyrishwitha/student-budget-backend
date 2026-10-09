package com.studentbudget.backend.repository;

import com.studentbudget.backend.entity.Category;
import com.studentbudget.backend.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository
        extends JpaRepository<Category, Long> {

    List<Category> findByUserOrderByNameAsc(User user);

    Optional<Category> findByIdAndUser(
            Long id,
            User user
    );

    boolean existsByUserAndNameIgnoreCase(
            User user,
            String name
    );
}