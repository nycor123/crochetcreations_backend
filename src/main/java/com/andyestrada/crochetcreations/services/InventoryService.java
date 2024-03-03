package com.andyestrada.crochetcreations.services;

import com.andyestrada.crochetcreations.dto.request.UpdateStockDto;
import com.andyestrada.crochetcreations.entities.Item;

import java.util.List;
import java.util.Optional;

public interface InventoryService {
    Optional<List<Item>> updateStock(UpdateStockDto updateStockDto);
    Optional<List<Item>> getItems();
    Optional<List<Item>> getItems(Long productId);
    Optional<List<Item>> getItems(Boolean sold);
    Optional<List<Item>> getItems(Long productId, Boolean sold);
}
