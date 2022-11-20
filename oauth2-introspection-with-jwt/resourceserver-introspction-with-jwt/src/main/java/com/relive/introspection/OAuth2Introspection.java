package com.relive.introspection;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: ReLive
 * @date: 2022/11/20 21:46
 */
@Data
public final class OAuth2Introspection implements Serializable {
    private static final long serialVersionUID = 6932906039723670350L;

    private String issuer;

    private String clientId;

    private String clientSecret;

    private String introspectionUri;
}
