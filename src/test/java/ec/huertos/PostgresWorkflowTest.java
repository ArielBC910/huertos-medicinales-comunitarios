package ec.huertos;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.*;

/** Runs the same acceptance suite against real PostgreSQL when Docker is available. */
@Testcontainers(disabledWithoutDocker = true)
class PostgresWorkflowTest extends WorkflowTest {
  @Container
  static PostgreSQLContainer<?> database = new PostgreSQLContainer<>("postgres:17-alpine");

  @DynamicPropertySource
  static void database(DynamicPropertyRegistry properties) {
    properties.add("spring.datasource.url", database::getJdbcUrl);
    properties.add("spring.datasource.username", database::getUsername);
    properties.add("spring.datasource.password", database::getPassword);
  }
}
