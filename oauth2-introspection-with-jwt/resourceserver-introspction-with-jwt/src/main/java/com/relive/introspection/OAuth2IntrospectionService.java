package com.relive.introspection;

/**
 * OAuth2 Introspection 服务接口，定义了对 OAuth2 introspection 数据的基本操作。
 * 该接口用于加载、保存和删除 OAuth2 Introspection 信息。
 *
 * @author: ReLive
 * @date: 2022/11/20 21:34
 */
public interface OAuth2IntrospectionService {

    /**
     * 根据发行者 URI 加载 OAuth2 Introspection 数据。
     *
     * @param issuer 发行者 URI
     * @return 返回对应的 OAuth2Introspection 对象
     */
    OAuth2Introspection loadIntrospection(String issuer);

    /**
     * 保存一个 OAuth2Introspection 对象。
     *
     * @param authorizedClient 要保存的 OAuth2Introspection 对象
     */
    void saveOAuth2Introspection(OAuth2Introspection authorizedClient);

    /**
     * 根据发行者 URI 删除 OAuth2 Introspection 数据。
     *
     * @param issuer 发行者 URI
     */
    void removeOAuth2Introspection(String issuer);
}
