package com.weekendwarriors.weekend_warriors_backend.service;

import com.weekendwarriors.weekend_warriors_backend.dto.ProductDTO;
import com.weekendwarriors.weekend_warriors_backend.model.Product;
import com.weekendwarriors.weekend_warriors_backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product changeDtoToEntity(ProductDTO productDTO)
    {
        return new Product
                (
                    productDTO.getName(),
                    productDTO.getPrice(),
                    productDTO.getDescription(),
                    productDTO.getTags()
                );
    }

    public ProductDTO ChangeEntityToDto(Product product)
    {
        return new ProductDTO
            (
                product.getName(),
                product.getPrice(),
                product.getDescription(),
                product.getTags()
            );
    }

    public ProductDTO addProduct(ProductDTO dto)
    {
        Product product = changeDtoToEntity(dto);
        Product savedProduct = productRepository.save(product);
        return ChangeEntityToDto(savedProduct);
    }

    public List<Product> getAllProducts()
    {
        return new ArrayList<>(productRepository.findAll());
    }

    public Product getProductById(String id)
    {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    public ProductDTO updateProduct(String id, ProductDTO dto)
    {
        return productRepository.findById(id).map(product ->
        {
            product.setName(dto.getName());
            product.setPrice(dto.getPrice());
            product.setDescription(dto.getDescription());
            product.setTags(dto.getTags().stream().map(String::toLowerCase).collect(Collectors.toList()));
            return ChangeEntityToDto(productRepository.save(product));
        }
        ).orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    public void deleteProduct(String id)
    {
        productRepository.deleteById(id);
    }
}
