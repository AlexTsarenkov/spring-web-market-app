package ru.yandex.praktikum.springwebmarketapp.configuration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.yandex.praktikum.springwebmarketapp.SpringWebMarketAppApplication;

@SpringBootTest(
        classes = SpringWebMarketAppApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Import(SecurityConfiguration.class)
class SecurityConfigurationTest {

    @Autowired
    private ApplicationContext applicationContext;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        this.webTestClient = WebTestClient.bindToApplicationContext(applicationContext)
                .apply(SecurityMockServerConfigurers.springSecurity())
                .build();
    }

    @Test
    void shouldRedirectUnauthenticatedUserToLogin() {
        webTestClient.get()
                .uri("/cart")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location", ".*/login");
    }

    @Test
    void shouldAllowAccessToLoginEndpoint() {
        webTestClient.get()
                .uri("/login")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void shouldAllowAuthenticatedUserToProtectedEndpoint() {
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockOAuth2Login()
                        .attributes(attrs -> attrs.put("sub", "user-1")))
                .get()
                .uri("/orders")
                .exchange()
                .expectStatus().isOk();
    }
}
