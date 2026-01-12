package com.monkcommerce.coupons.controller;

import com.monkcommerce.coupons.dto.*;
import com.monkcommerce.coupons.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/applicable-coupons")
public class CartController {

    private final CouponService couponService;

    @PostMapping
    public ResponseEntity<ApplicableCouponsResponse> getApplicableCoupons(
            @RequestBody Cart cart) {
        ApplicableCouponsResponse response = couponService.getApplicableCoupons(cart);
        return ResponseEntity.ok(response);
    }
}
