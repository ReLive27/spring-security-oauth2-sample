package com.relive.config;

import com.nimbusds.jose.shaded.gson.JsonObject;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

/**
 * OIDC 用户信息服务
 * <p>
 * 根据授权请求中的 scope 填充并返回 OIDC 标准格式的用户信息（UserInfo）
 * </p>
 * 支持的 scope 包括：
 * - profile：用户基本信息
 * - email：用户邮箱
 * - address：用户地址
 * - phone：用户电话
 *
 * @author ReLive
 * @date 2023/3/14 19:08
 */
@Service
public class OidcUserInfoService {

    /**
     * 根据用户名和 scope 构造 OIDC 用户信息
     *
     * @param name   用户唯一标识（sub）
     * @param scopes 客户端申请的 OIDC 范围
     * @return 构建好的 {@link OidcUserInfo} 实例
     */
    public OidcUserInfo loadUser(String name, Set<String> scopes) {
        // 创建 UserInfo builder，指定 subject（唯一用户标识）
        OidcUserInfo.Builder builder = OidcUserInfo.builder().subject(name);

        if (!CollectionUtils.isEmpty(scopes)) {
            // profile scope：包含基础的用户资料
            if (scopes.contains(OidcScopes.PROFILE)) {
                builder.name("First Last")                                // 全名
                        .givenName("First")                              // 名
                        .familyName("Last")                              // 姓
                        .middleName("Middle")                            // 中间名
                        .nickname("User")                                // 昵称
                        .preferredUsername(name)                         // 用户名
                        .profile("http://127.0.0.1:8080/" + name)        // 个人资料页面链接
                        .picture("http://127.0.0.1:8080/" + name + ".jpg") // 头像链接
                        .website("http://127.0.0.1:8080/")               // 个人网站
                        .gender("female")                                // 性别
                        .birthdate("2022-05-24")                         // 出生日期
                        .zoneinfo("China/Beijing")                       // 时区
                        .locale("zh-cn")                                 // 地区语言
                        .updatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE)); // 更新时间
            }

            // email scope：邮箱相关字段
            if (scopes.contains(OidcScopes.EMAIL)) {
                builder.email(name + "@outlook.com")                     // 邮箱地址
                        .emailVerified(true);                            // 邮箱是否已验证
            }

            // address scope：地址字段（格式为 JSON 字符串）
            if (scopes.contains(OidcScopes.ADDRESS)) {
                JsonObject formatted = new JsonObject();
                formatted.addProperty("formatted", "Champ de Mars\n5 Av. Anatole France\n75007 Paris\nFrance");
                JsonObject address = new JsonObject();
                address.add("address", formatted);
                builder.address(address.toString());                     // 地址字段（作为 JSON 字符串）
            }

            // phone scope：电话字段
            if (scopes.contains(OidcScopes.PHONE)) {
                builder.phoneNumber("13728903134")                       // 手机号
                        .phoneNumberVerified(false);                     // 是否验证
            }
        }

        // 返回构建后的用户信息对象
        return builder.build();
    }
}
