package com.example.restful.service;

import com.example.restful.entity.Category;
import com.example.restful.exception.ResourceNotFoundException;
import com.example.restful.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    
    public Category findById(Long id) {
        return categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("카테고리", id));
    }
    
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }
    
    @Transactional
    public Category create(Category category) {
        return categoryRepository.save(category);
    }
    
    @Transactional
    public Category update(Long id, Category category) {
        Category existing = findById(id);
        existing.setName(category.getName());
        existing.setDescription(category.getDescription());
        return categoryRepository.save(existing);
    }
    
    @Transactional
    public void delete(Long id) {
        Category category = findById(id);
        categoryRepository.delete(category);
    }
}

