package com.relive.jose;

import java.util.UUID;

/**
 * 生成 {@link UUID} 的 {@link KeyIDStrategy}
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
