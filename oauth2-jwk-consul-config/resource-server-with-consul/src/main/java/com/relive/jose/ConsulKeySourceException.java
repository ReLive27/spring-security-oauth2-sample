package com.relive.jose;

import com.nimbusds.jose.KeySourceException;

/**
 * @author: ReLive
 * @date: 2022/9/30 15:48
 */
public class ConsulKeySourceException extends KeySourceException {


    public ConsulKeySourceException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
