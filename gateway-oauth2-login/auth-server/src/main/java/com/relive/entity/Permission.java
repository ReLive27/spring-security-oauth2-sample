package com.relive.entity;

import jakarta.persistence.*;
import lombok.Data;

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
