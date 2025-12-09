package com.example.restful.graphql;

import com.example.restful.entity.Category;
import com.example.restful.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CategoryResolver {
    
    private final CategoryService categoryService;
    
    @QueryMapping
    public Category category(@Argument Long id) {
        return categoryService.findById(id);
    }
    
    @QueryMapping
    public List<Category> categories() {
        return categoryService.findAll();
    }
}

