package com.monkcommerce.coupons.dto;

import com.monkcommerce.coupons.model.CouponType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CouponResponse {
    
    private Long id;
    private CouponType type;
    private Object details;
}
