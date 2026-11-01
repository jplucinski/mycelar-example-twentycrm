package io.mycelar.example.twenty.support;

import java.time.Duration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

public final class TwentyDevContainer {

    /** Seeded admin access token from {@code twentycrm/twenty-app-dev} (Twenty CI spawn action). */
    public static final String SEEDED_ACCESS_TOKEN =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIyMDIwMjAyMC05ZTNiLTQ2ZDQtYTU1Ni04OGI5ZGRjMmIwMzQiLCJ1c2VySWQiOiIyMDIwMjAyMC05ZTNiLTQ2ZDQtYTU1Ni04OGI5ZGRjMmIwMzQiLCJ3b3Jrc3BhY2VJZCI6IjIwMjAyMDIwLTFjMjUtNGQwMi1iZjI1LTZhZWNjZjdlYTQxOSIsIndvcmtzcGFjZU1lbWJlcklkIjoiMjAyMDIwMjAtMDY4Ny00YzQxLWI3MDctZWQxYmZjYTk3MmE3IiwidXNlcldvcmtzcGFjZUlkIjoiMjAyMDIwMjAtOWUzYi00NmQ0LWE1NTYtODhiOWRkYzJiMDM1IiwidHlwZSI6IkFDQ0VTUyIsImF1dGhQcm92aWRlciI6InBhc3N3b3JkIiwiaWF0IjoxNzM1Njg5NjAwLCJleHAiOjQ4OTE0NDk2MDB9.Ow8z-Dptg4LDiO2nApu5UQcX2a-gXdu_McalWs7Pgt8";

    public static final int PORT = 2021;

    private TwentyDevContainer() {}

    public static GenericContainer<?> create() {
        return new GenericContainer<>(DockerImageName.parse("twentycrm/twenty-app-dev:latest"))
                .withExposedPorts(PORT)
                .withEnv("NODE_PORT", "2021")
                .withEnv("SERVER_URL", "http://localhost:2021")
                .withEnv("MARKETPLACE_CATALOG_SYNC_CRON_ENABLED", "false")
                .withEnv("API_RATE_LIMITING_SHORT_LIMIT", "100000")
                .withEnv("API_RATE_LIMITING_LONG_LIMIT", "100000")
                .waitingFor(
                        Wait.forLogMessage(".*Nest application successfully started.*", 1)
                                .withStartupTimeout(Duration.ofMinutes(25)));
    }

    public static String restBaseUrl(GenericContainer<?> container) {
        return "http://" + container.getHost() + ":" + container.getMappedPort(PORT) + "/rest";
    }
}
