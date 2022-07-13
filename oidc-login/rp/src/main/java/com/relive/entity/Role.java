package com.relive.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

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
