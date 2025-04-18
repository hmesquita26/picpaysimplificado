package com.picpay.picpaysimplificado.services;

import com.picpay.picpaysimplificado.domain.transaction.Transaction;
import com.picpay.picpaysimplificado.domain.user.User;
import com.picpay.picpaysimplificado.dtos.TransactionDTO;
import com.picpay.picpaysimplificado.repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TransactionService {

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionRepository repository;

    @Autowired
    private AuthorizationService authService;

    @Autowired
    private NotificationService notificationService;

    public Transaction createTransaction(TransactionDTO transaction) throws Exception {
        User sender = this.userService.findUserById(transaction.senderId());
        User receiver = this.userService.findUserById(transaction.receiverId());

        userService.validateTRansaction(sender, transaction.value());

        boolean isAuthorized = this.authService.authorizeTransaction(sender, transaction.value());
        if (!isAuthorized)
            throw new Exception("Transação não autorizada");

        Transaction newTransaction = new Transaction();
        newTransaction.setAmount(transaction.value());
        newTransaction.setSender(sender);
        newTransaction.setReceiver(receiver);
        newTransaction.setTimestamp(LocalDateTime.now());

        sender.setBalance(sender.getBalance().subtract(transaction.value()));
        receiver.setBalance(receiver.getBalance().add(transaction.value()));

        this.repository.save(newTransaction);
        this.userService.saveUser(sender);
        this.userService.saveUser(receiver);

        this.notificationService.sendNotification(sender, "Transação enviada com sucesso");
        this.notificationService.sendNotification(receiver, "Transação recebida com sucesso");

        return newTransaction;
    }
}
