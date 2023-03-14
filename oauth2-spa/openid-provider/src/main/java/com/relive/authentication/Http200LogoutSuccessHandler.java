package com.relive.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author: ReLive
 * @date: 2023/3/14 19:54
 */
public class Http200LogoutSuccessHandler implements LogoutSuccessHandler {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Map<String, Object> responseClaims = new LinkedHashMap<>();
        responseClaims.put("code", HttpServletResponse.SC_OK);
        responseClaims.put("message", "success");
        try (Writer writer = response.getWriter()) {
            writer.write(objectMapper.writeValueAsString(responseClaims));
        }
    }
}
