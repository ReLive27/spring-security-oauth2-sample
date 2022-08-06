package com.relive.repository;

import com.relive.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author: ReLive
 * @date: 2022/8/4 12:28
 */
public interface RoleRepository extends JpaRepository<Role, Long> {

    Role findByRoleCode(String roleCode);
}
