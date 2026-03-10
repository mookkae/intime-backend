package com.intime.common.exception;

import com.intime.common.BaseCode;
import lombok.Getter;

@Getter
public abstract class BaseCustomException extends RuntimeException {

    private final BaseCode baseCode;

    public BaseCustomException(BaseCode baseCode) {
        super(baseCode.getMessage());
        this.baseCode = baseCode;
    }

    public BaseCustomException(BaseCode baseCode, String customMessage) {
        super(customMessage);
        this.baseCode = baseCode;
    }
}
