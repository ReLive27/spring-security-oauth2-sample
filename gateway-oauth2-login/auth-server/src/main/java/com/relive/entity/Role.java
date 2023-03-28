package com.relive.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

/**
 * @author: ReLive
 * @date: 2022/8/02 20:10 下午
 */
@Data
@Entity
@Table(name = "`role`")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String roleCode;

    @ManyToMany(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_mtm_permission",
            joinColumns = {
                    @JoinColumn(name = "role_id")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "permission_id")
            }
    )
    private List<Permission> permissions;
}
