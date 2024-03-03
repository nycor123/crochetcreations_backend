package com.andyestrada.crochetcreations.controllers;

import com.andyestrada.crochetcreations.CrochetCreationsApplication;
import com.andyestrada.crochetcreations.dto.request.ProductDto;
import com.andyestrada.crochetcreations.dto.request.UpdateStockDto;
import com.andyestrada.crochetcreations.entities.Item;
import com.andyestrada.crochetcreations.entities.Order;
import com.andyestrada.crochetcreations.entities.Product;
import com.andyestrada.crochetcreations.services.InventoryService;
import com.andyestrada.crochetcreations.services.OrderService;
import com.andyestrada.crochetcreations.services.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = CrochetCreationsApplication.class)
@AutoConfigureMockMvc(addFilters = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductService productService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ObjectMapper mapper;

    private List<Product> products;

    @BeforeAll
    public void createProducts() {
        List<ProductDto> productDtoList = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            ProductDto productDto = ProductDto.builder()
                    .name("Product_" + i)
                    .description("test description")
                    .build();
            productDtoList.add(productDto);
        }
        products = productService.saveAll(productDtoList).orElseThrow();
    }

    @Test
    public void canIncreaseProductStock() throws Exception {
        //given
        Product product = this.products.get(0);
        int itemCountBefore = inventoryService.getItems(product.getId()).orElse(new ArrayList<>()).size();
        int createItemCount = 3;
        int expectedItemCount = itemCountBefore + createItemCount;
        //when
        UpdateStockDto updateStockDto = UpdateStockDto.builder()
                .productId(product.getId())
                .quantity(createItemCount)
                .build();
        ResultActions result = mockMvc.perform(post("/api/v1/inventory/update-stock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(updateStockDto))
                .characterEncoding("utf-8"));
        //then
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(createItemCount)));
        List<Item> itemList = inventoryService.getItems(product.getId()).orElseThrow();
        assertEquals(itemList.size(), expectedItemCount);
    }

    @Test
    public void canGetAllItems() throws Exception {
        //given
        int itemCountBefore = inventoryService.getItems().orElse(new ArrayList<>()).size();
        int createdItemCount = 0;
        for (Product product : this.products) {
            UpdateStockDto updateStockDto = UpdateStockDto.builder()
                    .productId(product.getId())
                    .quantity(1)
                    .build();
            inventoryService.updateStock(updateStockDto);
            createdItemCount++;
        }
        int expectedItemCount = createdItemCount + itemCountBefore;
        //when
        ResultActions result = mockMvc.perform(get("/api/v1/inventory/items"));
        //then
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(expectedItemCount)));
    }

    @Test
    public void canGetItemsByProductId() throws Exception {
        //given
        Product product = products.get(0);
        int itemCountBefore = inventoryService.getItems(product.getId()).orElse(new ArrayList<>()).size();
        int createItemCount = 1;
        UpdateStockDto updateStockDto = UpdateStockDto.builder()
                .productId(product.getId())
                .quantity(createItemCount)
                .build();
        inventoryService.updateStock(updateStockDto);
        int expectedItemCount = createItemCount + itemCountBefore;
        //when
        String url = String.format("/api/v1/inventory/items?productId=%s", product.getId());
        ResultActions result = mockMvc.perform(get(url));
        //then
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(expectedItemCount)));
    }

    @Test
    public void canGetItemsBySoldStatus() throws Exception {
        //given
        Product product = products.get(0);
        int soldCountBefore = inventoryService.getItems(true).orElse(new ArrayList<>()).size();
        int createItemCount = 1;
        int expectedSoldCount = soldCountBefore + createItemCount;
        UpdateStockDto updateStockDto = UpdateStockDto.builder()
                .productId(product.getId())
                .quantity(createItemCount)
                .build();
        Item item = inventoryService.updateStock(updateStockDto).orElseThrow().get(0);
        List<Item> orderItems = new ArrayList<>();
        orderItems.add(item);
        Order order = Order.builder().items(orderItems).build();
        orderService.save(order);
        //when
        ResultActions result = mockMvc.perform(get("/api/v1/inventory/items?sold=true"));
        //then
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(expectedSoldCount)));
    }

    @Test
    public void canGetItemsByProductIdAndSoldStatus() throws Exception {
        //given
        Product product = products.get(0);
        int unsoldCountBefore = inventoryService.getItems(false).orElse(new ArrayList<>()).size();
        int createItemCount = 2;
        int expectedUnsoldCount = unsoldCountBefore + 1;
        UpdateStockDto updateStockDto = UpdateStockDto.builder()
                .productId(product.getId())
                .quantity(createItemCount)
                .build();
        List<Item> items = inventoryService.updateStock(updateStockDto).orElseThrow();
        List<Item> orderItems = new ArrayList<>();
        orderItems.add(items.get(0));
        Order order = Order.builder().items(orderItems).build();
        orderService.save(order);
        //when
        String url = String.format("/api/v1/inventory/items?productId=%s&sold=%s", product.getId(), false);
        ResultActions result = mockMvc.perform(get(url));
        //then
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(expectedUnsoldCount)));
    }

}
