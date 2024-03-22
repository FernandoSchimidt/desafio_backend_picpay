package br.com.fernandoschimidt.picpaydesafiobackend.authorization;

public class UnauthorizedTransactionExceprion extends RuntimeException {

    public UnauthorizedTransactionExceprion(String message) {
        super(message);
    }
}
