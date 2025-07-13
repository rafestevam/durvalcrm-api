package br.org.cecairbar.durvalcrm.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTOs para responses de autenticação
 */
public class AuthDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginInfoResponse {
        private boolean authenticated;
        private String username;
        private String email;
        private String name;
        private String loginUrl;
        private String provider;
        private String realm;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LogoutResponse {
        private String message;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfoResponse {
        private String subject;
        private String username;
        private String email;
        private String name;
        private Object groups;
        private Object roles;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthCheckResponse {
        private boolean authenticated;
        private String username;
    }
}