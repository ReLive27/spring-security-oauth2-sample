package com.relive.jose;

import com.nimbusds.jose.KeySourceException;

/**
 * @author: ReLive
 * @date: 2022/8/23 19:21
 */
public class RotateKeySourceException extends KeySourceException {
    public RotateKeySourceException(String message, Throwable cause) {
        super(message, cause);
    }

}
