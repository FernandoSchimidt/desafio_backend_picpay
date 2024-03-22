package br.com.fernandoschimidt.picpaydesafiobackend.authorization;

public record Authorization(
        String message) {
    public boolean isAuthorized() {
        return message.equals("Atorizado");
    }
}
