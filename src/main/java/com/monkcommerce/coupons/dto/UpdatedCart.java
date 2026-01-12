package com.monkcommerce.coupons.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatedCart {
    private List<CartItem> items;
    private double totalPrice;
    private double totalDiscount;
    private double finalPrice;
}
