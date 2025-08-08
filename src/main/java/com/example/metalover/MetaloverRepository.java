package com.example.metalover;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MetaloverRepository extends JpaRepository<Metalover, Long> {
	
	Optional<Metalover> findByUsername(String username);
	Optional<Metalover> findByUsernameAndEmail(String username, String email);
    Optional<Metalover> findByUseridAndUsernameAndEmail(String userid, String username, String email);
    Optional<Metalover> findByEmail(String email);
    Optional<Metalover> findByUserid(String userid);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    
}
