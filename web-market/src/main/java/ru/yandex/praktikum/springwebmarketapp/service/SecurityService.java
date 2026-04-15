package ru.yandex.praktikum.springwebmarketapp.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class SecurityService {
    public static final String ANONYMOUS_USER_KEY = "anonymous";

    public Mono<String> getCurrentUserKey() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .map(Authentication::getPrincipal)
                .flatMap(principal -> {
                    if (principal instanceof OAuth2User oauth2User) {
                        Object sub = oauth2User.getAttributes().get("sub");
                        if (sub != null) return Mono.just(sub.toString());
                    }
                    if (principal instanceof String principalName && !principalName.isBlank()
                        && !"anonymousUser".equalsIgnoreCase(principalName)) {
                        return Mono.just(principalName);
                    }
                    return Mono.empty();
                })
                .switchIfEmpty(Mono.just(ANONYMOUS_USER_KEY));
    }
}
