package com.airmonitor.usermicroservice.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "device_tokens",
        indexes = {
                @Index(name="idx_dt_user", columnList="user_id"),
                @Index(name="idx_dt_token", columnList="token", unique = true)
        })
public class DeviceToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable=false)
    private Long userId;

    @Column(nullable=false, length=255)
    private String token;

    @Column(nullable=false, length=20)
    private String platform;

    @Column(nullable=false)
    private Boolean expo = true;

    @Column(nullable=false)
    private Instant updatedAt = Instant.now();

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getToken() { return token; }
    public String getPlatform() { return platform; }
    public Boolean getExpo() { return expo; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setId(Long id) { this.id = id; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setToken(String token) { this.token = token; }
    public void setPlatform(String platform) { this.platform = platform; }
    public void setExpo(Boolean expo) { this.expo = expo; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
