package com.picpay.picpaysimplificado.repositories;

import com.picpay.picpaysimplificado.domain.user.User;
import com.picpay.picpaysimplificado.domain.user.UserType;
import com.picpay.picpaysimplificado.dtos.UserDTO;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    EntityManager entityManager;

    @Test
    void findUserById() {
    }

    @Test
    @DisplayName("should get User successfully from DB")
    void findUserByDocumentCase1() {
        String document = "09876543210";
        Optional<User> result = this.userRepository.findUserByDocument(document);
        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("should not get User from DB when user not exists")
    void findUserByDocumentCase2() {
        String document = "09876543210";
        UserDTO data = new UserDTO("Felipe", "Teste", document, new BigDecimal(10), "teste@exemplo.com", "senha123", UserType.COMMON);
        this.createUser(data);
        Optional<User> result = this.userRepository.findUserByDocument(document);
        assertThat(result.isPresent()).isTrue();
    }

    private User createUser(UserDTO data) {
        User newUser = new User(data);
        this.entityManager.persist(newUser);
        return newUser;
    }
}