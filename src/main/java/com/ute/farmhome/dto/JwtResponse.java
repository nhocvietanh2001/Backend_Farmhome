package com.ute.farmhome.dto;

import lombok.Data;

import java.util.List;

@Data
public class JwtResponse {
    private String accessToken;
    private String refreshToken;
    private String type="Bearer";
    private String username;
    private String firstName;
    private String lastName;
    private String avatar;
    private long idUser;
    private String success = "True";
    private String message = "";
    public JwtResponse(String accessToken, String refreshToken, String username, String firstName, String lastName, String avatar, long id) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.avatar = avatar;
        this.idUser = id;
    }
}
