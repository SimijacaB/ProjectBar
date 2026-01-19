package com.app.projectbar.infra.repositories;

import com.app.projectbar.domain.UserEntity;
import com.app.projectbar.domain.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IUserRepository  extends JpaRepository <UserEntity, String>{

    Optional<UserEntity> findByEmail(String email);
    
    /**
     * Busca usuarios activos que tengan un rol específico
     */
    @Query("SELECT u FROM UserEntity u JOIN u.roles r WHERE r = :role AND u.disabled = false AND u.locked = false")
    List<UserEntity> findByRoleAndActive(@Param("role") Role role);
    
    /**
     * Busca TODOS los usuarios que tengan un rol específico (activos e inactivos)
     */
    @Query("SELECT u FROM UserEntity u JOIN u.roles r WHERE r = :role")
    List<UserEntity> findAllByRole(@Param("role") Role role);

}
