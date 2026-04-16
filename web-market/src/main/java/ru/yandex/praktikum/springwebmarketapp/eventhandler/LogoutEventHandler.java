package ru.yandex.praktikum.springwebmarketapp.eventhandler;

import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.logout.ServerLogoutHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.yandex.praktikum.springwebmarketapp.service.CartService;
import ru.yandex.praktikum.springwebmarketapp.service.SecurityService;

@Component
@AllArgsConstructor
public class LogoutEventHandler implements ServerLogoutHandler {
    private final CartService cartService;
    private final SecurityService securityService;

    @Override
    public Mono<Void> logout(WebFilterExchange exchange, Authentication authentication) {
        return securityService.getIdFromAuthentication(authentication)
                .flatMap(userId -> cartService.deleteCartFromRedis(userId));
    }
}
