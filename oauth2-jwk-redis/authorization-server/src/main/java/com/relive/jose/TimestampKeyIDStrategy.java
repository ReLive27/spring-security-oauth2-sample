package com.relive.jose;

/**
 * 生成时间戳的 {@link KeyIDStrategy}
 *
 * @author: ReLive
 * @date: 2022/8/23 19:28
 * @see KeyIDStrategy
 */
public class TimestampKeyIDStrategy implements KeyIDStrategy {

    @Override
    public String generateKeyID() {
        return String.valueOf(System.nanoTime());
    }
}
