package com.project.stress_traffic_system.product.service;

import com.project.stress_traffic_system.members.entity.Members;
import com.project.stress_traffic_system.product.model.Product;
import com.project.stress_traffic_system.product.model.dto.ProductResponseDto;
import com.project.stress_traffic_system.product.model.dto.ProductSearchCondition;
import com.project.stress_traffic_system.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final int PAGE_SIZE = 10;
    private final String SORT_BY = "date";

    //todo param Members 없앨지 정하기

    //전체상품 가져오기
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getProducts(Members member, int page) {

        // 페이징 처리
        Sort.Direction direction = Sort.Direction.ASC;
        Sort sort = Sort.by(direction, SORT_BY);
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, sort);

        //모든 상품 10개 단위로 가져오기
        Page<Product> products = productRepository.findAll(pageable);

        //dto로 변환하기
        return new PageImpl<>(products.stream().map(product -> ProductResponseDto.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .price(product.getPrice())
                        .description(product.getDescription())
                        .shippingFee(product.getShippingFee())
                        .imgurl(product.getImgurl())
                        .count(product.getCount())
                        .stock(product.getStock())
                        .introduction(product.getIntroduction())
                        .pages(product.getPages())
                        .date(product.getDate())
                        .build())
                .collect(Collectors.toList()));
    }

    //상품 id로 상세정보 가져오기
    @Transactional(readOnly = true)
    public ProductResponseDto getProduct(Members member, Long productId) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다"));
        return ProductResponseDto.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .description(product.getDescription())
                .shippingFee(product.getShippingFee())
                .imgurl(product.getImgurl())
                .count(product.getCount())
                .stock(product.getStock())
                .introduction(product.getIntroduction())
                .pages(product.getPages())
                .date(product.getDate())
                .build();

    }

    // 상품 검색하기 (이름, 가격 필터링)
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> searchProducts(Members member, ProductSearchCondition condition, int page) {
        return productRepository.searchProducts(condition, page);
    }

    /*
        카테고리 1~5 각각 조회하는 Api
     */
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> searchByCategory(Members member, Long categoryId, int page) {
        return productRepository.searchByCategory(categoryId, page);
    }
}
