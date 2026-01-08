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
    
    @Query("SELECT u FROM UserEntity u JOIN u.roles r WHERE r = :role AND u.disabled = false AND u.locked = false")
    List<UserEntity> findByRoleAndActive(@Param("role") Role role);

}
