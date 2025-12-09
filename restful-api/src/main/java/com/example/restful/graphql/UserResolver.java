package com.example.restful.graphql;

import com.example.restful.dto.UserCreateRequest;
import com.example.restful.entity.User;
import com.example.restful.service.UserService;
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
public class UserResolver {
    
    private final UserService userService;
    
    @QueryMapping
    public User user(@Argument Long id) {
        return userService.findById(id);
    }
    
    @QueryMapping
    public UserPage users(@Argument Integer page, @Argument Integer size) {
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 10;
        
        Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.by(Sort.Direction.ASC, "id"));
        Page<User> userPage = userService.findAll(pageable);
        
        return UserPage.from(userPage);
    }
    
    @MutationMapping
    public User createUser(@Argument UserInput input) {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername(input.username());
        request.setEmail(input.email());
        request.setName(input.name());
        
        return userService.create(request);
    }
    
    @MutationMapping
    public User updateUser(@Argument Long id, @Argument UserInput input) {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername(input.username());
        request.setEmail(input.email());
        request.setName(input.name());
        
        return userService.update(id, request);
    }
    
    @MutationMapping
    public Boolean deleteUser(@Argument Long id) {
        userService.delete(id);
        return true;
    }
    
    public record UserInput(String username, String email, String name) {}
}

