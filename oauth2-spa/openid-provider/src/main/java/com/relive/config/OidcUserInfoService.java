package com.relive.config;

import com.nimbusds.jose.shaded.json.JSONObject;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Set;

/**
 * @author: ReLive
 * @date: 2023/3/14 19:08
 */
@Service
public class OidcUserInfoService {

    public OidcUserInfo loadUser(String name, Set<String> scopes) {
        OidcUserInfo.Builder builder = OidcUserInfo.builder().subject(name);
        if (!CollectionUtils.isEmpty(scopes)) {
            if (scopes.contains(OidcScopes.PROFILE)) {
                builder.name("First Last")
                        .givenName("First")
                        .familyName("Last")
                        .middleName("Middle")
                        .nickname("User")
                        .preferredUsername(name)
                        .profile("http://127.0.0.1:8080/" + name)
                        .picture("http://127.0.0.1:8080/" + name + ".jpg")
                        .website("http://127.0.0.1:8080/")
                        .gender("female")
                        .birthdate("2022-05-24")
                        .zoneinfo("China/Beijing")
                        .locale("zh-cn")
                        .updatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
            }
            if (scopes.contains(OidcScopes.EMAIL)) {
                builder.email(name + "@outlook.com").emailVerified(true);
            }
            if (scopes.contains(OidcScopes.ADDRESS)) {
                JSONObject address = new JSONObject();
                address.put("address", Collections.singletonMap("formatted", "Champ de Mars\n5 Av. Anatole France\n75007 Paris\nFrance"));
                builder.address(address.toJSONString());
            }
            if (scopes.contains(OidcScopes.PHONE)) {
                builder.phoneNumber("13728903134").phoneNumberVerified("false");
            }
        }
        return builder.build();
    }
}
