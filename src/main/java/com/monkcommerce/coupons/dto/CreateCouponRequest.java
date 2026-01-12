package com.monkcommerce.coupons.dto;

import com.monkcommerce.coupons.model.CouponType;
import lombok.Data;

@Data
public class CreateCouponRequest {
    
    private CouponType type;
    private Object details;
}
