package com.example.NgoDangKhoa_2280601515.controller;

import com.example.NgoDangKhoa_2280601515.model.Order;
import com.example.NgoDangKhoa_2280601515.model.OrderStatus;
import com.example.NgoDangKhoa_2280601515.model.Product;
import com.example.NgoDangKhoa_2280601515.repository.OrderRepository;
import com.example.NgoDangKhoa_2280601515.repository.ProductRepository;
import java.util.List;
import com.mservice.config.Environment;
import com.mservice.enums.RequestType;
import com.mservice.models.PaymentResponse;
import com.mservice.processor.CreateOrderMoMo;
import com.mservice.shared.utils.LogUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/momo")
public class MomoController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/pay/{orderId}")
    public RedirectView payOrder(@PathVariable Long orderId) throws Exception {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid order Id:" + orderId));

        LogUtils.init();
        Environment environment = Environment.selectEnv("dev");
        String requestId = String.valueOf(System.currentTimeMillis());
        String momoOrderId = String.valueOf(System.currentTimeMillis());
        long amount = (long) order.getTotalAmount();
        String orderInfo = "Thanh toan don hang " + orderId;
        String returnURL = "http://localhost:8080/momo/return";
        String notifyURL = "http://localhost:8080/momo/notify";

        PaymentResponse captureWalletMoMoResponse = CreateOrderMoMo.process(
                environment, momoOrderId, requestId, Long.toString(amount),
                orderInfo, returnURL, notifyURL, "", RequestType.CAPTURE_WALLET, Boolean.TRUE);

        return new RedirectView(captureWalletMoMoResponse.getPayUrl());
    }

    @PostMapping("/create")
    public RedirectView createMomoPayment(@RequestParam("amount") long amount, 
                                          @RequestParam("orderInfo") String orderInfo,
                                          @RequestParam(value="customerName", defaultValue="Khách hàng") String customerName,
                                          @RequestParam(value="productIds", required=false) List<Long> productIds) throws Exception {
        Order order = new Order();
        order.setCustomerName(customerName);
        order.setTotalAmount(amount);
        order.setStatus(OrderStatus.PENDING);
        
        if (productIds != null && !productIds.isEmpty()) {
            List<Product> products = productRepository.findAllById(productIds);
            order.setProducts(products);
        }
        order = orderRepository.save(order);

        LogUtils.init();
        Environment environment = Environment.selectEnv("dev");
        String requestId = String.valueOf(System.currentTimeMillis());
        String momoOrderId = order.getId() + "_" + System.currentTimeMillis();
        String returnURL = "http://localhost:8080/momo/return";
        String notifyURL = "http://localhost:8080/momo/notify";

        PaymentResponse captureWalletMoMoResponse = CreateOrderMoMo.process(
                environment, momoOrderId, requestId, Long.toString(amount),
                orderInfo, returnURL, notifyURL, "", RequestType.CAPTURE_WALLET, Boolean.TRUE);

        return new RedirectView(captureWalletMoMoResponse.getPayUrl());
    }

    @GetMapping("/return")
    public String momoReturn(
            @RequestParam(value = "orderId", required = false) String momoOrderId,
            @RequestParam(value = "requestId", required = false) String requestId,
            @RequestParam(value = "amount", required = false) String amount,
            @RequestParam(value = "orderInfo", required = false) String orderInfo,
            @RequestParam(value = "resultCode", required = false) String resultCode,
            Model model) {
        
        model.addAttribute("orderId", momoOrderId);
        model.addAttribute("amount", amount);
        model.addAttribute("orderInfo", orderInfo);
        
        if (momoOrderId != null) {
            try {
                String[] parts = momoOrderId.split("_");
                Long realOrderId = Long.parseLong(parts[0]);
                Order order = orderRepository.findById(realOrderId).orElse(null);
                
                if (order != null) {
                    if ("0".equals(resultCode)) {
                        order.setStatus(OrderStatus.CONFIRMED);
                        orderRepository.save(order);
                        model.addAttribute("message", "Thanh toán thành công. Đơn hàng đã được lưu!");
                    } else {
                        order.setStatus(OrderStatus.CANCELLED);
                        orderRepository.save(order);
                        model.addAttribute("message", "Thanh toán thất bại hoặc đã bị hủy.");
                    }
                }
            } catch (Exception e) {}
        }
        
        if (!model.containsAttribute("message")) {
            model.addAttribute("message", "Đang xử lý kết quả giao dịch...");
        }
        
        return "momo/return";
    }
}
