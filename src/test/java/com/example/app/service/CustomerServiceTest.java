package com.example.app.service;

import com.example.app.dao.CustomerRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

class CustomerServiceTest {
    @Test
    void registerNormalizesValuesBeforeDelegatingToTheRepository() throws SQLException {
        RecordingCustomerRepository repository = new RecordingCustomerRepository();
        CustomerService service = new CustomerService(repository);

        long id = service.register(" Alice@Example.COM ", " Alice ", " Liddell ");

        Assertions.assertEquals(42L, id);
        Assertions.assertEquals("alice@example.com", repository.email);
        Assertions.assertEquals("Alice", repository.firstName);
        Assertions.assertEquals("Liddell", repository.lastName);
    }

    @Test
    void registerRejectsInvalidCustomerDetailsWithoutDelegating() {
        RecordingCustomerRepository repository = new RecordingCustomerRepository();
        CustomerService service = new CustomerService(repository);

        Assertions.assertThrows(IllegalArgumentException.class, () -> service.register("invalid", "Alice", "Liddell"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> service.register("alice@example.com", " ", "Liddell"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> service.register("alice@example.com", "Alice", null));
        Assertions.assertNull(repository.email);
    }

    @Test
    void delegatesReadOperationsToTheRepository() throws SQLException {
        RecordingCustomerRepository repository = new RecordingCustomerRepository();
        CustomerService service = new CustomerService(repository);

        Assertions.assertEquals(7, service.count());
        Assertions.assertEquals(List.of("one@example.com", "two@example.com"), service.listEmails());
    }

    private static final class RecordingCustomerRepository implements CustomerRepository {
        private String email;
        private String firstName;
        private String lastName;

        @Override
        public long create(String email, String firstName, String lastName) {
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            return 42L;
        }

        @Override
        public int count() {
            return 7;
        }

        @Override
        public List<String> listEmails() {
            return List.of("one@example.com", "two@example.com");
        }
    }
}
