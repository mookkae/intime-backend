package com.intime.common.exception;

import com.intime.common.BaseCode;

public class BusinessException extends BaseCustomException {

    public BusinessException(BaseCode baseCode) {
        super(baseCode);
    }

    public BusinessException(BaseCode baseCode, String customMessage) {
        super(baseCode, customMessage);
    }
}
