package com.example.restful.graphql;

import com.example.restful.dto.ProductCreateRequest;
import com.example.restful.dto.ProductUpdateRequest;
import com.example.restful.entity.Product;
import com.example.restful.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ProductResolver {
    
    private final ProductService productService;
    
    @QueryMapping
    public Product product(@Argument Long id) {
        return productService.findById(id);
    }
    
    @QueryMapping
    public ProductPage products(
            @Argument Integer page,
            @Argument Integer size,
            @Argument String sort) {
        
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 10;
        String sortParam = sort != null ? sort : "id,asc";
        
        String[] sortParams = sortParam.split(",");
        String sortField = sortParams[0];
        Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1]) 
            ? Sort.Direction.DESC 
            : Sort.Direction.ASC;
        
        Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.by(direction, sortField));
        Page<Product> productPage = productService.findAll(pageable);
        
        return ProductPage.from(productPage);
    }
    
    @MutationMapping
    public Product createProduct(@Argument ProductInput input) {
        ProductCreateRequest request = new ProductCreateRequest();
        request.setName(input.name());
        request.setPrice(input.price());
        request.setCategoryId(input.categoryId());
        request.setDescription(input.description());
        
        return productService.create(request);
    }
    
    @MutationMapping
    public Product updateProduct(@Argument Long id, @Argument ProductUpdateInput input) {
        ProductUpdateRequest request = new ProductUpdateRequest();
        request.setName(input.name());
        request.setPrice(input.price());
        request.setCategoryId(input.categoryId());
        request.setDescription(input.description());
        
        return productService.update(id, request);
    }
    
    @MutationMapping
    public Boolean deleteProduct(@Argument Long id) {
        productService.delete(id);
        return true;
    }
    
    public record ProductInput(String name, Double price, Long categoryId, String description) {}
    
    public record ProductUpdateInput(String name, Double price, Long categoryId, String description) {}
}

