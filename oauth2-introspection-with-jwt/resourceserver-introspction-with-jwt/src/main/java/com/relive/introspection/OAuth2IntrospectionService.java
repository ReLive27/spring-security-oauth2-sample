package com.relive.introspection;

/**
 * @author: ReLive
 * @date: 2022/11/20 21:34
 */
public interface OAuth2IntrospectionService {

    OAuth2Introspection loadIntrospection(String issuer);

    void saveOAuth2Introspection(OAuth2Introspection authorizedClient);

    void removeOAuth2Introspection(String issuer);
}
