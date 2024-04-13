package com.andyestrada.crochetcreations.services;

import com.andyestrada.crochetcreations.dto.request.UpdateStockDto;
import com.andyestrada.crochetcreations.entities.Product;
import com.andyestrada.crochetcreations.entities.Item;
import com.andyestrada.crochetcreations.repositories.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    @Autowired
    private final ProductService productService;

    @Autowired
    private final ItemRepository itemRepository;

    @Override
    public Optional<List<Item>> updateStock(UpdateStockDto updateStockDto) {
        Product product = productService.findById(updateStockDto.getProductId()).orElseThrow();
        List<Item> stock = new ArrayList<>();
        for (int i = 0; i < updateStockDto.getQuantity(); i++) {
            Item item = Item.builder().product(product).build();
            stock.add(item);
        }
        List<Item> savedItem = itemRepository.saveAll(stock);
        return Optional.of(savedItem);
    }

    @Override
    public Optional<List<Item>> getItems() {
        return Optional.of(itemRepository.findAll());
    }

    @Override
    public Optional<List<Item>> getItems(Long productId) {
        List<Item> itemList = itemRepository.findAll()
                .stream().filter(item -> item.getProduct().getId().equals(productId)).toList();
        return Optional.of(itemList);
    }

    @Override
    public Optional<List<Item>> getItems(Boolean sold) {
        List<Item> items = itemRepository.findAll().stream()
                .filter(item -> sold ? item.getOrder() != null : item.getOrder() == null).toList();
        return Optional.of(items);
    }

    @Override
    public Optional<List<Item>> getItems(Long productId, Boolean sold) {
        List<Item> items = itemRepository.findAll().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .filter(item -> sold ? item.getOrder() != null : item.getOrder() == null)
                .toList();
        return Optional.of(items);
    }

}
