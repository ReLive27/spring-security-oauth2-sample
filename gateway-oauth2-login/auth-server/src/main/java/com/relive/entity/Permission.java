package com.relive.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * @author: ReLive
 * @date: 2022/8/4 12:22
 */
@Data
@Entity
@Table(name = "`permission`")
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String permissionName;
    private String permissionCode;
}
