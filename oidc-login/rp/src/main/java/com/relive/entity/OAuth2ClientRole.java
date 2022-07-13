package com.relive.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * @author: ReLive
 * @date: 2022/7/13 12:32 下午
 */
@Data
@Entity
@Table(name = "`oauth2_client_role`")
public class OAuth2ClientRole {
    @Id
    private Long id;
    private String clientRegistrationId;
    private String roleCode;

    @ManyToOne
    @JoinTable(
            name = "oauth2_client_role_mapping",
            joinColumns = {
                    @JoinColumn(name = "oauth_client_role_id")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "role_id")
            }
    )
    private Role role;
}
