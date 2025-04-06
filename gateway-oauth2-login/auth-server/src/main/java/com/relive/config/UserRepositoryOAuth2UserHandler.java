package com.relive.config;

import com.relive.entity.Role;
import com.relive.entity.User;
import com.relive.repository.RoleRepository;
import com.relive.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * OAuth2 登录用户持久化到用户信息表。
 * <p>
 * 本类实现了 {@link Consumer} 接口，用于在用户通过 OAuth2 登录成功后，
 * 将用户信息持久化到数据库中的用户表。它根据 OAuth2 用户信息创建一个新的用户，
 * 并为用户分配角色。默认情况下，角色为 {@code ROLE_OPERATION}，但可以根据需要自定义。
 * </p>
 *
 * <p><b>使用方式：</b></p>
 * <pre>
 *     UserRepositoryOAuth2UserHandler userHandler = new UserRepositoryOAuth2UserHandler(userRepository, roleRepository);
 *     userHandler.accept(oAuth2User);
 * </pre>
 *
 * @author ReLive
 * @date 2022/8/4 19:51
 * @see UserRepository
 * @see RoleRepository
 */
@Component
@RequiredArgsConstructor
public final class UserRepositoryOAuth2UserHandler implements Consumer<OAuth2User> {

    /**
     * 用户数据访问层，用于操作用户信息。
     */
    private final UserRepository userRepository;

    /**
     * 角色数据访问层，用于获取角色信息。
     */
    private final RoleRepository roleRepository;

    /**
     * 接受 OAuth2 用户信息并将其持久化到用户表中。
     * <p>
     * 此方法会检查数据库中是否存在与 OAuth2 用户名相同的用户记录。如果不存在，
     * 则创建新的 {@link User} 实体，并为其分配角色，然后保存到数据库。
     * </p>
     *
     * @param oAuth2User OAuth2 登录成功后的用户信息
     */
    @Override
    public void accept(OAuth2User oAuth2User) {
        // 将 OAuth2User 强制转换为 DefaultOAuth2User 类型
        DefaultOAuth2User defaultOAuth2User = (DefaultOAuth2User) oAuth2User;

        // 如果用户不存在，则进行持久化
        if (this.userRepository.findUserByUsername(oAuth2User.getName()) == null) {
            User user = new User();
            user.setUsername(defaultOAuth2User.getName());

            // 获取角色，并将其分配给用户，默认角色为 "ROLE_OPERATION"
            Role role = roleRepository.findByRoleCode(defaultOAuth2User.getAuthorities()
                    .stream().map(GrantedAuthority::getAuthority).findFirst().orElse("ROLE_OPERATION"));
            user.setRoleList(Arrays.asList(role));
            userRepository.save(user);
        }
    }
}
