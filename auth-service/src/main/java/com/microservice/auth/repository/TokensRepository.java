package com.microservice.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.microservice.auth.entity.Tokens;

@Repository
public interface TokensRepository extends JpaRepository<Tokens, String> {

	@Query(value = " SELECT t.* FROM tokens t WHERE t.user_id =:userId AND t.token =:token ", nativeQuery = true)
	Optional<Tokens> findByUserTokenByUserIdAndToken(@Param("userId") String userId, @Param("token") String token);
}
