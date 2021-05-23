package cc.eumc.eusminerhat.exception;

public class MinerException extends Exception {
    public enum MinerExceptionType {
        FAILED_LOADING_POLICY,
        FAILED_STARTING,
        FAILED_TERMINATING
    }

    MinerExceptionType type;

    public MinerException(MinerExceptionType type, String message) {
        super(message);

        this.type = type;
    }
}

