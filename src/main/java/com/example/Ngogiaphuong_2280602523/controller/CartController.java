package com.example.Ngogiaphuong_2280602523.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Controller
public class CartController {

    @GetMapping(value = "/cart", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public String viewCart() throws IOException {
        // Serve file cart.html như raw HTML với UTF-8 encoding
        ClassPathResource resource = new ClassPathResource("templates/cart.html");
        try (InputStream inputStream = resource.getInputStream()) {
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        }
    }
}
