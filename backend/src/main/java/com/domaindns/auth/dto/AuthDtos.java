package com.domaindns.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AuthDtos {
    public static class RegisterReq {
        @NotBlank(message = "用户名必填")
        public String username;
        @Email(message = "邮箱格式不正确")
        public String email;
        @NotBlank(message = "密码必填")
        public String password;
        public String inviteCode;
    }

    public static class RegisterResp {
        public Long userId;
    }

    public static class LoginReq {
        public String username;
        public String email;
        @NotBlank(message = "密码必填")
        public String password;
    }

    public static class LoginResp {
        public String token;
        public String role;
    }

    public static class ForgotPasswordReq {
        @Email(message = "邮箱格式不正确")
        @NotBlank(message = "邮箱必填")
        public String email;
    }

    public static class ResetPasswordReq {
        @Email(message = "邮箱格式不正确")
        @NotBlank(message = "邮箱必填")
        public String email;
        @NotBlank(message = "验证码必填")
        public String code;
        @NotBlank(message = "新密码必填")
        public String newPassword;
    }
}
