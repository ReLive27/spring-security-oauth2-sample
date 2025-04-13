package com.relive.introspection;

import lombok.Data;
import org.springframework.util.Assert;

import java.io.Serializable;

/**
 * OAuth2 Introspection 数据对象，表示一个 OAuth2 introspection 结果。
 * 使用 Builder 模式创建该对象，确保对象的不可变性和灵活的构建方式。
 *
 * @author: ReLive
 * @date: 2022/11/20 21:46
 */
@Data
public final class OAuth2Introspection implements Serializable {
    private static final long serialVersionUID = 6932906039723670350L;

    /**
     * 唯一标识符
     */
    private String id;

    /**
     * 发行者 URI（OpenID Connect 1.0 提供者或 OAuth 2.0 授权服务器的标识符 URI）
     */
    private String issuer;

    /**
     * 客户端 ID
     */
    private String clientId;

    /**
     * 客户端密钥
     */
    private String clientSecret;

    /**
     * Introspection 端点的 URI
     */
    private String introspectionUri;

    /**
     * 创建一个带有指定 issuer 的 Builder。
     *
     * @param issuer 发行者 URI
     * @return 返回一个 Builder 对象
     */
    public static Builder withIssuer(String issuer) {
        Assert.hasText(issuer, "issuer cannot be empty");
        return new Builder(issuer);
    }

    /**
     * {@link OAuth2Introspection} 的构建器。
     * 通过链式调用设置 OAuth2Introspection 对象的各个字段。
     */
    public static final class Builder implements Serializable {

        private String id;

        private String issuer;

        private String clientId;

        private String clientSecret;

        private String introspectionUri;

        /**
         * 构造器初始化，必须指定 issuer。
         *
         * @param issuer 发行者 URI
         */
        private Builder(String issuer) {
            this.issuer = issuer;
        }

        /**
         * 设置 id 字段。
         *
         * @param id 唯一标识符
         * @return 返回当前 Builder 对象
         */
        public Builder id(String id) {
            this.id = id;
            return this;
        }

        /**
         * 设置客户端 ID。
         *
         * @param clientId 客户端 ID
         * @return 返回当前 Builder 对象
         */
        public Builder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        /**
         * 设置客户端密钥。
         *
         * @param clientSecret 客户端密钥
         * @return 返回当前 Builder 对象
         */
        public Builder clientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
            return this;
        }

        /**
         * 设置发行者标识符 URI。
         *
         * @param issuerUri 发行者标识符 URI
         * @return 返回当前 Builder 对象
         */
        public Builder issuerUri(String issuerUri) {
            this.issuer = issuerUri;
            return this;
        }

        /**
         * 设置 introspection 端点的 URI。
         *
         * @param introspectionUri introspection 端点的 URI
         * @return 返回当前 Builder 对象
         */
        public Builder introspectionUri(String introspectionUri) {
            this.introspectionUri = introspectionUri;
            return this;
        }

        /**
         * 构建并返回一个 {@link OAuth2Introspection} 对象。
         *
         * @return 构建的 OAuth2Introspection 对象
         */
        public OAuth2Introspection build() {
            OAuth2Introspection oAuth2Introspection = new OAuth2Introspection();
            oAuth2Introspection.id = this.id;
            oAuth2Introspection.issuer = this.issuer;
            oAuth2Introspection.clientId = this.clientId;
            oAuth2Introspection.clientSecret = clientSecret;
            oAuth2Introspection.introspectionUri = this.introspectionUri;
            return oAuth2Introspection;
        }

    }
}
