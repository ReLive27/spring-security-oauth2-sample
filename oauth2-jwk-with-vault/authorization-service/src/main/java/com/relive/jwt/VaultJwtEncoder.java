package com.relive.jwt;

import com.nimbusds.jose.*;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.util.Base64;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.SneakyThrows;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.support.Plaintext;
import org.springframework.vault.support.VaultTransitContext;

import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.util.*;

/**
 * @author: ReLive27
 * @date: 2024/7/7 22:04
 */
public final class VaultJwtEncoder implements JwtEncoder {
    private final VaultOperations vaultOperations;
    private static final JwsHeader DEFAULT_JWS_HEADER = JwsHeader.with(SignatureAlgorithm.RS256).type(JOSEObjectType.JWT.getType()).build();
    private String key = "oauth2";

    public VaultJwtEncoder(VaultOperations vaultOperations) {
        Assert.notNull(vaultOperations, "vaultOperations cannot be null");
        this.vaultOperations = vaultOperations;
    }

    @SneakyThrows
    @Override
    public Jwt encode(JwtEncoderParameters parameters) throws JwtEncodingException {
        JwsHeader headers = parameters.getJwsHeader();
        if (headers == null) {
            headers = DEFAULT_JWS_HEADER;
        }
        JWSHeader jwsHeader = convert(headers);
        JwtClaimsSet claims = parameters.getClaims();
        JWTClaimsSet jwtClaimsSet = convert(claims);

        JWSObject jwsObject = new JWSObject(jwsHeader, new Payload(jwtClaimsSet.toJSONObject()));

        // Sign the JWS object
        String signingInput = jwsObject.getSigningInput().toString();
        Plaintext plaintext = Plaintext.of(signingInput).with(VaultTransitContext.builder().build());
        String signature = vaultOperations.opsForTransit().sign(key, plaintext).getSignature();

        // Attach the signature to the JWS object
        Base64URL signatureBase64URL = Base64URL.from(signature);
        jwsObject = new JWSObject(jwsHeader.toBase64URL(), new Payload(jwtClaimsSet.toJSONObject()), signatureBase64URL);

        // Serialize JWS to compact format
        String jws = jwsObject.serialize();
        return new Jwt(jws, claims.getIssuedAt(), claims.getExpiresAt(), headers.getHeaders(), claims.getClaims());

    }

    public void setKey(String key) {
        this.key = key;
    }

    private static JWSHeader convert(JwsHeader headers) {
        com.nimbusds.jose.JWSHeader.Builder builder = new com.nimbusds.jose.JWSHeader.Builder(JWSAlgorithm.parse(headers.getAlgorithm().getName()));
        if (headers.getJwkSetUrl() != null) {
            builder.jwkURL(convertAsURI("jku", headers.getJwkSetUrl()));
        }

        Map<String, Object> jwk = headers.getJwk();
        if (!CollectionUtils.isEmpty(jwk)) {
            try {
                builder.jwk(JWK.parse(jwk));
            } catch (Exception var11) {
                throw new JwtEncodingException(String.format("An error occurred while attempting to encode the Jwt: %s", "Unable to convert 'jwk' JOSE header"), var11);
            }
        }

        String keyId = headers.getKeyId();
        if (StringUtils.hasText(keyId)) {
            builder.keyID(keyId);
        }

        if (headers.getX509Url() != null) {
            builder.x509CertURL(convertAsURI("x5u", headers.getX509Url()));
        }

        List<String> x509CertificateChain = headers.getX509CertificateChain();
        if (!CollectionUtils.isEmpty(x509CertificateChain)) {
            List<com.nimbusds.jose.util.Base64> x5cList = new ArrayList();
            x509CertificateChain.forEach((x5c) -> {
                x5cList.add(new Base64(x5c));
            });
            if (!x5cList.isEmpty()) {
                builder.x509CertChain(x5cList);
            }
        }

        String x509SHA1Thumbprint = headers.getX509SHA1Thumbprint();
        if (StringUtils.hasText(x509SHA1Thumbprint)) {
            builder.x509CertThumbprint(new Base64URL(x509SHA1Thumbprint));
        }

        String x509SHA256Thumbprint = headers.getX509SHA256Thumbprint();
        if (StringUtils.hasText(x509SHA256Thumbprint)) {
            builder.x509CertSHA256Thumbprint(new Base64URL(x509SHA256Thumbprint));
        }

        String type = headers.getType();
        if (StringUtils.hasText(type)) {
            builder.type(new JOSEObjectType(type));
        }

        String contentType = headers.getContentType();
        if (StringUtils.hasText(contentType)) {
            builder.contentType(contentType);
        }

        Set<String> critical = headers.getCritical();
        if (!CollectionUtils.isEmpty(critical)) {
            builder.criticalParams(critical);
        }

        Map<String, Object> customHeaders = new HashMap();
        headers.getHeaders().forEach((name, value) -> {
            if (!JWSHeader.getRegisteredParameterNames().contains(name)) {
                customHeaders.put(name, value);
            }

        });
        if (!customHeaders.isEmpty()) {
            builder.customParams(customHeaders);
        }

        return builder.build();
    }


    private static JWTClaimsSet convert(JwtClaimsSet claims) {
        com.nimbusds.jwt.JWTClaimsSet.Builder builder = new com.nimbusds.jwt.JWTClaimsSet.Builder();
        Object issuer = claims.getClaim("iss");
        if (issuer != null) {
            builder.issuer(issuer.toString());
        }

        String subject = claims.getSubject();
        if (StringUtils.hasText(subject)) {
            builder.subject(subject);
        }

        List<String> audience = claims.getAudience();
        if (!CollectionUtils.isEmpty(audience)) {
            builder.audience(audience);
        }

        Instant expiresAt = claims.getExpiresAt();
        if (expiresAt != null) {
            builder.expirationTime(Date.from(expiresAt));
        }

        Instant notBefore = claims.getNotBefore();
        if (notBefore != null) {
            builder.notBeforeTime(Date.from(notBefore));
        }

        Instant issuedAt = claims.getIssuedAt();
        if (issuedAt != null) {
            builder.issueTime(Date.from(issuedAt));
        }

        String jwtId = claims.getId();
        if (StringUtils.hasText(jwtId)) {
            builder.jwtID(jwtId);
        }

        Map<String, Object> customClaims = new HashMap();
        claims.getClaims().forEach((name, value) -> {
            if (!JWTClaimsSet.getRegisteredNames().contains(name)) {
                customClaims.put(name, value);
            }

        });
        if (!customClaims.isEmpty()) {
            Objects.requireNonNull(builder);
            customClaims.forEach(builder::claim);
        }

        return builder.build();
    }

    private static URI convertAsURI(String header, URL url) {
        try {
            return url.toURI();
        } catch (Exception var3) {
            throw new JwtEncodingException(String.format("An error occurred while attempting to encode the Jwt: %s", "Unable to convert '" + header + "' JOSE header to a URI"), var3);
        }
    }
}
