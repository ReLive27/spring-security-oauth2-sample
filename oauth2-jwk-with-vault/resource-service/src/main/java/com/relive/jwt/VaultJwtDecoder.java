package com.relive.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.support.Plaintext;
import org.springframework.vault.support.Signature;
import org.springframework.vault.support.VaultTransitContext;

import java.text.ParseException;
import java.util.*;

/**
 * @author: ReLive27
 * @date: 2024/7/7 22:34
 */
@Slf4j
public class VaultJwtDecoder implements JwtDecoder {
    private String key = "oauth2";
    private OAuth2TokenValidator<Jwt> jwtValidator = JwtValidators.createDefault();
    private Converter<Map<String, Object>, Map<String, Object>> claimSetConverter = MappedJwtClaimSetConverter.withDefaults(Collections.emptyMap());
    private final VaultOperations vaultOperations;


    public VaultJwtDecoder(VaultOperations vaultOperations) {
        Assert.notNull(vaultOperations, "vaultOperations cannot be null");
        this.vaultOperations = vaultOperations;
    }

    public void setJwtValidator(OAuth2TokenValidator<Jwt> jwtValidator) {
        Assert.notNull(jwtValidator, "jwtValidator cannot be null");
        this.jwtValidator = jwtValidator;
    }

    @SneakyThrows
    @Override
    public Jwt decode(String token) throws JwtException {
        SignedJWT jwt = this.parse(token);
        Jwt createdJwt = this.createJwt(token, jwt);
        return this.validateJwt(createdJwt);
    }

    private SignedJWT parse(String token) {
        try {
            return SignedJWT.parse(token);
        } catch (Exception e) {
            log.trace("Failed to parse token", e);
            throw new BadJwtException(String.format("An error occurred while attempting to decode the Jwt: %s", e.getMessage()), e);
        }
    }

    private Jwt createJwt(String token, SignedJWT parsedJwt) {
        try {
            // Verify signature using Vault
            String signingInput = new String(parsedJwt.getSigningInput());
            String signature = parsedJwt.getSignature().toString();

            Plaintext plaintext = Plaintext.of(signingInput).with(VaultTransitContext.builder().build());
            boolean isValid = vaultOperations.opsForTransit().verify(key, plaintext, Signature.of("vault:v1:" + signature));

            if (!isValid) {
                throw new JOSEException("Token signature is not valid");
            }
            JWTClaimsSet jwtClaimsSet = parsedJwt.getJWTClaimsSet();
            Map<String, Object> headers = new LinkedHashMap<>(parsedJwt.getHeader().toJSONObject());
            Map<String, Object> claims = this.claimSetConverter.convert(jwtClaimsSet.getClaims());
            return Jwt.withTokenValue(token).headers((h) -> {
                h.putAll(headers);
            }).claims((c) -> {
                c.putAll(claims);
            }).build();
        } catch (JOSEException e) {
            log.trace("Failed to process JWT", e);
            throw new JwtException(String.format("An error occurred while attempting to decode the Jwt: %s", e.getMessage()), e);
        } catch (Exception e) {
            log.trace("Failed to process JWT", e);
            if (e.getCause() instanceof ParseException) {
                throw new BadJwtException(String.format("An error occurred while attempting to decode the Jwt: %s", "Malformed payload"), e);
            } else {
                throw new BadJwtException(String.format("An error occurred while attempting to decode the Jwt: %s", e.getMessage()), e);
            }
        }
    }

    private Jwt validateJwt(Jwt jwt) {
        OAuth2TokenValidatorResult result = this.jwtValidator.validate(jwt);
        if (result.hasErrors()) {
            Collection<OAuth2Error> errors = result.getErrors();
            String validationErrorString = this.getJwtValidationExceptionMessage(errors);
            throw new JwtValidationException(validationErrorString, errors);
        } else {
            return jwt;
        }
    }

    private String getJwtValidationExceptionMessage(Collection<OAuth2Error> errors) {
        Iterator iterator = errors.iterator();

        OAuth2Error oAuth2Error;
        do {
            if (!iterator.hasNext()) {
                return "Unable to validate Jwt";
            }

            oAuth2Error = (OAuth2Error) iterator.next();
        } while (!StringUtils.hasText(oAuth2Error.getDescription()));

        return String.format("An error occurred while attempting to decode the Jwt: %s", oAuth2Error.getDescription());
    }
}
