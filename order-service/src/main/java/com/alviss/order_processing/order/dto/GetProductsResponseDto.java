package com.alviss.order_processing.order.dto;

import java.util.List;

public class GetProductsResponseDto {
    private List<ProductDto> products;
    private int totalPages;
    private int currentPage;
    private ErrorDto error;

    public GetProductsResponseDto() {}

    public GetProductsResponseDto(List<ProductDto> products, int totalPages, int currentPage, ErrorDto error) {
        this.products = products;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
        this.error = error;
    }

    public List<ProductDto> getProducts() {
        return products;
    }

    public void setProducts(List<ProductDto> products) {
        this.products = products;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public ErrorDto getError() {
        return error;
    }

    public void setError(ErrorDto error) {
        this.error = error;
    }
}
