package com.studentbudget.backend.service;

import com.studentbudget.backend.dto.CategoryRequest;
import com.studentbudget.backend.entity.Category;
import com.studentbudget.backend.entity.User;
import com.studentbudget.backend.exception.BadRequestException;
import com.studentbudget.backend.exception.ResourceNotFoundException;
import com.studentbudget.backend.repository.CategoryRepository;
import com.studentbudget.backend.repository.UserRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public CategoryService(
            CategoryRepository categoryRepository,
            UserRepository userRepository) {

        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Authenticated user not found"
                        )
                );
    }

    public Category addCategory(
            String email,
            CategoryRequest request) {

        User user = getCurrentUser(email);

        String categoryName =
                request.name().trim();

        if (categoryRepository
                .existsByUserAndNameIgnoreCase(
                        user,
                        categoryName
                )) {

            throw new BadRequestException(
                    "Category already exists"
            );
        }

        Category category = Category.builder()
                .name(categoryName)
                .description(
                        cleanDescription(
                                request.description()
                        )
                )
                .user(user)
                .build();

        return categoryRepository.save(category);
    }

    public List<Category> getAllCategories(
            String email) {

        User user = getCurrentUser(email);

        return categoryRepository
                .findByUserOrderByNameAsc(user);
    }

    public Category getCategoryById(
            String email,
            Long id) {

        User user = getCurrentUser(email);

        return categoryRepository
                .findByIdAndUser(id, user)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category not found"
                        )
                );
    }

    public Category updateCategory(
            String email,
            Long id,
            CategoryRequest request) {

        User user = getCurrentUser(email);

        Category category =
                categoryRepository
                        .findByIdAndUser(id, user)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Category not found"
                                )
                        );

        String newName =
                request.name().trim();

        if (!category.getName()
                .equalsIgnoreCase(newName)
                && categoryRepository
                .existsByUserAndNameIgnoreCase(
                        user,
                        newName
                )) {

            throw new BadRequestException(
                    "Category already exists"
            );
        }

        category.setName(newName);

        category.setDescription(
                cleanDescription(
                        request.description()
                )
        );

        return categoryRepository.save(category);
    }

    public void deleteCategory(
            String email,
            Long id) {

        User user = getCurrentUser(email);

        Category category =
                categoryRepository
                        .findByIdAndUser(id, user)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Category not found"
                                )
                        );

        categoryRepository.delete(category);
    }

    private String cleanDescription(
            String description) {

        if (description == null) {
            return null;
        }

        String cleaned =
                description.trim();

        return cleaned.isEmpty()
                ? null
                : cleaned;
    }
}