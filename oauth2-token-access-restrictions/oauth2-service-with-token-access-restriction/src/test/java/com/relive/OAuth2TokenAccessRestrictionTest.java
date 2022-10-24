package com.relive;

import com.relive.config.AuthorizationServerConfig;
import com.relive.config.RedisConfig;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author: ReLive
 * @date: 2022/10/13 19:02
 */
@RunWith(SpringRunner.class)
@WebMvcTest
@SpringJUnitWebConfig(classes = {AuthorizationServerConfig.class, RedisConfig.class, RedisAutoConfiguration.class})
public class OAuth2TokenAccessRestrictionTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    public void authorizationWhenObtainingTheAccessTokenSucceeds() throws Exception {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.set(OAuth2ParameterNames.GRANT_TYPE, AuthorizationGrantType.CLIENT_CREDENTIALS.getValue());
        parameters.set(OAuth2ParameterNames.CLIENT_ID, "relive-client");
        parameters.set(OAuth2ParameterNames.CLIENT_SECRET, "relive-client");
        this.mockMvc.perform(post("/oauth2/token")
                .params(parameters))
                .andExpect(status().is2xxSuccessful());


    }

    @Test
    public void authorizationWhenTokenAccessRestrictionIsTriggeredThrowOAuth2AuthenticationException() throws Exception {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.set(OAuth2ParameterNames.GRANT_TYPE, AuthorizationGrantType.CLIENT_CREDENTIALS.getValue());
        parameters.set(OAuth2ParameterNames.CLIENT_ID, "relive-client");
        parameters.set(OAuth2ParameterNames.CLIENT_SECRET, "relive-client");
        this.mockMvc.perform(post("/oauth2/token")
                .params(parameters))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertEquals("{\"error_description\":\"The token generation fails, and the same client is prohibited from repeatedly obtaining the token within a short period of time.\",\"error\":\"access_denied\"}", result.getResponse().getContentAsString()));
    }
}
