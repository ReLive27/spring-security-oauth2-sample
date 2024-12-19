package com.relive.oauth2.client;

import com.relive.oauth2.client.authentication.OAuth2DeviceAuthenticationToken;
import com.relive.oauth2.client.endpoint.OAuth2DeviceAuthorizationRequest;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.AuthenticatedPrincipalOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.util.Assert;

import java.io.IOException;


/**
 * 设备授权认证过滤器，处理基于设备授权码的 OAuth2 认证。
 *
 * @author: ReLive27
 * @date: 2024/5/1 21:03
 * @see AbstractAuthenticationProcessingFilter
 */
public class OAuth2DeviceAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    /**
     * 默认的过滤器处理 URI（即设备授权认证请求的 URL 路径）。
     */
    public static final String DEFAULT_FILTER_PROCESSES_URI = "/login/oauth2/device/token";

    /**
     * 客户端注册存储库，用于获取与设备授权请求相关的客户端注册信息。
     */
    private ClientRegistrationRepository clientRegistrationRepository;

    /**
     * 授权客户端存储库，用于存储成功认证后的授权客户端信息。
     */
    private OAuth2AuthorizedClientRepository authorizedClientRepository;

    /**
     * 设备授权请求存储库，用于存储和加载设备授权请求信息。
     */
    private DeviceAuthorizationRequestRepository<OAuth2DeviceAuthorizationRequest> deviceCodeRepository = new HttpSessionOAuth2DeviceCodeRepository();

    /**
     * 用于将设备授权认证令牌转换为 OAuth2 认证令牌的转换器。
     */
    private Converter<OAuth2DeviceAuthenticationToken, OAuth2AuthenticationToken> authenticationResultConverter = this::createAuthenticationResult;

    /**
     * 构造函数，初始化设备认证过滤器。
     *
     * @param clientRegistrationRepository 客户端注册存储库
     * @param authorizedClientService      授权客户端服务
     */
    public OAuth2DeviceAuthenticationFilter(ClientRegistrationRepository clientRegistrationRepository,
                                            OAuth2AuthorizedClientService authorizedClientService) {
        this(clientRegistrationRepository, new AuthenticatedPrincipalOAuth2AuthorizedClientRepository(authorizedClientService), DEFAULT_FILTER_PROCESSES_URI);
    }

    /**
     * 构造函数，初始化设备认证过滤器。
     *
     * @param clientRegistrationRepository 客户端注册存储库
     * @param authorizedClientRepository   授权客户端存储库
     * @param defaultFilterProcessesUrl    默认过滤器处理 URI
     */
    public OAuth2DeviceAuthenticationFilter(ClientRegistrationRepository clientRegistrationRepository,
                                            OAuth2AuthorizedClientRepository authorizedClientRepository,
                                            String defaultFilterProcessesUrl) {
        super(defaultFilterProcessesUrl);
        Assert.notNull(clientRegistrationRepository, "clientRegistrationRepository cannot be null");
        Assert.notNull(authorizedClientRepository, "authorizedClientRepository cannot be null");
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.authorizedClientRepository = authorizedClientRepository;
    }

    /**
     * 处理设备授权认证请求。
     * 该方法从设备授权请求中加载授权信息，验证客户端，执行设备认证，并保存授权客户端信息。
     *
     * @param request  当前的 HTTP 请求
     * @param response 当前的 HTTP 响应
     * @return 返回 OAuth2 认证令牌
     * @throws AuthenticationException 如果认证失败，则抛出此异常
     * @throws IOException             IO 异常
     * @throws ServletException        Servlet 异常
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {

        // 从存储库加载设备授权请求
        OAuth2DeviceAuthorizationRequest deviceAuthorizationRequest = this.deviceCodeRepository
                .loadAuthorizationRequest(request);
        if (deviceAuthorizationRequest == null) {
            OAuth2Error oauth2Error = new OAuth2Error("device_authorization_request_not_found");
            throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString());
        }

        // 获取客户端注册信息
        String registrationId = deviceAuthorizationRequest.getRegistrationId();
        ClientRegistration clientRegistration = this.clientRegistrationRepository.findByRegistrationId(registrationId);
        if (clientRegistration == null) {
            OAuth2Error oauth2Error = new OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT,
                    "Client Registration not found with Id: " + registrationId, null);
            throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString());
        }

        // 创建认证请求并调用认证管理器进行认证
        Object authenticationDetails = this.authenticationDetailsSource.buildDetails(request);
        OAuth2DeviceAuthenticationToken authenticationRequest = new OAuth2DeviceAuthenticationToken(clientRegistration, deviceAuthorizationRequest);
        authenticationRequest.setDetails(authenticationDetails);
        OAuth2DeviceAuthenticationToken authenticationResult = (OAuth2DeviceAuthenticationToken) this.getAuthenticationManager().authenticate(authenticationRequest);

        OAuth2AuthenticationToken oauth2Authentication = this.authenticationResultConverter
                .convert(authenticationResult);
        Assert.notNull(oauth2Authentication, "authentication result cannot be null");
        oauth2Authentication.setDetails(authenticationDetails);

        // 保存授权客户端信息
        OAuth2AuthorizedClient authorizedClient = new OAuth2AuthorizedClient(
                authenticationResult.getClientRegistration(), oauth2Authentication.getName(),
                authenticationResult.getAccessToken(), authenticationResult.getRefreshToken());

        this.authorizedClientRepository.saveAuthorizedClient(authorizedClient, oauth2Authentication, request, response);

        // 删除设备授权请求
        this.deviceCodeRepository.removeAuthorizationRequest(request, response);

        return oauth2Authentication;
    }

    /**
     * 用于将设备认证令牌转换为{@code OAuth2AuthenticationToken}的内部方法。
     *
     * @param authenticationResult 设备认证令牌
     * @return 转换后的 OAuth2 认证令牌
     */
    private OAuth2AuthenticationToken createAuthenticationResult(OAuth2DeviceAuthenticationToken authenticationResult) {
        return new OAuth2AuthenticationToken(authenticationResult.getPrincipal(), authenticationResult.getAuthorities(),
                authenticationResult.getClientRegistration().getRegistrationId());
    }

    // 以下是 setter 方法，用于设置依赖项（如客户端注册存储库、授权客户端存储库等）

    public void setClientRegistrationRepository(ClientRegistrationRepository clientRegistrationRepository) {
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    public void setAuthorizedClientRepository(OAuth2AuthorizedClientRepository authorizedClientRepository) {
        this.authorizedClientRepository = authorizedClientRepository;
    }

    public void setDeviceCodeRepository(DeviceAuthorizationRequestRepository<OAuth2DeviceAuthorizationRequest> deviceCodeRepository) {
        this.deviceCodeRepository = deviceCodeRepository;
    }

    public void setAuthenticationResultConverter(Converter<OAuth2DeviceAuthenticationToken, OAuth2AuthenticationToken> authenticationResultConverter) {
        this.authenticationResultConverter = authenticationResultConverter;
    }
}
