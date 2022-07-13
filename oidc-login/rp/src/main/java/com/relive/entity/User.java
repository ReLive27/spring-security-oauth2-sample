package com.relive.entity;


import lombok.Data;

import javax.persistence.*;
import java.util.List;

/**
 * @author: ReLive
 * @date: 2022/7/13 12:26 下午
 */
@Data
@Entity
@Table(name = "`user`")
public class User {
    @Id
    private Long id;
    private String username;
    private String password;

    @ManyToMany(cascade = CascadeType.ALL,fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_mtm_role",
            joinColumns = {
                    @JoinColumn(name = "user_id")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "role_id")
            }
    )
    private List<Role> roleList;
}
