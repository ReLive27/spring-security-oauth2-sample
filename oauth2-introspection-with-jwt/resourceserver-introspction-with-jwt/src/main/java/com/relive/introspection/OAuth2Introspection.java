package com.relive.introspection;

import lombok.Data;
import org.springframework.util.Assert;

import java.io.Serializable;

/**
 * @author: ReLive
 * @date: 2022/11/20 21:46
 */
@Data
public final class OAuth2Introspection implements Serializable {
    private static final long serialVersionUID = 6932906039723670350L;

    private String id;

    private String issuer;

    private String clientId;

    private String clientSecret;

    private String introspectionUri;

    public static Builder withIssuer(String issuer) {
        Assert.hasText(issuer, "issuer cannot be empty");
        return new Builder(issuer);
    }

    /**
     * A builder for {@link OAuth2Introspection}.
     */
    public static final class Builder implements Serializable {

        private String id;

        private String issuer;

        private String clientId;

        private String clientSecret;

        private String introspectionUri;

        private Builder(String issuer) {
            this.issuer = issuer;
        }

        /**
         * Sets the id.
         *
         * @param id the id
         * @return the {@link Builder}
         */
        public Builder id(String id) {
            this.id = id;
            return this;
        }

        /**
         * Sets the client identifier.
         *
         * @param clientId the client identifier
         * @return the {@link Builder}
         */
        public Builder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        /**
         * Sets the client secret.
         *
         * @param clientSecret the client secret
         * @return the {@link Builder}
         */
        public Builder clientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
            return this;
        }

        /**
         * Sets the issuer identifier uri for the OpenID Connect 1.0 provider or the OAuth
         * 2.0 Authorization Server.
         *
         * @param issuerUri the issuer identifier uri for the OpenID Connect 1.0 provider
         *                  or the OAuth 2.0 Authorization Server
         * @return the {@link Builder}
         */
        public Builder issuerUri(String issuerUri) {
            this.issuer = issuerUri;
            return this;
        }

        /**
         * Sets the uri for the introspection endpoint.
         *
         * @param introspectionUri the uri for the introspection endpoint
         * @return the {@link Builder}
         */
        public Builder introspectionUri(String introspectionUri) {
            this.introspectionUri = introspectionUri;
            return this;
        }

        /**
         * Builds a new {@link OAuth2Introspection}.
         *
         * @return a {@link OAuth2Introspection}
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
