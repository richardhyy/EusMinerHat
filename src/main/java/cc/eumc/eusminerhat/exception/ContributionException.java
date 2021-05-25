package cc.eumc.eusminerhat.exception;

public class ContributionException extends Exception {
    public enum ContributionExceptionType {
        WalletInformationNotReady,
        CheckoutWorkingInProgress
    }

    ContributionExceptionType type;

    public ContributionException(ContributionExceptionType type, String message) {
        super(message);

        this.type = type;
    }
}
