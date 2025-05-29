package io.github.irfnhanif.rifasims.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "Username tidak boleh kosong")
    @Size(min = 3, max = 50, message = "Username harus antara 3 dan 50 karakter")
    private String username;

    @Column(nullable = false)
    @NotBlank(message = "Password tidak boleh kosong")
    @Size(min = 8, message = "Password minimal harus 8 karakter")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Column(nullable = false)
    @NotNull(message = "Cabang tidak boleh kosong")
    @Min(value = 1, message = "Cabang hanya boleh 1 atau 2")
    @Max(value = 2, message = "Cabang hanya boleh 1 atau 2")
    private Integer branch;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Peran tidak boleh kosong")
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Status tidak boleh kosong")
    private UserStatus status;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

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

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }
}

