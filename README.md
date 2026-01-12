# Monk Commerce Coupon Management System
A RESTful API project to manage and apply different types of discount coupons (cart-wise, product-wise, and BxGy) for an e-commerce platform

## 🚀 **Quick Start**

```bash
git clone <your-repo>
cd coupons
./mvnw spring-boot:run
```

**Base URL:** `http://localhost:8080`

## 📋 **API Endpoints**

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/coupons` | Create new coupon |
| `GET` | `/coupons` | Get all coupons |
| `GET` | `/coupons/{id}` | Get coupon by ID |
| `PUT` | `/coupons/{id}` | Update coupon |
| `DELETE` | `/coupons/{id}` | Delete coupon |
| **`POST`** | **`/applicable-coupons`** | **Get applicable coupons for cart** |
| **`POST`** | **`/apply-coupon/{id}`** | **Apply specific coupon to cart** |

## 🧪 **Complete Test Flow**

### **1. Create Test Coupons**
```bash
# Cart-wise (10% off on cart > ₹100)
POST /coupons
{
  "type": "CART_WISE",
  "details": { 
    "threshold": 100, 
    "discount": 10 
  }
}

# Product-wise (20% off product ID 1)
POST /coupons
{
  "type": "PRODUCT_WISE", 
  "details": { 
    "product_id": 1,
    "discount": 20 
  }
}
```

### **2. Find Applicable Coupons**
```bash
POST /applicable-coupons
{ "items": [
  { "productId": 1, "quantity": 3, "price": 50.0 },
  { "productId": 2, "quantity": 2, "price": 75.0 }
] }
```
**Headers:** `Content-Type: application/json`
**Response:**
```json
{
  "applicableCoupons": [
    {
      "couponId": 1,
      "type": "CART_WISE",
      "discount": 37.5
    },
    {
      "couponId": 2, 
      "type": "PRODUCT_WISE",
      "discount": 30.0
    }
  ]
}
```

### **3. Apply Specific Coupon**
```bash
POST /apply-coupon/1
{ "items": [ { "productId": 1, "quantity": 3, "price": 50.0 } ] }
```
**Response:**
```json
{
  "updatedCart": {
    "items": [...],
    "totalPrice": 150.0,
    "totalDiscount": 15.0, 
    "finalPrice": 135.0
  }
}
```

## 💰 **Discount Logic Implementation**

### **1. CART_WISE**
```
If (cartTotal > threshold) 
  discount = (cartTotal * discount%) / 100
```
**Example:** Cart ₹150, threshold ₹100, 10% → **₹15 discount**

### **2. PRODUCT_WISE** 
```
discount = (productQty * productPrice * discount%) / 100
```
**Example:** Product#1 (3 qty × ₹50 × 20%) → **₹30 discount**

### **3. BXGY (Buy X Get Y)**
```
dealsApplicable = min(buyItemsInCart/buyQuantityNeeded, repetitionLimit)
totalFreeItems = dealsApplicable × getQuantityPerDeal
discount = freeItems × getProductPrice
```

## 🛠 **Tech Stack**
- **Spring Boot 3.x**
- **Spring Data JPA** + **H2 Database** (in-memory)
- **Lombok**
- **Jackson** (JSON processing)

## 📁 **Project Structure**
```
src/main/java/com/monkcommerce/coupons/
├── controller/
│   ├── CouponController.java      # CRUD
│   ├── CartController.java        # /applicable-coupons
│   └── ApplyCouponController.java # /apply-coupon/{id}
├── dto/                          # 8+ DTOs
├── service/
│   ├── CouponService.java
│   └── DiscountService.java       # Core discount logic
├── model/
│   └── Coupon.java
└── repository/
    └── CouponRepository.java
```

## 🔍 **Key Features Implemented**

### **✅ Coupon CRUD Operations**
- Create, Read, Update, Delete coupons
- JSON details parsing for different coupon types

### **✅ Applicable Coupons Detection**
- Scans ALL coupons against given cart
- Returns only coupons with `discount > 0`
- Returns discount amount for each applicable coupon

### **✅ Coupon Application**
- Applies specific coupon to cart items
- Returns updated cart with:
  - Individual item discounts
  - Total price, total discount, final price

## 🧪 **Edge Cases Handled**

| Scenario | Behavior |
|----------|----------|
| Empty cart | Returns `[]` applicable coupons |
| Cart below threshold | `CART_WISE` discount = `0` |
| Product not in cart | `PRODUCT_WISE` discount = `0` |
| Invalid coupon ID | `404 Not Found` |
| No coupons in DB | Returns `[]` applicable coupons |
| Zero quantity items | Treated as zero (no impact on totals or discounts) |

## 📈 **Sample Responses**

### **Applicable Coupons (Cart ₹225):**
```json
{
  "applicableCoupons": [
    {
      "couponId": 1,
      "type": "CART_WISE", 
      "discount": 22.5
    }
  ]
}
```

### **Applied Coupon Result:**
```json
{
  "updatedCart": {
    "items": [
      {
        "productId": 1,
        "quantity": 3,
        "price": 50.0,
        "totalDiscount": 15.0
      }
    ],
    "totalPrice": 150.0,
    "totalDiscount": 15.0,
    "finalPrice": 135.0
  }
}
```

## ⚠️ **Error Handling**
- **400 Bad Request**: Invalid JSON/missing fields
- **404 Not Found**: Coupon ID doesn't exist
- **500 Internal Server**: Database/parsing errors

## 🏗 **Database Schema**
```
Coupon Table:
- id (PK, Long)
- type (CART_WISE|PRODUCT_WISE|BXGY)
- details (JSON: {"threshold":100,"discount":10})
```

## 🔗 **Dependencies**
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>
</dependencies>
```

## 🎯 **Assignment Requirements Checklist**

- [x] **POST /applicable-coupons**: Returns applicable coupons + discount amounts
- [x] **POST /apply-coupon/{id}**: Returns updated cart with discounted prices
- [x] **All 3 coupon types**: CART_WISE, PRODUCT_WISE, BXGY ✅
- [x] **JSON details parsing** for each coupon type ✅
- [x] **Edge cases documented** in README ✅
- [x] **Complete CRUD operations** ✅
- [x] **Clean, production-ready code** ✅

## 📝 **Author**
**Shreya Pant** - Back-end Developer  
**Location:** Noida, Uttar Pradesh
