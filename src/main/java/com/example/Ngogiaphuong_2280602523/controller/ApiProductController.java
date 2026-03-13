package com.example.Ngogiaphuong_2280602523.controller;

import com.example.Ngogiaphuong_2280602523.model.Category;
import com.example.Ngogiaphuong_2280602523.model.Product;
import com.example.Ngogiaphuong_2280602523.repository.CategoryRepository;
import com.example.Ngogiaphuong_2280602523.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ApiProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Value("${file.upload-dir:uploads/products}")
    private String uploadDir;

    @PostMapping("/upload-image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File rỗng"));
        }
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
            String fileName = UUID.randomUUID().toString() + (ext != null ? "." + ext : "");
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            String fileUrl = "/" + uploadDir.replace("\\", "/") + "/" + fileName;
            return ResponseEntity.ok(Map.of("url", fileUrl));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public List<Product> getAllProducts() {
        List<Product> products = productRepository.findAll();
        for (Product p : products) {
            if (p.getCategory() != null) {
                p.setCategoryName(p.getCategory().getName());
            }
        }
        return products;
    }

    @PostMapping("/sync")
    public ResponseEntity<?> syncProducts(@RequestBody List<ProductDTO> products) {
        try {
            productRepository.deleteAll();
        } catch (Exception e) {
            System.err.println("Could not delete all products, some might be in use.");
        }

        for (ProductDTO dto : products) {
            Product product = new Product();
            Map<String, String> validation = validateFlashSalePayload(dto);
            if (!validation.isEmpty()) {
                return ResponseEntity.badRequest().body(validation);
            }

            applyDtoToProduct(product, dto);
            productRepository.save(product);
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody ProductDTO dto) {
        Map<String, String> validation = validateFlashSalePayload(dto);
        if (!validation.isEmpty()) {
            return ResponseEntity.badRequest().body(validation);
        }

        Product product = new Product();
        applyDtoToProduct(product, dto);
        return ResponseEntity.ok(productRepository.save(product));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody ProductDTO dto) {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, String> validation = validateFlashSalePayload(dto);
        if (!validation.isEmpty()) {
            return ResponseEntity.badRequest().body(validation);
        }

        applyDtoToProduct(product, dto);

        return ResponseEntity.ok(productRepository.save(product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        productRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    private void applyDtoToProduct(Product product, ProductDTO dto) {
        product.setName(dto.getName());
        product.setPrice(dto.getPrice());
        product.setOldPrice(dto.getOldPrice());
        product.setDiscount(dto.getDiscount());
        if (dto.getImage() != null && !dto.getImage().isEmpty()) {
            product.setImage(dto.getImage());
        }
        product.setLink(dto.getLink());

        boolean isPromotion = dto.getPromotion() != null && dto.getPromotion();
        product.setPromotion(isPromotion);
        if (isPromotion) {
            product.setFlashSaleQuantity(dto.getFlashSaleQuantity());
            product.setFlashSaleSold(dto.getFlashSaleSold() != null ? dto.getFlashSaleSold() : 0);
        } else {
            product.setFlashSaleQuantity(0);
            product.setFlashSaleSold(0);
        }

        Category category = resolveCategory(dto.getCategory());
        product.setCategory(category);
        product.setCategoryName(dto.getCategory());
    }

    private Category resolveCategory(String categoryName) {
        if (categoryName == null || categoryName.isEmpty()) {
            return null;
        }

        return categoryRepository.findAll().stream()
                .filter(c -> c.getName() != null && c.getName().equalsIgnoreCase(categoryName))
                .findFirst()
                .orElse(null);
    }

    private Map<String, String> validateFlashSalePayload(ProductDTO dto) {
        Map<String, String> errors = new HashMap<>();
        boolean isPromotion = dto.getPromotion() != null && dto.getPromotion();
        if (isPromotion) {
            Integer quantity = dto.getFlashSaleQuantity();
            if (quantity == null || quantity <= 0) {
                errors.put("error", "Sản phẩm Flash Sale phải có số lượng lớn hơn 0.");
            }

            Integer sold = dto.getFlashSaleSold();
            if (sold != null && sold < 0) {
                errors.put("error", "Số lượng đã bán không hợp lệ.");
            }
        }
        return errors;
    }
}

class ProductDTO {
    private String name;
    private double price;
    private double oldPrice;
    private double discount;
    private String image;
    private Boolean promotion;
    private Integer flashSaleQuantity;
    private Integer flashSaleSold;
    private String category;
    private String link;

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getOldPrice() {
        return oldPrice;
    }

    public void setOldPrice(double oldPrice) {
        this.oldPrice = oldPrice;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Boolean getPromotion() {
        return promotion;
    }

    public void setPromotion(Boolean promotion) {
        this.promotion = promotion;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getFlashSaleQuantity() {
        return flashSaleQuantity;
    }

    public void setFlashSaleQuantity(Integer flashSaleQuantity) {
        this.flashSaleQuantity = flashSaleQuantity;
    }

    public Integer getFlashSaleSold() {
        return flashSaleSold;
    }

    public void setFlashSaleSold(Integer flashSaleSold) {
        this.flashSaleSold = flashSaleSold;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
