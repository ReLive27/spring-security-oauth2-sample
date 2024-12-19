package com.relive.oauth2.client;

import com.relive.oauth2.client.authentication.OAuth2DeviceAuthorizationRequestToken;
import com.relive.oauth2.client.endpoint.OAuth2DeviceAuthorizationRequest;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.log.LogMessage;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;


/**
 * 设备授权请求过滤器，处理设备授权请求并生成设备授权码。
 * 此过滤器用于 OAuth2 设备授权流程中的授权请求处理，
 * 负责从请求中提取注册 ID，验证客户端信息，并保存授权请求。
 *
 * @author: ReLive27
 * @date: 2024/4/30 21:47
 */
@Slf4j
public class OAuth2DeviceAuthorizationRequestFilter extends OncePerRequestFilter {

    /**
     * URI 中的注册 ID 变量名称
     */
    private static final String REGISTRATION_ID_URI_VARIABLE_NAME = "registrationId";

    /**
     * 客户端注册未找到时的错误代码
     */
    private static final String CLIENT_REGISTRATION_NOT_FOUND_ERROR_CODE = "client_registration_not_found";

    /**
     * 默认的过滤器处理 URI（即设备授权请求的 URL 路径）
     */
    public static final String DEFAULT_FILTER_PROCESSES_URI = "/oauth2/device/authorization";

    /**
     * 设备授权请求存储库，用于存储和加载设备授权请求信息
     */
    private DeviceAuthorizationRequestRepository<OAuth2DeviceAuthorizationRequest> deviceCodeRepository = new HttpSessionOAuth2DeviceCodeRepository();

    /**
     * 认证管理器，用于处理设备授权认证
     */
    private final AuthenticationManager authenticationManager;

    /**
     * 客户端注册存储库，用于查找客户端注册信息
     */
    private ClientRegistrationRepository clientRegistrationRepository;

    /**
     * 授权请求的 URI 匹配器，用于匹配设备授权请求路径
     */
    private final AntPathRequestMatcher authorizationRequestMatcher;

    /**
     * 认证失败时的处理器
     */
    private AuthenticationFailureHandler authenticationFailureHandler = this::unsuccessfulRedirectForAuthorization;

    /**
     * 构造函数，初始化设备授权请求过滤器
     *
     * @param authenticationManager        认证管理器
     * @param clientRegistrationRepository 客户端注册存储库
     */
    public OAuth2DeviceAuthorizationRequestFilter(AuthenticationManager authenticationManager,
                                                  ClientRegistrationRepository clientRegistrationRepository) {
        this(authenticationManager, clientRegistrationRepository, DEFAULT_FILTER_PROCESSES_URI);
    }

    /**
     * 构造函数，初始化设备授权请求过滤器
     *
     * @param authenticationManager        认证管理器
     * @param clientRegistrationRepository 客户端注册存储库
     * @param authorizationRequestBaseUri  授权请求基础 URI
     */
    public OAuth2DeviceAuthorizationRequestFilter(AuthenticationManager authenticationManager,
                                                  ClientRegistrationRepository clientRegistrationRepository,
                                                  String authorizationRequestBaseUri) {
        Assert.notNull(authenticationManager, "authenticationManager cannot be null");
        Assert.notNull(clientRegistrationRepository, "clientRegistrationRepository cannot be null");
        Assert.hasText(authorizationRequestBaseUri, "authorizationRequestBaseUri cannot be empty");
        this.authenticationManager = authenticationManager;
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.authorizationRequestMatcher = new AntPathRequestMatcher(
                authorizationRequestBaseUri + "/{" + REGISTRATION_ID_URI_VARIABLE_NAME + "}");
    }

    /**
     * 处理设备授权请求的核心方法。此方法会检查请求的 URI 是否匹配授权请求路径，
     * 如果匹配，则加载客户端信息、生成授权请求并保存。
     *
     * @param request     当前的 HTTP 请求
     * @param response    当前的 HTTP 响应
     * @param filterChain 过滤器链
     * @throws ServletException Servlet 异常
     * @throws IOException      IO 异常
     */
    @SneakyThrows
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 如果 URI 不匹配，则直接继续处理请求
        if (!this.authorizationRequestMatcher.matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 提取注册 ID 并验证客户端
            String registrationId = this.resolveRegistrationId(request);
            if (registrationId == null) {
                OAuth2Error oauth2Error = new OAuth2Error(CLIENT_REGISTRATION_NOT_FOUND_ERROR_CODE,
                        "Client Registration not found with Id: " + registrationId, null);
                throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString());
            }

            ClientRegistration clientRegistration = this.clientRegistrationRepository.findByRegistrationId(registrationId);
            if (clientRegistration == null) {
                OAuth2Error oauth2Error = new OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT);
                throw new OAuth2AuthorizationException(oauth2Error);
            }

            if (log.isTraceEnabled()) {
                log.trace("Retrieved registered client");
            }

            OAuth2DeviceAuthorizationRequestToken authenticationToken = new OAuth2DeviceAuthorizationRequestToken(clientRegistration);
            Authentication authenticationResult = this.authenticationManager.authenticate(authenticationToken);

            // 创建授权请求并保存
            OAuth2DeviceAuthorizationRequest authorizationRequest = new OAuth2DeviceAuthorizationRequest();
            this.createOAuth2AuthorizationRequest(authorizationRequest, (OAuth2DeviceAuthorizationRequestToken) authenticationResult);
            this.deviceCodeRepository.saveAuthorizationRequest(authorizationRequest, request, response);

            // 发送设备验证信息
            sendDeviceVerification(request, response, (OAuth2DeviceAuthorizationRequestToken) authenticationResult);

        } catch (OAuth2AuthenticationException ex) {
            // 认证失败时处理
            if (this.logger.isTraceEnabled()) {
                this.logger.trace(LogMessage.format("Authorization request failed: %s", ex.getError()), ex);
            }
            this.authenticationFailureHandler.onAuthenticationFailure(request, response, ex);
        }
    }

    /**
     * 设置 OAuth2 设备授权请求
     *
     * @param authorizationRequest 设备授权请求
     * @param authenticationToken  认证令牌
     */
    private void createOAuth2AuthorizationRequest(OAuth2DeviceAuthorizationRequest authorizationRequest, OAuth2DeviceAuthorizationRequestToken authenticationToken) {
        authorizationRequest.setRegistrationId(authenticationToken.getClientRegistration().getRegistrationId());
        authorizationRequest.setDeviceCode(authenticationToken.getDeviceCode());
    }

    /**
     * 发送设备验证信息，通常是通过显示用户代码和验证 URI 提示用户进行设备认证
     *
     * @param request                            当前的 HTTP 请求
     * @param response                           当前的 HTTP 响应
     * @param authorizationConsentAuthentication 认证同意令牌
     * @throws IOException IO 异常
     */
    private void sendDeviceVerification(HttpServletRequest request, HttpServletResponse response,
                                        OAuth2DeviceAuthorizationRequestToken authorizationConsentAuthentication) throws IOException {

        String userCode = authorizationConsentAuthentication.getUserCode();
        String verificationUriComplete = authorizationConsentAuthentication.getVerificationUriComplete();
        String verificationUri = authorizationConsentAuthentication.getVerificationUri();

        // TODO: 重定向到自定义设备激活页面
        if (this.logger.isTraceEnabled()) {
            this.logger.trace("Displaying generated consent screen");
        }

        // 显示设备激活页面
        DefaultDeviceActivatePage.displayDeviceActivate(request, response, userCode, verificationUriComplete, verificationUri, Collections.emptyMap());
    }

    /**
     * 认证失败时的重定向处理
     *
     * @param request  当前的 HTTP 请求
     * @param response 当前的 HTTP 响应
     * @param ex       认证异常
     * @throws IOException IO 异常
     */
    private void unsuccessfulRedirectForAuthorization(HttpServletRequest request, HttpServletResponse response,
                                                      AuthenticationException ex) throws IOException {
        Throwable cause = ex.getCause();
        LogMessage message = LogMessage.format("Authorization Request failed: %s", cause);
        this.logger.error(message, ex);
        response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
    }

    /**
     * 解析请求中的注册 ID
     *
     * @param request 当前的 HTTP 请求
     * @return 注册 ID
     */
    private String resolveRegistrationId(HttpServletRequest request) {
        if (this.authorizationRequestMatcher.matches(request)) {
            return this.authorizationRequestMatcher.matcher(request)
                    .getVariables()
                    .get(REGISTRATION_ID_URI_VARIABLE_NAME);
        }
        return null;
    }

    // 以下是 setter 方法，用于设置依赖项（如设备授权请求存储库、认证失败处理器等）

    public void setDeviceCodeRepository(DeviceAuthorizationRequestRepository<OAuth2DeviceAuthorizationRequest> deviceCodeRepository) {
        this.deviceCodeRepository = deviceCodeRepository;
    }

    public void setAuthenticationFailureHandler(AuthenticationFailureHandler authenticationFailureHandler) {
        this.authenticationFailureHandler = authenticationFailureHandler;
    }
}
