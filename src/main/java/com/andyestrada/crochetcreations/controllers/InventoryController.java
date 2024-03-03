package com.andyestrada.crochetcreations.controllers;

import com.andyestrada.crochetcreations.dto.request.UpdateStockDto;
import com.andyestrada.crochetcreations.entities.Item;
import com.andyestrada.crochetcreations.services.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.swing.text.html.Option;
import java.util.*;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    @Autowired
    private final InventoryService inventoryService;

    @PostMapping("/update-stock")
    public ResponseEntity<List<Item>> updateStock(@RequestBody UpdateStockDto updateStockDto) {
        try {
            Optional<List<Item>> optionalItemList = inventoryService.updateStock(updateStockDto);
            return optionalItemList.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.badRequest().build());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @GetMapping("/items")
    public ResponseEntity<List<Item>> getItems(@RequestParam(required = false) Map<String, String> queryParams) {
        Optional<Long> productId = Optional.empty();
        Optional<Boolean> sold = Optional.empty();
        if (queryParams.get("productId") != null) {
            productId = Optional.of(Long.valueOf(queryParams.get("productId")));
        }
        if (queryParams.get("sold") != null) {
            sold = Optional.of(Boolean.valueOf(queryParams.get("sold")));
        }
        try {
            Optional<List<Item>> optionalItemList = Optional.empty();
            if (productId.isEmpty() && sold.isEmpty()) { // get all Items
                optionalItemList = inventoryService.getItems();
            } else if (productId.isPresent() && sold.isEmpty()) { // get Items by Product
                optionalItemList = inventoryService.getItems(productId.get());
            } else if (productId.isEmpty() && sold.isPresent()) { // get Items by sold? status
                optionalItemList = inventoryService.getItems(sold.get());
            } else if (productId.isPresent() && sold.isPresent()) { // get Items by Product and by sold? status
                optionalItemList = inventoryService.getItems(productId.get(), sold.get());
            }
            return optionalItemList.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.badRequest().build());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

}
