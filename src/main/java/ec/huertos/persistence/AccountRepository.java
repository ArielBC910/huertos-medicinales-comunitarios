package ec.huertos.persistence;

import ec.huertos.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
  java.util.Optional<Account> findByUsername(String username);
}
