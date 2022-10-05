package com.relive.jose;

/**
 * @author: ReLive
 * @date: 2022/10/3 22:50
 */
@FunctionalInterface
public interface KeyIDStrategy {

    String generateKeyID();
}
