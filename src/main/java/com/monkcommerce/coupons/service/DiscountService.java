package com.monkcommerce.coupons.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monkcommerce.coupons.dto.Cart;
import com.monkcommerce.coupons.dto.CartItem;
import com.monkcommerce.coupons.dto.UpdatedCart;
import com.monkcommerce.coupons.model.Coupon;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DiscountService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public double calculateDiscount(Coupon coupon, Cart cart) {
        try {
            Map<String, Object> details = objectMapper.readValue(coupon.getDetails(), Map.class);
            
            switch (coupon.getType()) {
                case CART_WISE:
                    return calculateCartWiseDiscount(details, cart);
                case PRODUCT_WISE:
                    return calculateProductWiseDiscount(details, cart);
                case BXGY:
                    return calculateBxGyDiscount(details, cart);
                default:
                    return 0;
            }
        } catch (Exception e) {
            return 0;
        }
    }

    public UpdatedCart applyDiscount(Coupon coupon, Cart cart) {
        try {
            Map<String, Object> details = objectMapper.readValue(coupon.getDetails(), Map.class);
            
            switch (coupon.getType()) {
                case CART_WISE:
                    return applyCartWiseDiscount(details, cart);
                case PRODUCT_WISE:
                    return applyProductWiseDiscount(details, cart);
                case BXGY:
                    return applyBxGyDiscount(details, cart);
                default:
                    return createUpdatedCart(cart, 0);
            }
        } catch (Exception e) {
            return createUpdatedCart(cart, 0);
        }
    }

    private double calculateCartWiseDiscount(Map<String, Object> details, Cart cart) {
        double threshold = ((Number) details.get("threshold")).doubleValue();
        double discountPercent = ((Number) details.get("discount")).doubleValue();
        
        double cartTotal = cart.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
        
        if (cartTotal > threshold) {
            return (cartTotal * discountPercent) / 100;
        }
        return 0;
    }

    private UpdatedCart applyCartWiseDiscount(Map<String, Object> details, Cart cart) {
        double discount = calculateCartWiseDiscount(details, cart);
        return createUpdatedCart(cart, discount);
    }

    private double calculateProductWiseDiscount(Map<String, Object> details, Cart cart) {
        long productId = ((Number) details.get("product_id")).longValue();
        double discountPercent = ((Number) details.get("discount")).doubleValue();
        
        return cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .mapToDouble(item -> (item.getPrice() * item.getQuantity() * discountPercent) / 100)
                .sum();
    }

    private UpdatedCart applyProductWiseDiscount(Map<String, Object> details, Cart cart) {
        long productId = ((Number) details.get("product_id")).longValue();
        double discountPercent = ((Number) details.get("discount")).doubleValue();
        
        List<CartItem> updatedItems = new ArrayList<>();
        double totalDiscount = 0;
        
        for (CartItem item : cart.getItems()) {
            CartItem newItem = CartItem.builder()
                    .productId(item.getProductId())
                    .quantity(item.getQuantity())
                    .price(item.getPrice())
                    .totalDiscount(0)
                    .build();
            
            if (item.getProductId().equals(productId)) {
                double itemDiscount = (item.getPrice() * item.getQuantity() * discountPercent) / 100;
                newItem.setTotalDiscount(itemDiscount);
                totalDiscount += itemDiscount;
            }
            
            updatedItems.add(newItem);
        }
        
        double totalPrice = cart.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
        
        return UpdatedCart.builder()
                .items(updatedItems)
                .totalPrice(totalPrice)
                .totalDiscount(totalDiscount)
                .finalPrice(totalPrice - totalDiscount)
                .build();
    }

    private double calculateBxGyDiscount(Map<String, Object> details, Cart cart) {
        List<Map<String, Object>> buyProducts = (List<Map<String, Object>>) details.get("buy_products");
        List<Map<String, Object>> getProducts = (List<Map<String, Object>>) details.get("get_products");
        int repetitionLimit = ((Number) details.get("repetition_limit")).intValue();
        
        int buyQuantityNeeded = buyProducts.stream()
                .mapToInt(p -> ((Number) p.get("quantity")).intValue())
                .sum();
        
        int buyItemsInCart = 0;
        for (Map<String, Object> buyProduct : buyProducts) {
            long productId = ((Number) buyProduct.get("product_id")).longValue();
            buyItemsInCart += cart.getItems().stream()
                    .filter(item -> item.getProductId().equals(productId))
                    .mapToInt(CartItem::getQuantity)
                    .sum();
        }
        
        int dealsApplicable = Math.min(buyItemsInCart / buyQuantityNeeded, repetitionLimit);
        
        if (dealsApplicable == 0) {
            return 0;
        }
        
        int getQuantityPerDeal = getProducts.stream()
                .mapToInt(p -> ((Number) p.get("quantity")).intValue())
                .sum();
        
        int totalFreeItems = dealsApplicable * getQuantityPerDeal;
        
        double discount = 0;
        int remainingFreeItems = totalFreeItems;
        
        for (Map<String, Object> getProduct : getProducts) {
            if (remainingFreeItems == 0) break;
            
            long productId = ((Number) getProduct.get("product_id")).longValue();
            
            for (CartItem item : cart.getItems()) {
                if (item.getProductId().equals(productId) && remainingFreeItems > 0) {
                    int freeQty = Math.min(item.getQuantity(), remainingFreeItems);
                    discount += freeQty * item.getPrice();
                    remainingFreeItems -= freeQty;
                }
            }
        }
        
        return discount;
    }

    private UpdatedCart applyBxGyDiscount(Map<String, Object> details, Cart cart) {
        double discount = calculateBxGyDiscount(details, cart);
        
        List<Map<String, Object>> getProducts = (List<Map<String, Object>>) details.get("get_products");
        List<Map<String, Object>> buyProducts = (List<Map<String, Object>>) details.get("buy_products");
        int repetitionLimit = ((Number) details.get("repetition_limit")).intValue();
        
        int buyQuantityNeeded = buyProducts.stream()
                .mapToInt(p -> ((Number) p.get("quantity")).intValue())
                .sum();
        
        int buyItemsInCart = 0;
        for (Map<String, Object> buyProduct : buyProducts) {
            long productId = ((Number) buyProduct.get("product_id")).longValue();
            buyItemsInCart += cart.getItems().stream()
                    .filter(item -> item.getProductId().equals(productId))
                    .mapToInt(CartItem::getQuantity)
                    .sum();
        }
        
        int dealsApplicable = Math.min(buyItemsInCart / buyQuantityNeeded, repetitionLimit);
        
        if (dealsApplicable == 0) {
            return createUpdatedCart(cart, 0);
        }
        
        int getQuantityPerDeal = getProducts.stream()
                .mapToInt(p -> ((Number) p.get("quantity")).intValue())
                .sum();
        
        int totalFreeItems = dealsApplicable * getQuantityPerDeal;
        
        List<CartItem> updatedItems = new ArrayList<>();
        int remainingFreeItems = totalFreeItems;
        
        for (CartItem item : cart.getItems()) {
            CartItem newItem = CartItem.builder()
                    .productId(item.getProductId())
                    .quantity(item.getQuantity())
                    .price(item.getPrice())
                    .totalDiscount(0)
                    .build();
            
            for (Map<String, Object> getProduct : getProducts) {
                long getProductId = ((Number) getProduct.get("product_id")).longValue();
                
                if (item.getProductId().equals(getProductId) && remainingFreeItems > 0) {
                    int freeQty = Math.min(item.getQuantity(), remainingFreeItems);
                    newItem.setTotalDiscount(freeQty * item.getPrice());
                    remainingFreeItems -= freeQty;
                }
            }
            
            updatedItems.add(newItem);
        }
        
        double totalPrice = cart.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
        
        return UpdatedCart.builder()
                .items(updatedItems)
                .totalPrice(totalPrice)
                .totalDiscount(discount)
                .finalPrice(totalPrice - discount)
                .build();
    }

    private UpdatedCart createUpdatedCart(Cart cart, double totalDiscount) {
        List<CartItem> items = cart.getItems().stream()
                .map(item -> CartItem.builder()
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .totalDiscount(0)
                        .build())
                .toList();
        
        double totalPrice = cart.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
        
        return UpdatedCart.builder()
                .items(items)
                .totalPrice(totalPrice)
                .totalDiscount(totalDiscount)
                .finalPrice(totalPrice - totalDiscount)
                .build();
    }
}
