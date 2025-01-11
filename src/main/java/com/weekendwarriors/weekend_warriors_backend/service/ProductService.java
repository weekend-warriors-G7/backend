package com.weekendwarriors.weekend_warriors_backend.service;

import com.weekendwarriors.weekend_warriors_backend.calls.ImageManagement;
import com.weekendwarriors.weekend_warriors_backend.dto.ProductDTO;
import com.weekendwarriors.weekend_warriors_backend.enums.ProductStatus;
import com.weekendwarriors.weekend_warriors_backend.model.Product;
import com.weekendwarriors.weekend_warriors_backend.repository.ProductRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final MongoTemplate mongoTemplate;

    private final ProductRepository productRepository;
    private ImageManagement imageManagement;
    private final String defaultImageLink = "https://i.imgur.com/YWDk8ZY.jpeg";


    @Autowired
    public ProductService(ProductRepository productRepository, ImageManagement imageManagement, MongoTemplate mongoTemplate) throws IOException
    {
        this.productRepository = productRepository;
        this.imageManagement = imageManagement;
        this.mongoTemplate = mongoTemplate;
    }


    public Product changeDtoToEntity(ProductDTO productDTO)
    {
        return new Product
                (
                        productDTO.getOwner_id(),
                        productDTO.getName(),
                        productDTO.getPrice(),
                        productDTO.getDescription(),
                        productDTO.getSize(),
                        productDTO.getMaterial(),
                        productDTO.getClothingType(),
                        productDTO.getColour(),
                        productDTO.getImageId(),
                        productDTO.getStatus()

                );
    }

    public ProductDTO ChangeEntityToDto(Product product)
    {
        return new ProductDTO
                (
                        product.getOwner_id(),
                        product.getName(),
                        product.getPrice(),
                        product.getDescription(),
                        product.getSize(),
                        product.getMaterial(),
                        product.getClothingType(),
                        product.getColour(),
                        product.getImageId(),
                        product.getStatus()
                );
    }


    public String uploadProductImage(MultipartFile image) throws IOException
    {
        File tempFile = File.createTempFile("upload-", image.getOriginalFilename());
        image.transferTo(tempFile);
        return imageManagement.uploadImageFile(tempFile);
    }

    private List<Product> setImageLinksToProducts(List<Product> givenListOfProducts) throws IOException
    {
        ArrayList<Product> allProductsWithLinks = new ArrayList<>();
        for(Product product : givenListOfProducts)
        {
            Product productWithlink = new Product
                    (
                            product.getId(),
                            product.getOwner_id(),
                            product.getName(),
                            product.getPrice(),
                            product.getDescription(),
                            product.getSize(),
                            product.getMaterial(),
                            product.getClothingType(),
                            product.getColour(),
                            imageManagement.getImageLink(product.getImageId()),
                            product.getStatus()
                    );
            allProductsWithLinks.add(productWithlink);
        }
        return allProductsWithLinks;
    }


    public ProductDTO addProduct(ProductDTO dto)
    {

        Product product = changeDtoToEntity(dto);
        Product savedProduct = productRepository.save(product);
        return ChangeEntityToDto(savedProduct);
    }

    public void deleteProduct(String id) throws IOException {
        Product product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        if(product.getImageId() != null && product.getImageId() != imageManagement.provideDefaultImageId())
            imageManagement.deleteImage(product.getImageId());
        productRepository.deleteById(id);
    }

    public ProductDTO updateProduct(String id, ProductDTO dto, MultipartFile image) throws IOException
    {
        Product product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        if (image != null && !image.isEmpty())
        {
            if(product.getImageId() != null)
                imageManagement.deleteImage(product.getImageId());
            String newImageId = uploadProductImage(image);
            product.setImageId(newImageId);
        }
        product.setName(dto.getName());
        product.setPrice(dto.getPrice());
        product.setDescription(dto.getDescription());
        product.setSize(dto.getSize().toLowerCase());
        product.setColour(dto.getColour().toLowerCase());
        product.setMaterial(dto.getMaterial().toLowerCase());
        product.setClothingType(dto.getClothingType().toLowerCase());
        if(dto.getStatus() != null)
            product.setStatus(dto.getStatus());

        Product updatedProduct = productRepository.save(product);

        return ChangeEntityToDto(updatedProduct);
    }

    public List<Product> getAllProducts() throws IOException
    {
        ArrayList<Product> allProductsWithLinks = new ArrayList<>();
        for(Product product : productRepository.findAll())
        {
            Product productWithlink = new Product
                    (
                            product.getId(),
                            product.getOwner_id(),
                            product.getName(),
                            product.getPrice(),
                            product.getDescription(),
                            product.getSize(),
                            product.getMaterial(),
                            product.getClothingType(),
                            product.getColour(),
                            imageManagement.getImageLink(product.getImageId()),
                            product.getStatus()
                    );
            allProductsWithLinks.add(productWithlink);
        }
        return allProductsWithLinks;
    }

    public Product getProductById(String id) throws IOException
    {
        Product product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        Product productWithlink = new Product
                (
                        product.getId(),
                        product.getOwner_id(),
                        product.getName(),
                        product.getPrice(),
                        product.getDescription(),
                        product.getSize(),
                        product.getMaterial(),
                        product.getClothingType(),
                        product.getColour(),
                        imageManagement.getImageLink(product.getImageId()),
                        product.getStatus()
                );
        return productWithlink;
    }

    public List<Product> findProductsByCriteria
            (
                    Double startingPrice,

                    Double endingPrice,
                    String size,
                    String material,
                    String clothingType,
                    String colour,
                    String searchQuery,
                    Boolean sortType,
                    String status
            )
            throws IOException
    {
        List<Product> filteredProducts = this.findProductsByCriteriaWithoutImageLink(startingPrice, endingPrice, size, material, clothingType, colour,status);


        List<Product> searchedProducts = this.searchProducts(searchQuery);
        if(searchedProducts!=productRepository.findAll())
        {
            Set<String> filteredIds = filteredProducts.stream().map(Product::getId).collect(Collectors.toSet());
            searchedProducts = searchedProducts.stream()
                    .filter(product -> filteredIds.contains(product.getId())).collect(Collectors.toList());

            return this.sortProductsByPrice(this.setImageLinksToProducts(searchedProducts), sortType);
        }
        else
        {
            return this.sortProductsByPrice(this.setImageLinksToProducts(filteredProducts), sortType);
        }
    }

    private List<Product> findProductsByCriteriaWithoutImageLink
            (
                    Double startingPrice,
                    Double endingPrice,
                    String size,
                    String material,
                    String clothingType,
                    String colour,
                    String status
            )
    {
        Query query = new Query();

        if(size != null && !size.isEmpty())
            query.addCriteria(Criteria.where("size").is(size));
        if (material != null && !material.isEmpty())
            query.addCriteria(Criteria.where("material").is(material));
        if (clothingType != null && !clothingType.isEmpty())
            query.addCriteria(Criteria.where("clothingType").is(clothingType));
        if (colour != null && !colour.isEmpty())
            query.addCriteria(Criteria.where("colour").is(colour));
        if (status != null && !status.isEmpty())
            query.addCriteria(Criteria.where("status").is(status));
        if (startingPrice != null || endingPrice != null)
        {
            Criteria priceCriteria = Criteria.where("price");
            if (startingPrice != null)
            {
                priceCriteria.gte(startingPrice);
            }
            if (endingPrice != null)
            {
                priceCriteria.lte(endingPrice);
            }
            query.addCriteria(priceCriteria);
        }

        return mongoTemplate.find(query, Product.class);
    }

    public List<Product> searchProducts(String searchInput) throws IOException
    {
        if (searchInput == null || searchInput.trim().isEmpty())
        {
            return productRepository.findAll();
        }

        Query nameQuery = new Query(Criteria.where("name").regex(searchInput, "i"));
        Query descriptionQuery = new Query(Criteria.where("description").regex(searchInput, "i"));

        List<Product> nameMatches = mongoTemplate.find(nameQuery, Product.class);
        List<String> matchedIds = nameMatches.stream().map(Product::getId).collect(Collectors.toList());

        if (!matchedIds.isEmpty())
        {
            descriptionQuery.addCriteria(Criteria.where("id").nin(matchedIds));
        }

        List<Product> descriptionMatches = mongoTemplate.find(descriptionQuery, Product.class);

        List<Product> combinedResults = new ArrayList<>(nameMatches);
        combinedResults.addAll(descriptionMatches);

        return combinedResults;
    }

    public List<Product> sortProductsByPrice(List<Product> products, Boolean sortType)
    {
        if (products == null || products.isEmpty())
        {
            return Collections.emptyList();
        }

        if(sortType == null)
            return products;

        Comparator<Product> comparator;
        if(sortType)
        {
            comparator = Comparator.comparing(Product::getPrice);
        }
        else if(!sortType)
        {
            comparator = Comparator.comparing(Product::getPrice).reversed();
        }
        else
        {
            return products;
        }

        return products.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    public List<Product> getAllRecommendedImages(String productImageToCompareToId) throws IOException
    {
        try
        {
            Map<String, String> ImageLinkToProductIdMapper = new LinkedHashMap<>();
            List<String> onlyImageLinks = new LinkedList<>();
            List<String> productsWithDefaultImage = new LinkedList<>();

            for (Product p : productRepository.findAll())
            {
                if (!Objects.equals(p.getId(), productImageToCompareToId) && Objects.equals(p.getStatus(),ProductStatus.APROVED))
                {
                    String id = p.getId();
                    String imageLink = imageManagement.getImageLink(p.getImageId());

                    if (Objects.equals(imageLink, defaultImageLink)) {
                        productsWithDefaultImage.add(id);
                    } else {
                        ImageLinkToProductIdMapper.put(imageLink, id);
                    }
                    onlyImageLinks.add(imageLink);
                }
            }


            String imageLinkOfComparisonProduct = imageManagement.getImageLink(productRepository.findById(productImageToCompareToId).orElse(productRepository.findAll().getFirst()).getImageId());

            JSONObject requestBody = new JSONObject();
            requestBody.put("source", imageLinkOfComparisonProduct);
            requestBody.put("others", onlyImageLinks);

            WebClient webClient = WebClient.create();
            String response = webClient.post()
                    .uri("http://127.0.0.1:5000/compare")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody.toString())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JSONArray jsonResponse = new JSONArray(response);
            List<String> matchedAndSortedImageLinks = jsonResponse.toList().stream()
                    .map(entry -> ((Map<String, Object>) entry).get("id").toString())
                    .collect(Collectors.toList());

            List<Product> matchedProducts = new LinkedList<>();
            for (String imageLink : matchedAndSortedImageLinks)
            {
                if (Objects.equals(imageLink, defaultImageLink))
                {
                    String productId = productsWithDefaultImage.getFirst();
                    Product p = this.getProductById(productId);
                    matchedProducts.add(p);
                    productsWithDefaultImage.removeFirst();
                }
                else
                {
                    String productId = ImageLinkToProductIdMapper.get(imageLink);
                    Product p = this.getProductById(productId);
                    matchedProducts.add(p);
                }
            }
            return matchedProducts;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Failed to compare images: " + e.getMessage());
        }
    }

    public void attemptToUpdateImageIds() throws IOException
    {
        for(Product p : productRepository.findAll())
        {
            p.setImageId(imageManagement.imageExists(p.getImageId()) ? p.getImageId() : imageManagement.provideDefaultImageId());
            productRepository.save(p);
        }
    }

    public void terminateProducts()
    {
        for(Product p : productRepository.findAll())
            productRepository.deleteById(p.getId());
    }

    public List<Product> getProductsOwnedByUser(String userId) {
        try {
            Query query = new Query();
            query.addCriteria(Criteria.where("owner_id").is(userId)); // Add filter based on ownerId
            List<Product> products= mongoTemplate.find(query, Product.class);
            products = products.stream()
                    .map(product -> {
                        try {
                            // Update the image ID with the fetched image link
                            product.setImageId(imageManagement.getImageLink(product.getImageId()));
                            return product;
                        } catch (IOException e) {
                            // Handle exceptions properly, e.g., log or rethrow
                            e.printStackTrace();
                            throw new RuntimeException("Failed to fetch image link for product: " + product.getId(), e);
                        }
                    })
                    .collect(Collectors.toList());

            return products;

        } catch (Exception e) {
            // Log the exception and rethrow it if necessary
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch products for user ID: " + userId, e);
        }
    }
}