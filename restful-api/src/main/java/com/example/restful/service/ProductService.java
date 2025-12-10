package com.example.restful.service;

import com.example.restful.dto.ProductCreateRequest;
import com.example.restful.dto.ProductUpdateRequest;
import com.example.restful.entity.Category;
import com.example.restful.entity.Product;
import com.example.restful.exception.ResourceNotFoundException;
import com.example.restful.repository.CategoryRepository;
import com.example.restful.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
    
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    
    public Product findById(Long id) {
        return productRepository.findByIdWithCategory(id)
            .orElseThrow(() -> new ResourceNotFoundException("제품", id));
    }
    
    public List<Product> findAll() {
        return productRepository.findAll();
    }
    
    public Page<Product> findAll(Pageable pageable) {
        return productRepository.findAllWithCategory(pageable);
    }
    
    public Page<Product> findByNameContaining(String name, Pageable pageable) {
        return productRepository.findByNameContaining(name, pageable);
    }
    
    // 제품명 또는 설명에서 검색
    public Page<Product> searchByNameOrDescription(String keyword, Pageable pageable) {
        return productRepository.searchByNameOrDescription(keyword, pageable);
    }
    
    public Page<Product> findByCategoryId(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable);
    }
    
    public List<Product> findByCategoryId(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }
    
    // 기존 메서드 (페이징 미지원)
    public List<Product> findByPriceRange(Double minPrice, Double maxPrice) {
        return productRepository.findByPriceRange(minPrice, maxPrice);
    }
    
    // 가격 범위 필터링 (페이징 지원)
    public Page<Product> findByPriceRange(Double minPrice, Double maxPrice, Pageable pageable) {
        return productRepository.findByPriceRange(minPrice, maxPrice, pageable);
    }
    
    @Transactional
    public Product create(ProductCreateRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setDescription(request.getDescription());
        
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("카테고리", request.getCategoryId()));
            product.setCategory(category);
        }
        
        return productRepository.save(product);
    }
    
    @Transactional
    public Product update(Long id, ProductUpdateRequest request) {
        Product product = findById(id);
        
        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("카테고리", request.getCategoryId()));
            product.setCategory(category);
        }
        
        return productRepository.save(product);
    }
    
    @Transactional
    public void delete(Long id) {
        Product product = findById(id);
        productRepository.delete(product);
    }
}

