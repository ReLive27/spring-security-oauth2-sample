package com.relive.jose;

import com.nimbusds.jose.KeySourceException;

/**
 * @author: ReLive
 * @date: 2022/10/3 22:12
 */
public class ConsulConfigKeySourceException extends KeySourceException {
    public ConsulConfigKeySourceException(String message, Throwable cause) {
        super(message, cause);
    }

}
