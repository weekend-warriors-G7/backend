package com.weekendwarriors.weekend_warriors_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weekendwarriors.weekend_warriors_backend.dto.UserDTO;
import com.weekendwarriors.weekend_warriors_backend.enums.ProductStatus;
import com.weekendwarriors.weekend_warriors_backend.exception.InvalidToken;
import com.weekendwarriors.weekend_warriors_backend.model.Product;
import com.weekendwarriors.weekend_warriors_backend.service.ProductService;
import com.weekendwarriors.weekend_warriors_backend.dto.ProductDTO;

import com.weekendwarriors.weekend_warriors_backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/products")
public class ProductController
{
    private final ProductService productService;
    private final UserService userService;

    @Autowired
    public ProductController(ProductService productService, UserService userService)
    {
        this.productService = productService;
        this.userService = userService;
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

    @GetMapping("/user")
    public ResponseEntity<?> getProductsOwnedByUser(HttpServletRequest request) {
        try {
            // Extract the token from the request
            String token = userService.getJwtTokenFromRequest(request);

            // Validate the token and retrieve the user information
            UserDTO user = userService.getUser(token);



            // Fetch the products owned by the user
            List<Product> products = productService.getProductsOwnedByUser(user.getId());
            Map<String, List<Product>> categorizedProducts = new HashMap<>();

            categorizedProducts.put("Approved", new ArrayList<>());
            categorizedProducts.put("Pending", new ArrayList<>());
            categorizedProducts.put("Rejected", new ArrayList<>());

            for (Product product : products) {
                switch (product.getStatus()) {
                    case ProductStatus.APROVED:
                        categorizedProducts.get("Approved").add(product);
                        break;
                    case ProductStatus.PENDING:
                        categorizedProducts.get("Pending").add(product);
                        break;
                    case ProductStatus.REJECTED:
                        categorizedProducts.get("Rejected").add(product);
                        break;
                    default:
                        break;
                }
            }
            // Return the products in the response
            return ResponseEntity.ok(categorizedProducts);
        } catch (InvalidToken invalidToken) {
            // Handle token validation errors
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", invalidToken.getMessage()));
        } catch (IOException e) {
            // Handle other exceptions
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "An error occurred while processing the request."));
        }
    }



    @PostMapping(value = "/add", consumes = {"multipart/form-data"})
    public ResponseEntity<?> addProduct( @RequestPart("product") String productJson, @RequestPart("image") MultipartFile image,HttpServletRequest request)
    {
        try
        {
            String token = userService.getJwtTokenFromRequest(request);

            // Validate the token and retrieve the user information
            UserDTO user = userService.getUser(token);

            ObjectMapper objectMapper = new ObjectMapper();
            ProductDTO productDTO = objectMapper.readValue(productJson, ProductDTO.class);
            String imageId = productService.uploadProductImage(image);
            productDTO.setImageId(imageId);
            productDTO.setSellerId(user.getId());
            productDTO.setStatus(ProductStatus.PENDING);
           ProductDTO productAdded = productService.addProduct(productDTO);
            return ResponseEntity.ok(productAdded);
        } catch (InvalidToken invalidToken) {
            // Handle token validation errors
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", invalidToken.getMessage()));
        } catch (IOException e) {
            // Handle other exceptions
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "An error occurred while processing the request."));
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

            @Parameter(description = "The sorting indicator. If it exists it will indicate that a sort needs to be done; true -> ascending, while false -> descending")
            @RequestParam(required = false) Boolean sortType,

            @Parameter(description = "The product status")
            @RequestParam(required = false) String status
    ) throws IOException
    {
        return this.productService.findProductsByCriteria(startingPrice, endingPrice, size, material, clothingType, colour, searchQuery, sortType, status);
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

    @Scheduled(fixedRate = 3600000)
    @PostConstruct
    public void checkImageIdsForProducts() throws IOException {
        productService.attemptToUpdateImageIds();
    }

    //forbidden
    @DeleteMapping("/final")
    public void terminateProducts()
    {
        productService.terminateProducts();
    }
}
