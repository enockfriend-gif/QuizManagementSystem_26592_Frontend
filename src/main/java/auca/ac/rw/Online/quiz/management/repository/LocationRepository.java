package auca.ac.rw.Online.quiz.management.repository;

import auca.ac.rw.Online.quiz.management.model.Location;
import auca.ac.rw.Online.quiz.management.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    List<Location> findByProvinceId(Long provinceId);
    List<Location> findByProvinceName(String provinceName);

    @Query("SELECT u FROM User u WHERE u.location.provinceName = :provinceName")
    List<User> findUsersByProvinceName(@Param("provinceName") String provinceName);
    
    @Query("SELECT u FROM User u WHERE u.location.provinceId = :provinceId")
    List<User> findUsersByProvinceId(@Param("provinceId") Long provinceId);
}


