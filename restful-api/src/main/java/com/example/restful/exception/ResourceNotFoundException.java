package com.example.restful.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String resource, Long id) {
        super(resource + "를 찾을 수 없습니다. ID: " + id);
    }
}

