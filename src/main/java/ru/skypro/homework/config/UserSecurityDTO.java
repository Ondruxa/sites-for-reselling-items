package ru.skypro.homework.config;

/**
 * Содержит только чтение данных и не вносит бизнес‑логики.
 */
public class UserSecurityDTO implements org.springframework.security.core.userdetails.UserDetails {
    private final ru.skypro.homework.model.UserEntity user;

    public UserSecurityDTO(ru.skypro.homework.model.UserEntity user) {
        this.user = user;
    }

    /**
     * Возвращает коллекцию прав (одна роль = одно GrantedAuthority).
     */
    @Override
    public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
        return java.util.Collections.singletonList(
                new org.springframework.security.core.authority.SimpleGrantedAuthority(user.getRole().name()));
    }

    /** @return хэш пароля пользователя */
    @Override
    public String getPassword() { return user.getPassword(); }

    /** @return email пользователя (используется как логин) */
    @Override
    public String getUsername() { return user.getEmail(); }

    /** Аккаунт не протухает в текущей реализации */
    @Override
    public boolean isAccountNonExpired() { return true; }

    /** Блокировка аккаунта не реализована */
    @Override
    public boolean isAccountNonLocked() { return true; }

    /** Срок действия учётных данных не ограничен */
    @Override
    public boolean isCredentialsNonExpired() { return true; }

    /** Все активные пользователи считаются включёнными */
    @Override
    public boolean isEnabled() { return true; }

    /** Доменная сущность (для сервисного слоя при необходимости). */
    public ru.skypro.homework.model.UserEntity getUser() { return user; }
}
