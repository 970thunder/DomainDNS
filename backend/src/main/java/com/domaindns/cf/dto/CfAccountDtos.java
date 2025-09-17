package com.domaindns.cf.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public class CfAccountDtos {
    public static class CreateReq {
        public String name;
        @Email(message = "邮箱格式不正确")
        public String email;
        @NotBlank(message = "apiType 不能为空，填写 GLOBAL_KEY 或 API_TOKEN")
        public String apiType; // GLOBAL_KEY / API_TOKEN
        @NotBlank(message = "凭证不能为空")
        public String apiKey; // Global Key 或 API Token
        public Boolean enabled;
    }

    public static class UpdateReq {
        public String name;
        @Email(message = "邮箱格式不正确")
        public String email;
        public String apiType; // 可选
        public String apiKey; // 可选
        public Boolean enabled;
    }

    public static class ItemResp {
        public Long id;
        public String name;
        public String email;
        public String apiType;
        public Integer enabled;
        public LocalDateTime createdAt;
    }
}
