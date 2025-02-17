package com.example.banking.util;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

public class TestContainerConfig {

    public static MySQLContainer<?> mySqlContainer = new MySQLContainer<>(DockerImageName.parse("mysql:8.0.28")
                                                                                         .asCompatibleSubstituteFor(
                                                                                                 MySQLContainer.NAME))
            .withUsername("db_user")
            .withPassword("db_pass")
            .withDatabaseName("bank")
            .withCopyToContainer(MountableFile.forClasspathResource("init-db.sql"),
                                 "/docker-entrypoint-initdb.d/init-db.sql");

    static {
        Startables.deepStart(mySqlContainer).join();
    }

    @DynamicPropertySource
    static void overridePropertiesByContainers(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mySqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mySqlContainer::getUsername);
        registry.add("spring.datasource.password", mySqlContainer::getPassword);
    }
}
