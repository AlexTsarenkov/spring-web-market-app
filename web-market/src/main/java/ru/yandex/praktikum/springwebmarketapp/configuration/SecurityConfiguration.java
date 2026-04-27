package ru.yandex.praktikum.springwebmarketapp.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.security.web.server.savedrequest.NoOpServerRequestCache;
import ru.yandex.praktikum.springwebmarketapp.eventhandler.LogoutEventHandler;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http,
                                                            ServerLogoutSuccessHandler logoutSuccessHandler,
                                                            LogoutEventHandler logoutEventHandler) {
        RedirectServerAuthenticationSuccessHandler oauth2LoginSuccessHandler =
                new RedirectServerAuthenticationSuccessHandler("/items");
        // иначе берётся URL из WebSessionServerRequestCache (часто «/») и перекрывает /items
        oauth2LoginSuccessHandler.setRequestCache(NoOpServerRequestCache.getInstance());

        return http.authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.GET, "/items").permitAll()
                        .pathMatchers(HttpMethod.GET, "/items/{id}").permitAll()
                        .pathMatchers(HttpMethod.GET, "/login").permitAll()
                        .pathMatchers("/oauth2/**", "/login/**").permitAll()
                        .pathMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .authenticationSuccessHandler(oauth2LoginSuccessHandler)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutHandler(handlers -> handlers.add(0, logoutEventHandler))
                        .logoutSuccessHandler(logoutSuccessHandler)
                )
                .csrf(csrf -> csrf.disable())
                .build();
    }

    @Bean
    public ServerLogoutSuccessHandler oidcLogoutSuccessHandler(
            ReactiveClientRegistrationRepository clientRegistrationRepository) {
        OidcClientInitiatedServerLogoutSuccessHandler oidcLogoutSuccessHandler =
                new OidcClientInitiatedServerLogoutSuccessHandler(clientRegistrationRepository);
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}");

        return (webFilterExchange, authentication) ->
                oidcLogoutSuccessHandler.onLogoutSuccess(webFilterExchange, authentication);
    }
}
