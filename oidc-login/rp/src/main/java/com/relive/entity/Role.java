package com.relive.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;


/**
 * @author: ReLive
 * @date: 2022/7/13 12:31 下午
 */
@Data
@Entity
@Table(name = "`role`")
public class Role {
    @Id
    private Long id;
    private String roleCode;
}
