package com.relive.jose;

import java.util.UUID;

/**
 * The {@link KeyIDStrategy} that generates the {@link UUID}.
 *
 * @author: ReLive
 * @date: 2022/8/23 13:12
 */
@Deprecated
public class UUIDKeyIDStrategy implements KeyIDStrategy {

    @Override
    public String generateKeyID() {
        return UUID.randomUUID().toString();
    }
}
