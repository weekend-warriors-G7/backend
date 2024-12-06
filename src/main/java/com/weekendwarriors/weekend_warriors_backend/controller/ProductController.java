package com.weekendwarriors.weekend_warriors_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weekendwarriors.weekend_warriors_backend.model.Product;
import com.weekendwarriors.weekend_warriors_backend.service.ProductService;
import com.weekendwarriors.weekend_warriors_backend.dto.ProductDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    @Operation(
            summary = "Get products by criteria",
            description = "Retrieve a list of products filtered by the provided criteria: a range for price, denoted by 'startingPrice' and/or 'endingPrice', 'size', 'material', 'clothingType' and/or 'colour'",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved products"),
                    @ApiResponse(responseCode = "400", description = "Bad request, invalid parameters"),
                    @ApiResponse(responseCode = "500", description = "Internal server error"),
                    @ApiResponse(responseCode = "403", description = "Forbidden, access denied")
            }
    )
    @GetMapping("")
    public List<Product> getProductsByCriteria(
            @Parameter(description = "The starting price filter (not mandatory)")
            @RequestParam(required = false) Double startingPrice,

            @Parameter(description = "The ending price filter (not mandatory)")
            @RequestParam(required = false) Double endingPrice,

            @Parameter(description = "The size filter (not mandatory)")
            @RequestParam(required = false) String size,

            @Parameter(description = "The material filter (not mandatory)")
            @RequestParam(required = false) String material,

            @Parameter(description = "The clothing type filter (not mandatory)")
            @RequestParam(required = false) String clothingType,

            @Parameter(description = "The color filter (not mandatory)")
            @RequestParam(required = false) String colour,

            @Parameter(description = "The search query")
            @RequestParam(required = false) String searchQuery,

            @Parameter(description = "The sorting indicator (ascending/descending)")
            @RequestParam(required = false) String sortIndicator
    ) throws IOException
    {
        return this.productService.findProductsByCriteria(startingPrice, endingPrice, size, material, clothingType, colour, searchQuery, sortIndicator);
    }


    @Operation(
            summary = "Get all products similar to the one on whose page you are now",
            description = "It gives a list of all products that are similar to the one the user is looking at currently, maybe a loading should be implemented, as it takes a while for the code to get data to the ai and back and then interpret it itself.",
            responses =
                {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved search results"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
                }
    )
    @GetMapping("/compare/{id}")
    public List<Product> compareImages
            (
                @Parameter(description = "The id of product")
                @PathVariable String id
            )
    {
        try {
            Product sourceProduct = productService.getProductById(id);
            if (sourceProduct == null)
            {
                throw new RuntimeException("Product with ID " + id + " not found");
            }
            return productService.getAllRecommendedImages(id);

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to compare images: " + e.getMessage());
        }
    }

    //forbidden
    @DeleteMapping("/final")
    public void terminateProducts()
    {
        productService.terminateProducts();
    }
}
