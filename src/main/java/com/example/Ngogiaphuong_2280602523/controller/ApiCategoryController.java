package com.example.Ngogiaphuong_2280602523.controller;

import com.example.Ngogiaphuong_2280602523.model.Category;
import com.example.Ngogiaphuong_2280602523.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/categories")
public class ApiCategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping
    public List<Category> getAllCategories() {
        List<Category> all = categoryRepository.findAll();
        for (Category cat : all) {
            cat.setImage(cat.getImageurl());
            if (cat.getParentId() != null) {
                categoryRepository.findById(cat.getParentId()).ifPresent(p -> cat.setParent(p.getName()));
            }
        }
        return all;
    }

    @PostMapping("/sync")
    public ResponseEntity<?> syncCategories(@RequestBody List<Category> categories) {
        try {
            categoryRepository.deleteAll();
        } catch (Exception e) {
            System.err.println("Could not delete all categories, some might be in use.");
        }

        for (Category cat : categories) {
            String parentStr = cat.getParent();
            if (parentStr != null && !parentStr.isEmpty()) {
                Category parentCat = categoryRepository.findByName(parentStr).orElse(null);
                if (parentCat == null) {
                    parentCat = new Category();
                    parentCat.setName(parentStr);
                    parentCat.setShowInAccessories(false);
                    parentCat = categoryRepository.save(parentCat);
                }
                cat.setParentId(parentCat.getId());
            }
            cat.setImageurl(cat.getImage());

            if (cat.getId() != null) {
                Category existing = categoryRepository.findById(cat.getId()).orElse(null);
                if (existing != null) {
                    existing.setName(cat.getName());
                    existing.setParentId(cat.getParentId());
                    existing.setImageurl(cat.getImageurl());
                    existing.setShowInAccessories(cat.getShowInAccessories());
                    categoryRepository.save(existing);
                    continue;
                }
            }
            // Ensure ID is null so it inserts cleanly if not found
            cat.setId(null);
            categoryRepository.save(cat);
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/bulk")
    public ResponseEntity<?> createBulk(@RequestBody List<Category> categories) {
        for (Category c : categories) {
            String parentStr = c.getParent();
            if (parentStr != null && !parentStr.isEmpty()) {
                Category parentCat = categoryRepository.findByName(parentStr).orElse(null);
                if (parentCat == null) {
                    parentCat = new Category();
                    parentCat.setName(parentStr);
                    parentCat.setShowInAccessories(false);
                    parentCat = categoryRepository.save(parentCat);
                }
                c.setParentId(parentCat.getId());
            }
            c.setImageurl(c.getImage());
        }
        categoryRepository.saveAll(categories);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public Category createCategory(@RequestBody Category category) {
        String parentStr = category.getParent();
        if (parentStr != null && !parentStr.isEmpty()) {
            Category parentCat = categoryRepository.findByName(parentStr).orElse(null);
            if (parentCat == null) {
                parentCat = new Category();
                parentCat.setName(parentStr);
                parentCat.setShowInAccessories(false);
                parentCat = categoryRepository.save(parentCat);
            }
            category.setParentId(parentCat.getId());
        }
        category.setImageurl(category.getImage());
        return categoryRepository.save(category);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @RequestBody Category categoryDetails) {
        Optional<Category> categoryOptional = categoryRepository.findById(id);
        if (categoryOptional.isPresent()) {
            Category category = categoryOptional.get();
            category.setName(categoryDetails.getName());
            category.setShowInAccessories(categoryDetails.getShowInAccessories());

            String parentStr = categoryDetails.getParent();
            if (parentStr != null && !parentStr.isEmpty()) {
                Category parentCat = categoryRepository.findByName(parentStr).orElse(null);
                if (parentCat == null) {
                    parentCat = new Category();
                    parentCat.setName(parentStr);
                    parentCat.setShowInAccessories(false);
                    parentCat = categoryRepository.save(parentCat);
                }
                category.setParentId(parentCat.getId());
            } else {
                category.setParentId(null);
            }

            category.setImageurl(categoryDetails.getImage());
            return ResponseEntity.ok(categoryRepository.save(category));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        categoryRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
