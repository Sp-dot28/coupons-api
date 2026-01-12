package com.monkcommerce.coupons.controller;

import com.monkcommerce.coupons.dto.*;
import com.monkcommerce.coupons.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/apply-coupon")
public class ApplyCouponController {

    private final CouponService couponService;

    @PostMapping("/{id}")
    public ResponseEntity<ApplyCouponResponse> applyCoupon(
            @PathVariable Long id,
            @RequestBody(required = false) Cart cart) {
        if (cart == null || cart.getItems() == null) {
            return ResponseEntity.<ApplyCouponResponse>badRequest().build();
        }

        ApplyCouponResponse response = couponService.applyCoupon(id, cart);
        return ResponseEntity.ok(response);
    }
}
