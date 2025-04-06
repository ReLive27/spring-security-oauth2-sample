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
 * 用户服务，用于在表单认证过程中获取用户信息
 * <p>
 * 该类实现了 `UserDetailsService` 接口，并重写了 `loadUserByUsername` 方法，
 * 从数据库中加载用户及其角色信息，返回 Spring Security 的 `UserDetails` 实现。
 * </p>
 *
 * @author ReLive
 * @date 2022/8/4 19:27
 * @see UserDetailsService
 * @see UserDetails
 */
@RequiredArgsConstructor
public class JdbcUserDetailsService implements UserDetailsService {

    /**
     * 用户信息存储库，用于查询用户数据。
     */
    private final UserRepository userRepository;

    /**
     * 根据用户名加载用户信息，并返回 `UserDetails`。
     * <p>
     * 该方法从 `userRepository` 中查找对应用户名的用户信息，如果未找到则抛出 `UsernameNotFoundException`。
     * 然后，从用户的角色列表中提取角色代码，转换为 Spring Security 所需的 `SimpleGrantedAuthority`，
     * 最后返回一个封装了用户信息的 `User` 对象。
     * </p>
     *
     * @param username 用户名，用于从数据库查找对应的用户信息
     * @return 一个包含用户信息和权限的 `UserDetails` 对象
     * @throws UsernameNotFoundException 如果找不到用户或用户没有角色，将抛出此异常
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 从数据库中查找用户信息
        com.relive.entity.User user = userRepository.findUserByUsername(username);

        // 如果未找到用户，抛出异常
        if (ObjectUtils.isEmpty(user)) {
            throw new UsernameNotFoundException("user is not found");
        }

        // 如果用户没有角色，抛出异常
        if (CollectionUtils.isEmpty(user.getRoleList())) {
            throw new UsernameNotFoundException("role is not found");
        }
        Set<SimpleGrantedAuthority> authorities = user.getRoleList().stream().map(Role::getRoleCode)
                .map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
        return new User(user.getUsername(), user.getPassword(), authorities);
    }
}
