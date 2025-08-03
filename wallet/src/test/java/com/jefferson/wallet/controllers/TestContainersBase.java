package com.jefferson.wallet.controllers;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.nio.file.Paths;

@Testcontainers
public abstract class TestContainersBase {

    @Container
    public static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:13")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeAll
    static void setUp() throws IOException, InterruptedException {

        postgres.start();

        runLiquibaseMigrations();
    }

    static void runLiquibaseMigrations() {
        String jdbcUrl = postgres.getJdbcUrl().replace("localhost", "host.docker.internal");

        String changelogPath = Paths.get("").toAbsolutePath()
                .getParent()
                .resolve("db-migrations/db")
                .normalize()
                .toString()
                .replace("\\", "/")
                .replaceFirst("^([A-Za-z]):", "/$1");

        System.out.println(changelogPath);

        ProcessBuilder builder = new ProcessBuilder(
                "docker", "run", "--rm",
                "-v", changelogPath + ":/liquibase/changelog",
                "--add-host=host.docker.internal:host-gateway",
                "liquibase/liquibase:4.24",
                "--url=" + jdbcUrl,
                "--changeLogFile=changelog-master.xml",
                "--username=" + postgres.getUsername(),
                "--password=" + postgres.getPassword(),
                "update"
        );

        Process process = null;
        try {
            process = builder.inheritIO().start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        int exitCode = 0;
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (exitCode != 0) {
            throw new RuntimeException("Liquibase migration failed");
        }
    }
}
