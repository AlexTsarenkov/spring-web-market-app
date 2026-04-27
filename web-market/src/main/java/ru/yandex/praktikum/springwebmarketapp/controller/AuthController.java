package ru.yandex.praktikum.springwebmarketapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

@Controller
public class AuthController {

    @GetMapping("/login")
    public Mono<Rendering> loginPage() {
        return Mono.just(Rendering.view("/login")
                .modelAttribute("isLogout", false)
                .build());
    }

}
