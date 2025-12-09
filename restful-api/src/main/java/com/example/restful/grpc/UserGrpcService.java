package com.example.restful.grpc;

import com.example.restful.dto.UserCreateRequest;
import com.example.restful.entity.User;
import com.example.restful.service.UserService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@GrpcService
@RequiredArgsConstructor
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {
    
    private final UserService userService;
    
    @Override
    public void getUser(GetUserRequest request, StreamObserver<UserResponse> responseObserver) {
        try {
            User user = userService.findById(request.getId());
            UserResponse response = UserResponse.newBuilder()
                .setId(user.getId())
                .setUsername(user.getUsername())
                .setEmail(user.getEmail())
                .setName(user.getName())
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }
    
    @Override
    public void getUsers(GetUsersRequest request, StreamObserver<UserListResponse> responseObserver) {
        try {
            int page = request.getPage();
            int size = request.getSize() > 0 ? request.getSize() : 10;
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
            Page<User> userPage = userService.findAll(pageable);
            
            UserListResponse.Builder responseBuilder = UserListResponse.newBuilder()
                .setPage(userPage.getNumber())
                .setSize(userPage.getSize())
                .setTotalElements(userPage.getTotalElements())
                .setTotalPages(userPage.getTotalPages());
            
            for (User user : userPage.getContent()) {
                UserResponse userResponse = UserResponse.newBuilder()
                    .setId(user.getId())
                    .setUsername(user.getUsername())
                    .setEmail(user.getEmail())
                    .setName(user.getName())
                    .build();
                responseBuilder.addUsers(userResponse);
            }
            
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }
    
    @Override
    public void createUser(CreateUserRequest request, StreamObserver<UserResponse> responseObserver) {
        try {
            UserCreateRequest createRequest = new UserCreateRequest();
            createRequest.setUsername(request.getUsername());
            createRequest.setEmail(request.getEmail());
            createRequest.setName(request.getName());
            
            User user = userService.create(createRequest);
            UserResponse response = UserResponse.newBuilder()
                .setId(user.getId())
                .setUsername(user.getUsername())
                .setEmail(user.getEmail())
                .setName(user.getName())
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }
    
    @Override
    public void updateUser(UpdateUserRequest request, StreamObserver<UserResponse> responseObserver) {
        try {
            UserCreateRequest updateRequest = new UserCreateRequest();
            updateRequest.setUsername(request.getUsername());
            updateRequest.setEmail(request.getEmail());
            updateRequest.setName(request.getName());
            
            User user = userService.update(request.getId(), updateRequest);
            UserResponse response = UserResponse.newBuilder()
                .setId(user.getId())
                .setUsername(user.getUsername())
                .setEmail(user.getEmail())
                .setName(user.getName())
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }
    
    @Override
    public void deleteUser(DeleteUserRequest request, StreamObserver<DeleteUserResponse> responseObserver) {
        try {
            userService.delete(request.getId());
            DeleteUserResponse response = DeleteUserResponse.newBuilder()
                .setSuccess(true)
                .setMessage("사용자가 삭제되었습니다")
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            DeleteUserResponse response = DeleteUserResponse.newBuilder()
                .setSuccess(false)
                .setMessage("사용자 삭제 실패: " + e.getMessage())
                .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}

