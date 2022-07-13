package com.relive.repository;

import com.relive.entity.OAuth2ClientRole;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author: ReLive
 * @date: 2022/7/13 1:01 下午
 */
public interface OAuth2ClientRoleRepository extends JpaRepository<OAuth2ClientRole, Long> {

    OAuth2ClientRole findByClientRegistrationIdAndRoleCode(String clientRegistrationId, String roleCode);
}
