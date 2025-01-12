package com.weekendwarriors.weekend_warriors_backend;

import com.weekendwarriors.weekend_warriors_backend.calls.ImageManagement;
import com.weekendwarriors.weekend_warriors_backend.dto.ProductDTO;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock(answer = Answers.RETURNS_MOCKS)
    private MongoTemplate mongoTemplate;

    @Mock
    private ImageManagement imageManagement;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private MultipartFile mockImage;

    @InjectMocks
    private ProductService productService;

    private List<Product> mockProductList;

    @BeforeEach
    void setUp() {
        mockProductList = new ArrayList<>();
        mockProductList.add(new Product("1","1", "Blue shirt", 24.9, "Blue cotton shirt", "M", "Cotton", "Casual", "Blue", "imageId1", ProductStatus.PENDING));
        mockProductList.add(new Product("2","2", "Fancy Pants", 40.0, "Denim fancy pants", "L", "Denim", "Formal", "Black", "imageId2", ProductStatus.APROVED));
        mockProductList.add(new Product("3","3", "Skirt", 38.99, "Simple skirt", "S", "Denim", "Formal", "Black", "imageId3", ProductStatus.PENDING));
    };

    @Test
    public void addProduct_validInput_savesAndReturnsProduct() throws IOException
    {
        // ARRANGE
        Product newProduct = mockProductList.getFirst();
        when(productRepository.save(any(Product.class))).thenReturn(newProduct);

        // ACT
        ProductDTO savedProductDTO = productService.addProduct(new ProductDTO("1", "Blue shirt", 24.9, "Blue cotton shirt", "M", "Cotton", "Casual", "Blue", "imageId1", ProductStatus.PENDING));
        ProductDTO newProductDTO = productService.ChangeEntityToDto(newProduct);

        // ASSERT
        assertNotNull(savedProductDTO);
        assertEquals(newProductDTO.getClass(), savedProductDTO.getClass());
        assertEquals(newProductDTO.getOwner_id(), savedProductDTO.getOwner_id());
        assertEquals(newProductDTO.getName(), savedProductDTO.getName());
        assertEquals(newProductDTO.getStatus(), savedProductDTO.getStatus());
        assertEquals(newProductDTO.getImageId(), savedProductDTO.getImageId());
        assertEquals(newProductDTO.getColour(), savedProductDTO.getColour());
        assertEquals(newProductDTO.getSize(), savedProductDTO.getSize());
        assertEquals(newProductDTO.getDescription(), savedProductDTO.getDescription());
        assertEquals(newProductDTO.getPrice(), savedProductDTO.getPrice());
        assertEquals(newProductDTO.getMaterial(), savedProductDTO.getMaterial());
        assertEquals(newProductDTO.getClothingType(), savedProductDTO.getClothingType());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    private boolean areTwoProductsEqual(Product product1, Product product2)
    {
        return product1.getId().equals(product2.getId()) &&
                product1.getOwner_id().equals(product2.getOwner_id()) &&
                product1.getName().equals(product2.getName()) &&
                product1.getPrice().equals(product2.getPrice()) &&
                product1.getDescription().equals(product2.getDescription()) &&
                product1.getSize().equals(product2.getSize()) &&
                product1.getMaterial().equals(product2.getMaterial()) &&
                product1.getClothingType().equals(product2.getClothingType()) &&
                product1.getColour().equals(product2.getColour())&&
                product1.getStatus().equals(product2.getStatus());
    }

    @Test
    public void deleteProduct_validId_deletesProduct() throws IOException
    {
        // ARRANGE
        Product productToDelete = mockProductList.get(0);
        when(productRepository.findById("1")).thenReturn(Optional.of(productToDelete));

        // ACT
        productService.deleteProduct("1");

        // ASSERT
        verify(productRepository, times(1)).deleteById("1");
    }

    @Test
    public void deleteProduct_invalidId_throwsException()
    {
        // ARRANGE
        when(productRepository.findById("invalidId")).thenReturn(Optional.empty());

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> productService.deleteProduct("invalidId"));
        assertEquals("Product not found with id: invalidId", exception.getMessage());
    }

    private boolean areTwoListsOfProductsEqual(List<Product> productList1, List<Product> productList2) {
        for (int i = 0; i < productList1.size(); i++) {
            if (!areTwoProductsEqual(productList1.get(i), productList2.get(i)))
                return false;
        }
        return true;
    }

    @Test
    public void updateProduct_validInput_updatesAndReturnsProduct() throws IOException
    {
        // ARRANGE
        Product existingProduct = mockProductList.getFirst();
        when(productRepository.findById("1")).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);
        when(mockImage.isEmpty()).thenReturn(false);

        // ACT
        ProductDTO updatedProduct = productService.updateProduct("1", new ProductDTO("1", "Updated Shirt", 30.0, "Updated description", "L", "Cotton", "Casual", "Red", "imageId1", ProductStatus.APROVED), mockImage);

        // ASSERT
        assertNotNull(updatedProduct);
        assertEquals("Updated Shirt", updatedProduct.getName());
        verify(productRepository, times(1)).save(any(Product.class));
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

    @Test
    public void getAllProducts_returnsListOfProducts() throws IOException
    {
        // ARRANGE
        when(productRepository.findAll()).thenReturn(mockProductList);

        // ACT
        List<Product> allProducts = productService.getAllProducts();

        // ASSERT
        assertNotNull(allProducts);
        assertEquals(mockProductList.size(), allProducts.size());
    }

    @Test
    public void getProductById_validId_returnsProduct() throws IOException
    {
        // ARRANGE
        Product existingProduct = mockProductList.get(0);
        when(productRepository.findById("1")).thenReturn(Optional.of(existingProduct));

        // ACT
        Product product = productService.getProductById("1");

        // ASSERT
        assertNotNull(product);
        assertEquals(existingProduct.getId(), product.getId());
    }

    @Test
    public void getProductById_invalidId_throwsException()
    {
        // ARRANGE
        when(productRepository.findById("invalidId")).thenReturn(Optional.empty());

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> productService.getProductById("invalidId"));
        assertEquals("Product not found with id: invalidId", exception.getMessage());
    }

    @Test
    public void attemptToUpdateImageIds_updatesInvalidImageIds() throws IOException
    {
        // ARRANGE
        when(productRepository.findAll()).thenReturn(mockProductList);
        when(imageManagement.imageExists(anyString())).thenReturn(false);
        when(imageManagement.provideDefaultImageId()).thenReturn("defaultImageId");

        // ACT
        productService.attemptToUpdateImageIds();

        // ASSERT
        for (Product product : mockProductList)
        {
            assertEquals("defaultImageId", product.getImageId());
        }
        verify(productRepository, times(mockProductList.size())).save(any(Product.class));
    }

    @Test
    public void getProductsOwnedByUser_validUserId_returnsProductList()
    {
        // ARRANGE
        String userId = "1";
        when(mongoTemplate.find(any(Query.class), eq(Product.class))).thenReturn(mockProductList);

        // ACT
        List<Product> products = productService.getProductsOwnedByUser(userId);

        // ASSERT
        assertNotNull(products);
        assertEquals(mockProductList.size(), products.size());
    }
}
