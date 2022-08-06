package com.relive.service;

import com.relive.entity.Role;
import com.relive.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户服务，用于Form认证时获取用户信息
 *
 * @author: ReLive
 * @date: 2022/8/4 19:27
 */
@RequiredArgsConstructor
public class JdbcUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        com.relive.entity.User user = userRepository.findUserByUsername(username);
        if (ObjectUtils.isEmpty(user)) {
            throw new UsernameNotFoundException("user is not found");
        }
        if (CollectionUtils.isEmpty(user.getRoleList())) {
            throw new UsernameNotFoundException("role is not found");
        }
        Set<SimpleGrantedAuthority> authorities = user.getRoleList().stream().map(Role::getRoleCode)
                .map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
        return new User(user.getUsername(), user.getPassword(), authorities);
    }
}
