package io.github.irfnhanif.rifasims.dto;

import io.github.irfnhanif.rifasims.entity.User;

public class UserWithTokenResponse {
    User user;
    String token;

    public UserWithTokenResponse(User user, String token) {
        this.user = user;
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
