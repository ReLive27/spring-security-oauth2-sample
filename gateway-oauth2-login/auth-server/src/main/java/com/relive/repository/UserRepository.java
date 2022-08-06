package com.relive.repository;

import com.relive.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author: ReLive
 * @date: 2022/8/02 20:20 下午
 */
public interface UserRepository extends JpaRepository<User, Long> {

    User findUserByUsername(String username);
}
