package com.relive.jose;

/**
 * The {@link KeyIDStrategy} that generates the timestamp.
 *
 * @author: ReLive
 * @date: 2022/8/23 19:28
 * @see KeyIDStrategy
 */
public class TimestampKeyIDStrategy implements KeyIDStrategy {

    @Override
    public String generateKeyID() {
        return String.valueOf(System.currentTimeMillis());
    }
}
