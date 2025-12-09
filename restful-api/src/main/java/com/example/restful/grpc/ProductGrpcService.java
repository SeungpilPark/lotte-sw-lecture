package com.example.restful.grpc;

import com.example.restful.dto.ProductCreateRequest;
import com.example.restful.dto.ProductUpdateRequest;
import com.example.restful.entity.Category;
import com.example.restful.entity.Product;
import com.example.restful.service.ProductService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class ProductGrpcService extends ProductServiceGrpc.ProductServiceImplBase {
    
    private final ProductService productService;
    
    @Override
    public void getProduct(GetProductRequest request, StreamObserver<ProductResponse> responseObserver) {
        try {
            log.debug("gRPC GetProduct request: id={}", request.getId());
            Product product = productService.findById(request.getId());
            
            ProductResponse.Builder responseBuilder = ProductResponse.newBuilder()
                .setId(product.getId())
                .setName(product.getName())
                .setPrice(product.getPrice())
                .setDescription(product.getDescription() != null ? product.getDescription() : "");
            
            if (product.getCategory() != null) {
                responseBuilder.setCategory(CategoryInfo.newBuilder()
                    .setId(product.getCategory().getId())
                    .setName(product.getCategory().getName())
                    .setDescription(product.getCategory().getDescription() != null ? product.getCategory().getDescription() : "")
                    .build());
            }
            
            ProductResponse response = responseBuilder.build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            log.debug("gRPC GetProduct response: id={}, name={}", response.getId(), response.getName());
        } catch (Exception e) {
            log.error("gRPC GetProduct error: id={}, error={}", request.getId(), e.getMessage(), e);
            responseObserver.onError(Status.INTERNAL
                .withDescription("제품 조회 실패: " + e.getMessage())
                .withCause(e)
                .asRuntimeException());
        }
    }
    
    @Override
    public void getProducts(GetProductsRequest request, StreamObserver<ProductListResponse> responseObserver) {
        try {
            int page = request.getPage();
            int size = request.getSize() > 0 ? request.getSize() : 10;
            String sortParam = request.getSort().isEmpty() ? "id,asc" : request.getSort();
            
            String[] sortParams = sortParam.split(",");
            String sortField = sortParams[0];
            Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1]) 
                ? Sort.Direction.DESC 
                : Sort.Direction.ASC;
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
            Page<Product> productPage = productService.findAll(pageable);
            
            ProductListResponse.Builder responseBuilder = ProductListResponse.newBuilder()
                .setPage(productPage.getNumber())
                .setSize(productPage.getSize())
                .setTotalElements(productPage.getTotalElements())
                .setTotalPages(productPage.getTotalPages());
            
            for (Product product : productPage.getContent()) {
                ProductResponse.Builder productResponseBuilder = ProductResponse.newBuilder()
                    .setId(product.getId())
                    .setName(product.getName())
                    .setPrice(product.getPrice())
                    .setDescription(product.getDescription() != null ? product.getDescription() : "");
                
                if (product.getCategory() != null) {
                    productResponseBuilder.setCategory(CategoryInfo.newBuilder()
                        .setId(product.getCategory().getId())
                        .setName(product.getCategory().getName())
                        .setDescription(product.getCategory().getDescription() != null ? product.getCategory().getDescription() : "")
                        .build());
                }
                
                responseBuilder.addProducts(productResponseBuilder.build());
            }
            
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("gRPC GetProducts error: {}", e.getMessage(), e);
            responseObserver.onError(Status.INTERNAL
                .withDescription("제품 목록 조회 실패: " + e.getMessage())
                .withCause(e)
                .asRuntimeException());
        }
    }
    
    @Override
    public void createProduct(CreateProductRequest request, StreamObserver<ProductResponse> responseObserver) {
        try {
            ProductCreateRequest createRequest = new ProductCreateRequest();
            createRequest.setName(request.getName());
            createRequest.setPrice(request.getPrice());
            createRequest.setCategoryId(request.getCategoryId() > 0 ? request.getCategoryId() : null);
            createRequest.setDescription(request.getDescription());
            
            Product product = productService.create(createRequest);
            ProductResponse.Builder responseBuilder = ProductResponse.newBuilder()
                .setId(product.getId())
                .setName(product.getName())
                .setPrice(product.getPrice())
                .setDescription(product.getDescription() != null ? product.getDescription() : "");
            
            if (product.getCategory() != null) {
                responseBuilder.setCategory(CategoryInfo.newBuilder()
                    .setId(product.getCategory().getId())
                    .setName(product.getCategory().getName())
                    .setDescription(product.getCategory().getDescription() != null ? product.getCategory().getDescription() : "")
                    .build());
            }
            
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("gRPC CreateProduct error: {}", e.getMessage(), e);
            responseObserver.onError(Status.INTERNAL
                .withDescription("제품 생성 실패: " + e.getMessage())
                .withCause(e)
                .asRuntimeException());
        }
    }
    
    @Override
    public void updateProduct(UpdateProductRequest request, StreamObserver<ProductResponse> responseObserver) {
        try {
            ProductUpdateRequest updateRequest = new ProductUpdateRequest();
            updateRequest.setName(request.getName().isEmpty() ? null : request.getName());
            updateRequest.setPrice(request.getPrice() > 0 ? request.getPrice() : null);
            updateRequest.setCategoryId(request.getCategoryId() > 0 ? request.getCategoryId() : null);
            updateRequest.setDescription(request.getDescription().isEmpty() ? null : request.getDescription());
            
            Product product = productService.update(request.getId(), updateRequest);
            ProductResponse.Builder responseBuilder = ProductResponse.newBuilder()
                .setId(product.getId())
                .setName(product.getName())
                .setPrice(product.getPrice())
                .setDescription(product.getDescription() != null ? product.getDescription() : "");
            
            if (product.getCategory() != null) {
                responseBuilder.setCategory(CategoryInfo.newBuilder()
                    .setId(product.getCategory().getId())
                    .setName(product.getCategory().getName())
                    .setDescription(product.getCategory().getDescription() != null ? product.getCategory().getDescription() : "")
                    .build());
            }
            
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("gRPC UpdateProduct error: id={}, error={}", request.getId(), e.getMessage(), e);
            responseObserver.onError(Status.INTERNAL
                .withDescription("제품 수정 실패: " + e.getMessage())
                .withCause(e)
                .asRuntimeException());
        }
    }
    
    @Override
    public void deleteProduct(DeleteProductRequest request, StreamObserver<DeleteProductResponse> responseObserver) {
        try {
            productService.delete(request.getId());
            DeleteProductResponse response = DeleteProductResponse.newBuilder()
                .setSuccess(true)
                .setMessage("제품이 삭제되었습니다")
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            DeleteProductResponse response = DeleteProductResponse.newBuilder()
                .setSuccess(false)
                .setMessage("제품 삭제 실패: " + e.getMessage())
                .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}

