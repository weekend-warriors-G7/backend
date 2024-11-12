package com.weekendwarriors.weekend_warriors_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weekendwarriors.weekend_warriors_backend.model.Product;
import com.weekendwarriors.weekend_warriors_backend.service.ProductService;
import com.weekendwarriors.weekend_warriors_backend.dto.ProductDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController
{
    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService)
    {
        this.productService = productService;
    }

    @GetMapping("/all")
    public List<Product> getAllProducts() throws IOException {
        try
        {
            return productService.getAllProducts();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping("/{id}")
    public Product getProductById(@PathVariable String id) throws IOException {
        try
        {
            return productService.getProductById(id);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    @PostMapping(value = "/add", consumes = {"multipart/form-data"})
    public ProductDTO addProduct( @RequestPart("product") String productJson, @RequestPart("image") MultipartFile image)
    {
        try
        {
            ObjectMapper objectMapper = new ObjectMapper();
            ProductDTO productDTO = objectMapper.readValue(productJson, ProductDTO.class);
            String imageId = productService.uploadProductImage(image);
            productDTO.setImageId(imageId);
            return productService.addProduct(productDTO);
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    @PutMapping("/{id}/update")
    public ProductDTO updateProduct(@PathVariable String id, @RequestPart("product") String productJson, @RequestPart(value = "image", required = false) MultipartFile image) {
        try
        {
            ObjectMapper objectMapper = new ObjectMapper();
            ProductDTO productDTO = objectMapper.readValue(productJson, ProductDTO.class);
            return productService.updateProduct(id, productDTO, image);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }


    @DeleteMapping("/{id}/delete")
    public void deleteProduct(@PathVariable String id)
    {
        try {
            productService.deleteProduct(id);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    @GetMapping("/images")
    public List<String> getAllProductImageLinks() {
        try
        {
            return productService.getAllProductImageLinks();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    //forbidden
    @DeleteMapping("/final")
    public void terminateProducts()
    {
        productService.terminateProducts();
    }
}
