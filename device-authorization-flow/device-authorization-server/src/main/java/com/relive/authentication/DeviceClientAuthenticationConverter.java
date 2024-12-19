package com.relive.authentication;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * {@code DeviceClientAuthenticationConverter} 类实现了 {@link AuthenticationConverter} 接口，
 * 用于将设备授权请求或设备访问令牌请求转换为 {@link DeviceClientAuthenticationToken} 实例。
 *
 * <p>它主要处理以下两类请求：</p>
 * <ul>
 *     <li>设备授权请求（包含 "device_code" 参数）。</li>
 *     <li>设备访问令牌请求（包含 "grant_type=device_code" 和 "device_code" 参数）。</li>
 * </ul>
 *
 * <p>该转换器对请求参数进行严格验证，如校验 client_id 是否唯一且有效。</p>
 *
 * @author: ReLive27
 * @date: 2024/1/23 22:43
 */
public class DeviceClientAuthenticationConverter implements AuthenticationConverter {

    /**
     * 匹配设备授权请求的条件。
     */
    private final RequestMatcher deviceAuthorizationRequestMatcher;

    /**
     * 匹配设备访问令牌请求的条件。
     */
    private final RequestMatcher deviceAccessTokenRequestMatcher;

    /**
     * 构造函数，初始化请求匹配器。
     *
     * @param deviceAuthorizationEndpointUri 设备授权端点的 URI，用于匹配设备授权请求。
     */
    public DeviceClientAuthenticationConverter(String deviceAuthorizationEndpointUri) {
        // 匹配设备授权请求：路径匹配、响应类型为 device_code 且包含 client_id 参数
        this.deviceAuthorizationRequestMatcher = new AndRequestMatcher(
                new AntPathRequestMatcher(
                        deviceAuthorizationEndpointUri, HttpMethod.POST.name()),
                request -> "device_code".equals(request.getParameter(OAuth2ParameterNames.RESPONSE_TYPE)),
                request -> request.getParameter(OAuth2ParameterNames.CLIENT_ID) != null);

        // 匹配设备访问令牌请求：grant_type 为 device_code，且包含 device_code 和 client_id 参数
        this.deviceAccessTokenRequestMatcher = request ->
                AuthorizationGrantType.DEVICE_CODE.getValue().equals(request.getParameter(OAuth2ParameterNames.GRANT_TYPE)) &&
                        request.getParameter(OAuth2ParameterNames.DEVICE_CODE) != null &&
                        request.getParameter(OAuth2ParameterNames.CLIENT_ID) != null;
    }

    /**
     * 转换请求为 {@link DeviceClientAuthenticationToken}。
     *
     * @param request 当前的 HTTP 请求。
     * @return 如果请求匹配设备授权或设备访问令牌请求，返回一个 {@link DeviceClientAuthenticationToken}，否则返回 {@code null}。
     * @throws OAuth2AuthenticationException 如果请求参数无效（如缺少或多于一个 client_id）。
     */
    @Nullable
    @Override
    public Authentication convert(HttpServletRequest request) {
        // 请求不匹配设备授权请求或设备访问令牌请求，返回 null
        if (!this.deviceAuthorizationRequestMatcher.matches(request) &&
                !this.deviceAccessTokenRequestMatcher.matches(request)) {
            return null;
        }

        // 提取 client_id，确保非空且唯一
        String clientId = request.getParameter(OAuth2ParameterNames.CLIENT_ID);
        if (!StringUtils.hasText(clientId) ||
                request.getParameterValues(OAuth2ParameterNames.CLIENT_ID).length != 1) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_REQUEST);
        }

        // 提取额外的请求参数，排除 client_id 参数
        Map<String, Object> additionalParameters = getParametersIfMatchesDeviceCodeGrantRequest(request,
                OAuth2ParameterNames.CLIENT_ID);

        // 返回一个新的 DeviceClientAuthenticationToken 实例
        return new DeviceClientAuthenticationToken(clientId, ClientAuthenticationMethod.NONE, null, additionalParameters);
    }

    /**
     * 提取请求中的参数，排除指定的参数名（如 client_id）。
     *
     * @param request    当前的 HTTP 请求。
     * @param exclusions 要排除的参数名。
     * @return 返回一个包含请求参数的 Map，排除了指定的参数。
     */
    static Map<String, Object> getParametersIfMatchesDeviceCodeGrantRequest(HttpServletRequest request, String... exclusions) {
        MultiValueMap<String, String> multiValueParameters = getParameters(request);

        // 排除指定的参数
        for (String exclusion : exclusions) {
            multiValueParameters.remove(exclusion);
        }

        Map<String, Object> parameters = new HashMap<>();
        multiValueParameters.forEach((key, value) ->
                parameters.put(key, (value.size() == 1) ? value.get(0) : value.toArray(new String[0])));

        return parameters;
    }

    /**
     * 提取请求中的所有参数，并返回为 {@link MultiValueMap}。
     *
     * @param request 当前的 HTTP 请求。
     * @return 包含请求参数的 {@link MultiValueMap}。
     */
    static MultiValueMap<String, String> getParameters(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>(parameterMap.size());

        parameterMap.forEach((key, values) -> {
            if (values.length > 0) {
                for (String value : values) {
                    parameters.add(key, value);
                }
            }
        });

        return parameters;
    }
}
