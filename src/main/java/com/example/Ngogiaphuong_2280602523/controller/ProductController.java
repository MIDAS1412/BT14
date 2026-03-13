package com.example.Ngogiaphuong_2280602523.controller;

import com.example.Ngogiaphuong_2280602523.model.Product;
import com.example.Ngogiaphuong_2280602523.repository.ProductRepository;
import com.example.Ngogiaphuong_2280602523.repository.CategoryRepository;
import com.example.Ngogiaphuong_2280602523.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private FileStorageService fileStorageService;

    // Hiển thị danh sách sản phẩm
    @GetMapping
    public String listProducts(Model model) {
        model.addAttribute("promotionProducts", productRepository.findByPromotionTrue());
        model.addAttribute("regularProducts", productRepository.findByPromotionFalse());
        model.addAttribute("products", productRepository.findAll()); // Dành cho các view cũ dùng chung "products"
        return "products/ProductsList";
    }

    // Hiển thị form thêm sản phẩm mới
    @GetMapping("/new")
    public String showProductForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryRepository.findAll());
        return "products/form";
    }

    // Lưu sản phẩm (thêm mới hoặc cập nhật)
    @PostMapping("/save")
    public String saveProduct(@ModelAttribute("product") Product product,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {
        try {
            // Xử lý upload file ảnh
            if (imageFile != null && !imageFile.isEmpty()) {
                // Kiểm tra file có phải là ảnh không
                if (fileStorageService.isImageFile(imageFile)) {
                    // Xóa ảnh cũ nếu đang cập nhật sản phẩm
                    if (product.getId() != null) {
                        Product existingProduct = productRepository.findById(product.getId()).orElse(null);
                        if (existingProduct != null && existingProduct.getImage() != null) {
                            fileStorageService.deleteFile(existingProduct.getImage());
                        }
                    }
                    // Lưu file mới và cập nhật đường dẫn
                    String filePath = fileStorageService.storeFile(imageFile);
                    product.setImage(filePath);
                }
            }

            // Tìm Category từ database và gán vào Product
            if (categoryId != null) {
                categoryRepository.findById(categoryId).ifPresent(product::setCategory);
            } else {
                product.setCategory(null);
            }

            productRepository.save(product);
        } catch (Exception e) {
            e.printStackTrace();
            // Có thể thêm thông báo lỗi cho user
        }
        return "redirect:/products";
    }

    // Hiển thị form chỉnh sửa sản phẩm
    @GetMapping("/edit/{id}")
    public String showUpdateForm(@PathVariable("id") Long id, Model model) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryRepository.findAll());
        return "products/form";
    }

    // Xóa sản phẩm
    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable("id") Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));

        // Xóa file ảnh trước khi xóa sản phẩm
        if (product.getImage() != null) {
            fileStorageService.deleteFile(product.getImage());
        }

        productRepository.delete(product);
        return "redirect:/products";
    }
}
