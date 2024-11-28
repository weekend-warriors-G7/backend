package com.weekendwarriors.weekend_warriors_backend.service;

import com.weekendwarriors.weekend_warriors_backend.calls.ImageManagement;
import com.weekendwarriors.weekend_warriors_backend.dto.ProductDTO;
import com.weekendwarriors.weekend_warriors_backend.model.Product;
import com.weekendwarriors.weekend_warriors_backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {
    private final MongoTemplate mongoTemplate;

    private final ProductRepository productRepository;
    private ImageManagement imageManagement;

    @Autowired
    public ProductService(ProductRepository productRepository, ImageManagement imageManagement, MongoTemplate mongoTemplate) throws IOException
    {
        this.productRepository = productRepository;
        this.imageManagement = imageManagement;
        this.mongoTemplate = mongoTemplate;
    }

    public String uploadProductImage(MultipartFile image) throws IOException
    {
        File tempFile = File.createTempFile("upload-", image.getOriginalFilename());
        image.transferTo(tempFile);
        return imageManagement.uploadImageFile(tempFile);
    }

    public Product changeDtoToEntity(ProductDTO productDTO)
    {
        return new Product
                (
                        productDTO.getName(),
                        productDTO.getPrice(),
                        productDTO.getDescription(),
                        productDTO.getSize(),
                        productDTO.getMaterial(),
                        productDTO.getClothingType(),
                        productDTO.getColour(),
                        productDTO.getImageId()
                );
    }

    public ProductDTO ChangeEntityToDto(Product product)
    {
        return new ProductDTO
            (
                product.getName(),
                product.getPrice(),
                product.getDescription(),
                product.getSize(),
                product.getMaterial(),
                product.getClothingType(),
                product.getColour(),
                product.getImageId()
            );
    }

    public ProductDTO addProduct(ProductDTO dto)
    {
        Product product = changeDtoToEntity(dto);
        Product savedProduct = productRepository.save(product);
        return ChangeEntityToDto(savedProduct);
    }

    public void deleteProduct(String id) throws IOException {
        Product product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        if (product.getImageId() != null)
        {
            Boolean deleted = imageManagement.deleteImage(product.getImageId());
            if (deleted)
            {
                productRepository.deleteById(id);
            }
            else
            {
                throw new RuntimeException("Failed to delete image from Imgur.");
            }
        }
        else
        {
            productRepository.deleteById(id);
        }
    }

    public void terminateProducts()
    {
        for(Product p : productRepository.findAll())
            productRepository.deleteById(p.getId());
    }

    public ProductDTO updateProduct(String id, ProductDTO dto, MultipartFile image) throws IOException
    {
        Product product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        if (image != null && !image.isEmpty())
        {
            if (product.getImageId() != null && !product.getImageId().isEmpty())
            {
                Boolean deleted = imageManagement.deleteImage(product.getImageId());
                if (!deleted)
                {
                    throw new RuntimeException("Failed to delete old image from Imgur.");
                }
            }
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

        Product updatedProduct = productRepository.save(product);

        return ChangeEntityToDto(updatedProduct);
    }

    public List<Product> getAllProducts() throws IOException
    {
        ArrayList<Product> allProductsWithLinks = new ArrayList<>();
        for(Product product : productRepository.findAll())
        {
            Product productWithlink = new Product(
                    product.getId(),
                    product.getName(),
                    product.getPrice(),
                    product.getDescription(),
                    product.getSize(),
                    product.getMaterial(),
                    product.getClothingType(),
                    product.getColour(),
                    imageManagement.getImageLink(product.getImageId())
            );
            allProductsWithLinks.add(productWithlink);
        }
        return allProductsWithLinks;
    }

    public Product getProductById(String id) throws IOException
    {
        Product product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        Product productWithlink = new Product(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getDescription(),
                product.getSize(),
                product.getMaterial(),
                product.getClothingType(),
                product.getColour(),
                imageManagement.getImageLink(product.getImageId())
        );
        return productWithlink;
    }

    public List<String> getAllProductImageLinks() throws IOException {
        List<String> imageLinks = new ArrayList<>();
        for (Product product : productRepository.findAll()) {
            if (product.getImageId() != null && !product.getImageId().isEmpty()) {
                String imageLink = imageManagement.getImageLink(product.getImageId());
                if (imageLink != null) {
                    imageLinks.add(imageLink);
                }
            }
        }
        return imageLinks;
    }

    public List<Product> findProductsByCriteria(
            Double startingPrice,
            Double endingPrice,
            String size,
            String material,
            String clothingType,
            String colour
    ) throws IOException {
        return setImageLinksToProducts(findProductsByCriteriaWithoutImageLink(
                startingPrice,
                endingPrice,
                size,
                material,
                clothingType,
                colour
        ));
    }

    private List<Product> findProductsByCriteriaWithoutImageLink(
            Double startingPrice,
            Double endingPrice,
            String size,
            String material,
            String clothingType,
            String colour
    ) {
        Query query = new Query();

        if(size != null && !size.isEmpty())
            query.addCriteria(Criteria.where("size").is(size));
        if (material != null && !material.isEmpty())
            query.addCriteria(Criteria.where("material").is(material));
        if (clothingType != null && !clothingType.isEmpty())
            query.addCriteria(Criteria.where("clothingType").is(clothingType));
        if (colour != null && !colour.isEmpty())
            query.addCriteria(Criteria.where("colour").is(colour));
        if (startingPrice != null || endingPrice != null) {
            Criteria priceCriteria = Criteria.where("price");
            if (startingPrice != null) {
                priceCriteria.gte(startingPrice);
            }
            if (endingPrice != null) {
                priceCriteria.lte(endingPrice);
            }
            query.addCriteria(priceCriteria);
        }

        return mongoTemplate.find(query, Product.class);
    }

    private List<Product> setImageLinksToProducts(List<Product> givenListOfProducts) throws IOException {
        ArrayList<Product> allProductsWithLinks = new ArrayList<>();
        for(Product product : givenListOfProducts)
        {
            Product productWithlink = new Product(
                    product.getId(),
                    product.getName(),
                    product.getPrice(),
                    product.getDescription(),
                    product.getSize(),
                    product.getMaterial(),
                    product.getClothingType(),
                    product.getColour(),
                    imageManagement.getImageLink(product.getImageId())
            );
            allProductsWithLinks.add(productWithlink);
        }
        return allProductsWithLinks;
    }
}