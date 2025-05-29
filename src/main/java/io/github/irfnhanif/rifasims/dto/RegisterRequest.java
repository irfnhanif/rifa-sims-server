package io.github.irfnhanif.rifasims.dto;

import jakarta.validation.constraints.*;

public class RegisterRequest {
    @NotBlank(message = "Username tidak boleh kosong")
    @Size(min = 3, max = 50, message = "Username harus memiliki 3-50 karakter")
    private String username;

    @NotBlank(message = "Password tidak boleh kosong")
    @Size(min = 8, max = 100, message = "Password harus memiliki minimal 8 karakter")
    private String password;

    @NotNull(message = "Cabang tidak boleh kosong")
    @Min(value = 1, message = "Cabang hanya boleh 1 atau 2")
    @Max(value = 2, message = "Cabang hanya boleh 1 atau 2")
    private Integer branch;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getBranch() {
        return branch;
    }

    public void setBranch(Integer branch) {
        this.branch = branch;
    }
}
