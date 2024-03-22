package br.com.fernandoschimidt.picpaydesafiobackend.transaction;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.fernandoschimidt.picpaydesafiobackend.authorization.AuthorizeService;
import br.com.fernandoschimidt.picpaydesafiobackend.notification.NotificationService;
import br.com.fernandoschimidt.picpaydesafiobackend.wallet.Wallet;
import br.com.fernandoschimidt.picpaydesafiobackend.wallet.WalletRepository;
import br.com.fernandoschimidt.picpaydesafiobackend.wallet.WalletType;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final AuthorizeService authorizeService;
    private final NotificationService notificationService;

    public TransactionService(TransactionRepository transactionRepository, WalletRepository walletRepository,
            AuthorizeService authorizeService, NotificationService notificationService) {
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
        this.authorizeService = authorizeService;
        this.notificationService = notificationService;
    }

    @Transactional
    public Transaction create(Transaction transaction) {

        validate(transaction);

        var newTransaction = this.transactionRepository.save(transaction);

        // debitar carteira
        var walletPayer = walletRepository.findById(transaction.payer()).get();
        var walletPayee = walletRepository.findById(transaction.payee()).get();
        walletRepository.save(walletPayer.debit(transaction.value()));
        walletRepository.save(walletPayee.credit(transaction.value()));

        // authorize transaction
        authorizeService.authorize(transaction);

        notificationService.notify(transaction);

        return newTransaction;
    }

    private void validate(Transaction transaction) {
        walletRepository.findById(transaction.payee())
                .map(payee -> walletRepository.findById(transaction.payer())
                        .map(payer -> isTransactionValid(transaction, payer) ? transaction : null)
                        .orElseThrow(() -> new InvalidTransactionException("Invalid Transaction - " + transaction)))
                .orElseThrow(() -> new InvalidTransactionException("Invalid Transaction - " + transaction));
    }

    private boolean isTransactionValid(Transaction transaction, Wallet payer) {
        return payer.type() == WalletType.COMUN.getValue() &&
                payer.balance().compareTo(transaction.value()) >= 0 &&
                !payer.id().equals(transaction.payee());

    }

    public List<Transaction> list() {
        return transactionRepository.findAll();
    }

}
