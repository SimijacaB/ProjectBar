package com.app.projectbar.application.implementation;

import com.app.projectbar.domain.enums.Permission;
import com.app.projectbar.domain.enums.RoleEnum;
import com.app.projectbar.domain.UserEntity;
import com.app.projectbar.infra.repositories.IUserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceSecurity implements UserDetailsService {
    private final IUserRepository userRepository;

    public UserServiceSecurity(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        UserEntity userEntity = userRepository.findById(username).orElseThrow(() -> new UsernameNotFoundException ("User " + username + " not found."));

        List<RoleEnum> roles = userEntity.getRoles().stream()
                .map(userRoleEntity -> RoleEnum.valueOf(userRoleEntity.name()))
                .toList();

        return User.builder()
                .username(userEntity.getUsername())
                .password(userEntity.getPassword())
                .authorities(this.grantedAuthorities(roles))
                .accountLocked(userEntity.getLocked())
                .disabled(userEntity.getDisabled())
                .build();
    }

    private List<GrantedAuthority> grantedAuthorities(List<RoleEnum> roles) {
        List<GrantedAuthority> authorities = new ArrayList<>(roles.size());
        for (RoleEnum role : roles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.name()));

            for (Permission permission : this.getPermissions(role)) {
                authorities.add(new SimpleGrantedAuthority(permission.name()));
            }
        }
        return authorities;
    }

    private List<Permission> getPermissions(RoleEnum role) {
        if (role == RoleEnum.ADMIN || role == RoleEnum.WAITER) {
            return List.of(Permission.READ, Permission.WRITE);
        }
        return List.of();
    }
}
