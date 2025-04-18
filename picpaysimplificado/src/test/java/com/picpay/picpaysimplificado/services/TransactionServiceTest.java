package com.picpay.picpaysimplificado.services;

import com.picpay.picpaysimplificado.domain.user.User;
import com.picpay.picpaysimplificado.domain.user.UserType;
import com.picpay.picpaysimplificado.dtos.TransactionDTO;
import com.picpay.picpaysimplificado.repositories.TransactionRepository;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransactionServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private TransactionRepository repository;

    @Mock
    private AuthorizationService authService;

    @Mock
    private NotificationService notificationService;

    @Autowired
    @InjectMocks
    private TransactionService transactionService;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    @DisplayName("should create transaction successfully when everything is OK")
    void createTransactionCase1() throws Exception {
        User sender = new User(1L, "Felipe", "Silva", "09876543210", "felipe@exemplo.com.br", "54321", new BigDecimal(10), UserType.COMMON);
        User receiver = new User(2L, "João", "Silva", "09876543211", "joao@exemplo.com.br", "54321", new BigDecimal(50), UserType.COMMON);

        when(userService.findUserById(1L)).thenReturn(sender);
        when(userService.findUserById(2L)).thenReturn(receiver);

        when(authService.authorizeTransaction(any(), any())).thenReturn(true);

        TransactionDTO request = new TransactionDTO(new BigDecimal(10), 1L, 2L);
        transactionService.createTransaction(request);

        verify(repository, times(1)).save(any());

        sender.setBalance(new BigDecimal(0));
        verify(userService, times(1)).saveUser(sender);

        receiver.setBalance(new BigDecimal(60));
        verify(userService, times(1)).saveUser(receiver);

        verify(notificationService, times(1)).sendNotification(sender, "Transação enviada com sucesso");
        verify(notificationService, times(1)).sendNotification(receiver, "Transação recebida com sucesso");
    }

    @Test
    @DisplayName("should throw Exception when Transaction is not allowed")
    void createTransactionCase2() throws Exception {
        User sender = new User(1L, "Felipe", "Silva", "09876543210", "felipe@exemplo.com.br", "54321", new BigDecimal(10), UserType.COMMON);
        User receiver = new User(2L, "João", "Silva", "09876543211", "joao@exemplo.com.br", "54321", new BigDecimal(50), UserType.COMMON);

        when(userService.findUserById(1L)).thenReturn(sender);
        when(userService.findUserById(2L)).thenReturn(receiver);

        when(authService.authorizeTransaction(any(), any())).thenReturn(false);

        Exception thrown = Assertions.assertThrows(Exception.class, () -> {
            TransactionDTO request = new TransactionDTO(new BigDecimal(10), 1L, 2L);
            transactionService.createTransaction(request);
        });

        Assertions.assertEquals("Transação não autorizada", thrown.getMessage());
    }
}