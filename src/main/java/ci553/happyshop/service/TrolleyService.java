package ci553.happyshop.service;

import ci553.happyshop.catalogue.Product;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Service class responsible for managing trolley operations.
 * This class handles business logic related to trolley management,
 * including merging duplicate products and sorting.
 * 
 * Responsibilities:
 * - Merge products with the same ID by combining their quantities
 * - Sort products in the trolley by product ID
 * - Maintain trolley organization and consistency
 * 
 * This service follows the Single Responsibility Principle by
 * separating trolley management logic from the CustomerModel.
 */
public class TrolleyService {

    /**
     * Merges products with the same product ID by combining their quantities,
     * and sorts the resulting list by product ID in ascending order.
     * 
     * When multiple instances of the same product exist in the trolley,
     * they are combined into a single product entry with the total quantity.
     * 
     * @param trolley The list of products in the trolley (may contain duplicates)
     * @return A new ArrayList with merged and sorted products
     */
    public static ArrayList<Product> mergeAndSort(ArrayList<Product> trolley) {
        if (trolley == null || trolley.isEmpty()) {
            return new ArrayList<>();
        }

        // Step 1: Merge products by ID (combine quantities)
        Map<String, Product> mergedProducts = new HashMap<>();
        
        for (Product product : trolley) {
            String productId = product.getProductId();
            
            if (mergedProducts.containsKey(productId)) {
                // Product already exists - combine quantities
                Product existing = mergedProducts.get(productId);
                int newQuantity = existing.getOrderedQuantity() + product.getOrderedQuantity();
                existing.setOrderedQuantity(newQuantity);
            } else {
                // New product - create a copy to avoid modifying the original
                Product productCopy = new Product(
                    product.getProductId(),
                    product.getProductDescription(),
                    product.getProductImageName(),
                    product.getUnitPrice(),
                    product.getStockQuantity()
                );
                productCopy.setOrderedQuantity(product.getOrderedQuantity());
                mergedProducts.put(productId, productCopy);
            }
        }

        // Step 2: Convert map values to list and sort by product ID
        ArrayList<Product> mergedAndSorted = new ArrayList<>(mergedProducts.values());
        mergedAndSorted.sort(Comparator.comparing(Product::getProductId));

        return mergedAndSorted;
    }

    /**
     * Sorts products in the trolley by product ID without merging.
     * Useful when products are already merged but need to be sorted.
     * 
     * @param trolley The list of products to sort
     * @return A new sorted ArrayList (original list is not modified)
     */
    public static ArrayList<Product> sortByProductId(ArrayList<Product> trolley) {
        if (trolley == null || trolley.isEmpty()) {
            return new ArrayList<>();
        }

        ArrayList<Product> sorted = new ArrayList<>(trolley);
        sorted.sort(Comparator.comparing(Product::getProductId));
        return sorted;
    }
}



