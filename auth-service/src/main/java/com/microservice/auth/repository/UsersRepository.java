package com.microservice.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.microservice.auth.entity.Users;

import io.lettuce.core.dynamic.annotation.Param;

@Repository
public interface UsersRepository extends JpaRepository<Users, String> {

	@Query(value = " SELECT u.* FROM users u WHERE u.user_name =:username AND u.tenant_id =:tenantId ", nativeQuery = true)
	Optional<Users> findByUsernameAndTenantId(@Param("username") String username, @Param("tenantId") String tenantId);

	@Query(value = " SELECT u.* FROM users u WHERE u.user_name =:username", nativeQuery = true)
	Optional<Users> findByUserName(@Param("username") String username);

}
