package br.com.fernandoschimidt.picpaydesafiobackend.wallet;

public enum WalletType {
    COMUN(1), LOJISTA(2);

    private int value;

    private WalletType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
