package goorm.tricount.util;

import goorm.tricount.enums.TricountApiErrorCode;

public class TricountApiException extends RuntimeException {
    private TricountApiErrorCode errorCode = TricountApiErrorCode.LOGIN_NEEDED;

    public TricountApiException(String message, TricountApiErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public TricountApiErrorCode getErrorCode() {
        return errorCode;
    }
}
