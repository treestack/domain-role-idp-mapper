package de.treestack.auth;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers(disabledWithoutDocker = true)
class DomainRoleIdpMapperIT {

    private final KeycloakContainer keycloak;

    DomainRoleIdpMapperIT() throws IOException {
        Path jar;
        try (var paths = Files.list(Path.of("target"))) {
            jar = paths
                    .filter(p -> p.getFileName().toString().startsWith("domain-role-idp-mapper-"))
                    .filter(p -> p.toString().endsWith(".jar"))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Provider JAR not found"));
        }

        keycloak = new KeycloakContainer("quay.io/keycloak/keycloak:22.0.5")
                        .withProviderLibsFrom(List.of(jar.toFile()));

        keycloak.start();
    }

    @Test
    void smokeTest() {
        assertTrue(keycloak.isRunning());
    }
}