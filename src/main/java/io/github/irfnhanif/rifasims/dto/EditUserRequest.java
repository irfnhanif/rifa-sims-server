package io.github.irfnhanif.rifasims.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.*;

public class EditUserRequest {

    @Column(nullable = false, unique = true)
    @NotBlank(message = "Username tidak boleh kosong")
    @Size(min = 3, max = 50, message = "Username harus antara 3 dan 50 karakter")
    String username;

    @Column(nullable = false)
    @NotNull(message = "Cabang tidak boleh kosong")
    @Min(value = 1, message = "Cabang hanya boleh 1 atau 2")
    @Max(value = 2, message = "Cabang hanya boleh 1 atau 2")
    Integer branch;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getBranch() {
        return branch;
    }

    public void setBranch(Integer branch) {
        this.branch = branch;
    }
}
