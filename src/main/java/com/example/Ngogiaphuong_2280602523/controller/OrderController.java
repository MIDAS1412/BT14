package com.example.Ngogiaphuong_2280602523.controller;

import com.example.Ngogiaphuong_2280602523.model.Order;
import com.example.Ngogiaphuong_2280602523.model.OrderStatus;
import com.example.Ngogiaphuong_2280602523.repository.OrderRepository;
import com.example.Ngogiaphuong_2280602523.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    // Hiển thị danh sách đơn hàng
    @GetMapping
    public String listOrders(Model model) {
        model.addAttribute("orders", orderRepository.findAll());
        return "orders/list";
    }

    // Hiển thị form tạo đơn hàng mới
    @GetMapping("/new")
    public String showOrderForm(Model model) {
        model.addAttribute("order", new Order());
        model.addAttribute("products", productRepository.findAll());
        model.addAttribute("statuses", OrderStatus.values());
        return "orders/form";
    }

    // Lưu đơn hàng (thêm mới hoặc cập nhật)
    @PostMapping("/save")
    public String saveOrder(@ModelAttribute("order") Order order) {
        // Tính tổng tiền
        if (order.getProducts() != null) {
            double total = order.getProducts().stream()
                    .mapToDouble(p -> p.getPrice())
                    .sum();
            order.setTotalAmount(total);
        }
        orderRepository.save(order);
        return "redirect:/orders";
    }

    // Hiển thị chi tiết đơn hàng
    @GetMapping("/view/{id}")
    public String viewOrder(@PathVariable("id") Long id, Model model) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid order Id:" + id));
        model.addAttribute("order", order);
        return "orders/view";
    }

    // Hiển thị form chỉnh sửa đơn hàng
    @GetMapping("/edit/{id}")
    public String showUpdateForm(@PathVariable("id") Long id, Model model) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid order Id:" + id));
        model.addAttribute("order", order);
        model.addAttribute("products", productRepository.findAll());
        model.addAttribute("statuses", OrderStatus.values());
        return "orders/form";
    }

    // Cập nhật trạng thái đơn hàng
    @PostMapping("/update-status/{id}")
    public String updateOrderStatus(@PathVariable("id") Long id, 
                                    @RequestParam("status") OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid order Id:" + id));
        order.setStatus(status);
        orderRepository.save(order);
        return "redirect:/orders";
    }

    // Xóa đơn hàng
    @GetMapping("/delete/{id}")
    public String deleteOrder(@PathVariable("id") Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid order Id:" + id));
        orderRepository.delete(order);
        return "redirect:/orders";
    }
}
