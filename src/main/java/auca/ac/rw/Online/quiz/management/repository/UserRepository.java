package auca.ac.rw.Online.quiz.management.repository;

import auca.ac.rw.Online.quiz.management.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsernameIgnoreCase(String username);
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByUsernameIgnoreCase(String username);
    java.util.List<User> findByUsernameIgnoreCaseContaining(String username);
    org.springframework.data.domain.Page<User> findByUsernameIgnoreCaseContaining(String username, org.springframework.data.domain.Pageable pageable);
    
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.location")
    java.util.List<User> findAllWithLocation();
    
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.location WHERE u.id IN :ids")
    java.util.List<User> findAllWithLocationByIds(java.util.List<Long> ids);
    
    // Search across multiple fields: ID, username, email, and role
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.location WHERE " +
           "CAST(u.id AS string) LIKE %:searchTerm% OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(CAST(u.role AS string)) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    org.springframework.data.domain.Page<User> searchUsers(@Param("searchTerm") String searchTerm, org.springframework.data.domain.Pageable pageable);
}


