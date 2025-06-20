package com.app.projectbar.infra.repositories;

import com.app.projectbar.domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IUserRepository  extends JpaRepository <UserEntity, String>{

}
