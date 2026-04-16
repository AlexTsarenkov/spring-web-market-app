package ru.yandex.praktikum.springwebmarketapp.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class SecurityService {
    public static final String ANONYMOUS_USER_KEY = "anonymous";

    public Mono<String> getIdFromAuthentication(Authentication authentication) {
        return Mono.fromSupplier(() -> {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            return oAuth2User.getAttributes().get("sub").toString();
        });
    }

    public Mono<String> getCurrentUserKey() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getPrincipal)
                .flatMap(principal -> {
                    return switch (principal) {
                        case OAuth2User oAuth2User -> Mono.just(oAuth2User.getAttributes().get("sub").toString());
                        case String principalName -> !principalName.equalsIgnoreCase("anonymousUser")
                                                     && !principalName.isBlank() ?
                                Mono.just(principalName) : Mono.just(ANONYMOUS_USER_KEY);
                        default -> Mono.just(ANONYMOUS_USER_KEY);
                    };
                })
                .switchIfEmpty(Mono.just(ANONYMOUS_USER_KEY));
    }
}
