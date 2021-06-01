package cc.eumc.eusminerhat.exception;

public class ContributionException extends Exception {
    public enum ContributionExceptionType {
        WALLET_INFORMATION_NOT_READY,
        CHECKOUT_WORKING_IN_PROGRESS,
        CHECKOUT_FAILED,
        NOT_ENOUGH_REVENUE,
        EXCHANGE_AMOUNT_TOO_SMALL
    }

    public ContributionExceptionType getType() {
        return type;
    }

    ContributionExceptionType type;

    public ContributionException(ContributionExceptionType type, String message) {
        super(message);

        this.type = type;
    }
}
