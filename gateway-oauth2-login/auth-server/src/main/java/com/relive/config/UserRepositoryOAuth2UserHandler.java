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
 * Persist OAuth2 Login User to User Information Table
 *
 * @author: ReLive
 * @date: 2022/8/4 19:51
 */
@Component
@RequiredArgsConstructor
public final class UserRepositoryOAuth2UserHandler implements Consumer<OAuth2User> {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    @Override
    public void accept(OAuth2User oAuth2User) {
        DefaultOAuth2User defaultOAuth2User = (DefaultOAuth2User) oAuth2User;
        if (this.userRepository.findUserByUsername(oAuth2User.getName()) == null) {
            User user = new User();
            user.setUsername(defaultOAuth2User.getName());
            Role role = roleRepository.findByRoleCode(defaultOAuth2User.getAuthorities()
                    .stream().map(GrantedAuthority::getAuthority).findFirst().orElse("ROLE_OPERATION"));
            user.setRoleList(Arrays.asList(role));
            userRepository.save(user);
        }
    }
}
