package com.relive.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * 自定义的 OAuth2 登录成功处理器。
 * <p>
 * 本类在用户通过 OAuth2 成功认证后，允许开发者在 {@link #oauth2UserHandler} 中处理用户信息，
 * 比如同步用户数据、记录日志或赋予角色等自定义逻辑。处理完成后，会委托给默认的
 * {@link SavedRequestAwareAuthenticationSuccessHandler}，实现跳转到用户原始请求页面。
 * </p>
 *
 * <p><b>使用方式：</b></p>
 * <pre>
 *     SavedUserAuthenticationSuccessHandler handler = new SavedUserAuthenticationSuccessHandler();
 *     handler.setOauth2UserHandler(user -> {
 *         // 自定义处理逻辑，如保存用户信息到数据库
 *     });
 * </pre>
 *
 * @author ReLive
 * @date 2022/8/4 19:59
 * @see SavedRequestAwareAuthenticationSuccessHandler
 */
public final class SavedUserAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    /**
     * 默认的认证成功处理器，处理跳转逻辑（如跳转回原请求地址）。
     */
    private final AuthenticationSuccessHandler delegate = new SavedRequestAwareAuthenticationSuccessHandler();

    /**
     * OAuth2 用户登录成功后的回调函数。
     * <p>
     * 可用于自定义处理登录用户数据，默认实现为空操作。
     * </p>
     */
    private Consumer<OAuth2User> oauth2UserHandler = (user) -> {
        // 默认什么也不做
    };

    /**
     * 处理认证成功逻辑。
     * <p>
     * 如果当前认证是 OAuth2 类型，并且 principal 是 OAuth2User，
     * 则调用自定义的 {@link #oauth2UserHandler} 进行处理，
     * 然后委托默认处理器执行跳转逻辑。
     * </p>
     *
     * @param request        请求对象
     * @param response       响应对象
     * @param authentication 当前认证信息
     * @throws IOException      I/O 异常
     * @throws ServletException Servlet 异常
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        if (authentication instanceof OAuth2AuthenticationToken) {
            if (authentication.getPrincipal() instanceof OAuth2User) {
                this.oauth2UserHandler.accept((OAuth2User) authentication.getPrincipal());
            }
        }

        this.delegate.onAuthenticationSuccess(request, response, authentication);
    }

    /**
     * 设置 OAuth2 登录成功时的用户处理逻辑。
     *
     * @param oauth2UserHandler 接收 OAuth2User 的处理逻辑
     */
    public void setOauth2UserHandler(Consumer<OAuth2User> oauth2UserHandler) {
        this.oauth2UserHandler = oauth2UserHandler;
    }
}
