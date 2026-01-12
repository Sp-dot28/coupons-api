package com.monkcommerce.coupons.service;

import com.monkcommerce.coupons.dto.ApplicableCoupon;
import com.monkcommerce.coupons.dto.ApplicableCouponsResponse;
import com.monkcommerce.coupons.dto.ApplyCouponResponse;
import com.monkcommerce.coupons.dto.Cart;
import com.monkcommerce.coupons.dto.CouponResponse;
import com.monkcommerce.coupons.dto.CreateCouponRequest;
import com.monkcommerce.coupons.dto.UpdatedCart;
import com.monkcommerce.coupons.model.Coupon;
import com.monkcommerce.coupons.repository.CouponRepository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final DiscountService discountService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CouponResponse createCoupon(CreateCouponRequest request) {
        String detailsJson = convertDetailsToJson(request.getDetails());

        Coupon coupon = Coupon.builder()
                .type(request.getType())
                .details(detailsJson)
                .build();

        Coupon saved = couponRepository.save(coupon);

        return CouponResponse.builder()
                .id(saved.getId())
                .type(saved.getType())
                .details(convertJsonToObject(saved.getDetails()))
                .build();
    }

    public List<CouponResponse> getAllCoupons() {
        return couponRepository.findAll().stream()
                .map(c -> CouponResponse.builder()
                        .id(c.getId())
                        .type(c.getType())
                        .details(convertJsonToObject(c.getDetails()))
                        .build())
                .collect(Collectors.toList());
    }

    private String convertDetailsToJson(Object details) {
        try {
            return objectMapper.writeValueAsString(details);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Invalid details payload", e);
        }
    }

    private Object convertJsonToObject(String json) {
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (JsonProcessingException e) {
            return json;
        }
    }

    public CouponResponse getCouponById(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found with id: " + id));

        return CouponResponse.builder()
                .id(coupon.getId())
                .type(coupon.getType())
                .details(convertJsonToObject(coupon.getDetails()))
                .build();
    }

    public CouponResponse updateCoupon(Long id, CreateCouponRequest request) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found with id: " + id));

        String detailsJson = convertDetailsToJson(request.getDetails());

        coupon.setType(request.getType());
        coupon.setDetails(detailsJson);

        Coupon updated = couponRepository.save(coupon);

        return CouponResponse.builder()
                .id(updated.getId())
                .type(updated.getType())
                .details(convertJsonToObject(updated.getDetails()))
                .build();
    }

    public void deleteCoupon(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found with id: " + id));

        couponRepository.delete(coupon);
    }


    public ApplicableCouponsResponse getApplicableCoupons(Cart cart) {
        List<Coupon> allCoupons = couponRepository.findAll();
        List<ApplicableCoupon> applicableCoupons = new ArrayList<>();

        for (Coupon coupon : allCoupons) {
            double discount = discountService.calculateDiscount(coupon, cart);
            if (discount > 0) {
                applicableCoupons.add(ApplicableCoupon.builder()
                        .couponId(coupon.getId())
                        .type(coupon.getType().toString())
                        .discount(discount)
                        .build());
            }
        }

        return ApplicableCouponsResponse.builder()
                .applicableCoupons(applicableCoupons)
                .build();
    }

    public ApplyCouponResponse applyCoupon(Long id, Cart cart) {
        if (cart == null || cart.getItems() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart or cart.items is missing");
        }

        Coupon coupon = couponRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coupon not found with id: " + id));

        UpdatedCart updatedCart = discountService.applyDiscount(coupon, cart);

        return ApplyCouponResponse.builder()
            .updatedCart(updatedCart)
            .build();
    }

    
}
