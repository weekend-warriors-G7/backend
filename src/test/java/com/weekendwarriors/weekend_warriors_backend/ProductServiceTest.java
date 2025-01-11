package com.weekendwarriors.weekend_warriors_backend;

import com.weekendwarriors.weekend_warriors_backend.calls.ImageManagement;
import com.weekendwarriors.weekend_warriors_backend.enums.ProductStatus;
import com.weekendwarriors.weekend_warriors_backend.model.Product;
import com.weekendwarriors.weekend_warriors_backend.repository.ProductRepository;
import com.weekendwarriors.weekend_warriors_backend.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {
    @Mock(answer = Answers.RETURNS_MOCKS)
    private MongoTemplate mongoTemplate;

    @Mock
    private ImageManagement imageManagement;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private List<Product> mockProductList;

    @BeforeEach
    void setUp() {
        mockProductList = new ArrayList<>();
        mockProductList.add(new Product("0","1", "Blue shirt", 24.9, "Blue cotton shirt", "M", "Cotton", "Casual", "Blue", "imageId1", ProductStatus.PENDING));
        mockProductList.add(new Product("0","2", "Fancy Pants", 40.0, "Denim fancy pants", "L", "Denim", "Formal", "Black", "imageId2",ProductStatus.PENDING));
        mockProductList.add(new Product("0","3", "Skirt", 38.99, "Simple skirt", "S", "Denim", "Formal", "Black", "imageId3",ProductStatus.PENDING));
    }

    private boolean areTwoProductsEqual(Product product1, Product product2) {
        return product1.getId().equals(product2.getId()) &&
                product1.getSellerId().equals(product2.getSellerId()) &&
                product1.getName().equals(product2.getName()) &&
                product1.getPrice().equals(product2.getPrice()) &&
                product1.getDescription().equals(product2.getDescription()) &&
                product1.getSize().equals(product2.getSize()) &&
                product1.getMaterial().equals(product2.getMaterial()) &&
                product1.getClothingType().equals(product2.getClothingType()) &&
                product1.getColour().equals(product2.getColour())&&
                product1.getStatus().equals(product2.getStatus());
    }

    private boolean areTwoListsOfProductsEqual(List<Product> productList1, List<Product> productList2) {
        for (int i = 0; i < productList1.size(); i++) {
            if (!areTwoProductsEqual(productList1.get(i), productList2.get(i)))
                return false;
        }
        return true;
    }

    @Test
    public void findProductsByCriteria_validInput_returnsListOfProducts() throws IOException {
        //ARRANGE
        when(mongoTemplate.find(any(Query.class), eq(Product.class))).thenReturn(mockProductList);
        when(productRepository.findAll()).thenReturn(mockProductList);

        //ACT
        List<Product> returnedProducts = productService.findProductsByCriteria(20.0, 50.0, null, null, null, null, null, null,ProductStatus.PENDING.toString());

        //ASSERT
        assertNotNull(returnedProducts);
        assertEquals(returnedProducts.size(), mockProductList.size());
        assertTrue(areTwoListsOfProductsEqual(returnedProducts, mockProductList));
    }

    @Test
    public void findProductsByCriteria_validInputWithNoResults_returnsEmptyList() throws IOException {
        //ARRANGE
        when(mongoTemplate.find(any(Query.class), eq(Product.class))).thenReturn(List.of());

        //ACT
        List<Product> returned_products = productService.findProductsByCriteria(20.0, 50.0, null, null, null, null, null, null,"");

        //ASSERT
        assertNotNull(returned_products);
        assertEquals(returned_products.size(), 0);
    }
}
