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
    List<Location> findByProvinceIdAndDistrictId(Long provinceId, Long districtId);
    List<Location> findByProvinceIdAndDistrictIdAndSectorId(Long provinceId, Long districtId, Long sectorId);
    List<Location> findByProvinceIdAndDistrictIdAndSectorIdAndCellId(Long provinceId, Long districtId, Long sectorId, Long cellId);

    @Query("SELECT DISTINCT l.provinceId, l.provinceName FROM Location l WHERE l.provinceId IS NOT NULL AND l.provinceName IS NOT NULL ORDER BY l.provinceName")
    List<Object[]> findDistinctProvinces();
    
    @Query("SELECT DISTINCT l.districtId, l.districtName FROM Location l WHERE l.provinceId = :provinceId AND l.districtId IS NOT NULL AND l.districtName IS NOT NULL ORDER BY l.districtName")
    List<Object[]> findDistinctDistrictsByProvinceId(@Param("provinceId") Long provinceId);
    
    @Query("SELECT DISTINCT l.sectorId, l.sectorName FROM Location l WHERE l.provinceId = :provinceId AND l.districtId = :districtId AND l.sectorId IS NOT NULL AND l.sectorName IS NOT NULL ORDER BY l.sectorName")
    List<Object[]> findDistinctSectorsByProvinceAndDistrict(@Param("provinceId") Long provinceId, @Param("districtId") Long districtId);
    
    @Query("SELECT DISTINCT l.cellId, l.cellName FROM Location l WHERE l.provinceId = :provinceId AND l.districtId = :districtId AND l.sectorId = :sectorId AND l.cellId IS NOT NULL AND l.cellName IS NOT NULL ORDER BY l.cellName")
    List<Object[]> findDistinctCellsByProvinceDistrictAndSector(@Param("provinceId") Long provinceId, @Param("districtId") Long districtId, @Param("sectorId") Long sectorId);
    
    @Query("SELECT DISTINCT l.villageId, l.villageName FROM Location l WHERE l.provinceId = :provinceId AND l.districtId = :districtId AND l.sectorId = :sectorId AND l.cellId = :cellId AND l.villageId IS NOT NULL AND l.villageName IS NOT NULL ORDER BY l.villageName")
    List<Object[]> findDistinctVillagesByLocation(@Param("provinceId") Long provinceId, @Param("districtId") Long districtId, @Param("sectorId") Long sectorId, @Param("cellId") Long cellId);

    @Query("SELECT u FROM User u WHERE u.location.provinceName = :provinceName")
    List<User> findUsersByProvinceName(@Param("provinceName") String provinceName);
    
    @Query("SELECT u FROM User u WHERE u.location.provinceId = :provinceId")
    List<User> findUsersByProvinceId(@Param("provinceId") Long provinceId);
    
    org.springframework.data.domain.Page<Location> findByProvinceNameContainingIgnoreCaseOrDistrictNameContainingIgnoreCaseOrSectorNameContainingIgnoreCase(
        String provinceName, String districtName, String sectorName, org.springframework.data.domain.Pageable pageable);
    
    @Query("SELECT DISTINCT l FROM Location l LEFT JOIN FETCH l.users")
    List<Location> findAllWithUser();

    @Query("SELECT DISTINCT l FROM Location l LEFT JOIN FETCH l.users WHERE l.id IN :ids")
    List<Location> findAllWithUserByIds(@Param("ids") List<Long> ids);

    @Query("SELECT DISTINCT l FROM Location l LEFT JOIN FETCH l.users WHERE SIZE(l.users) > 0")
    List<Location> findAllWithUsers();

    @Query("SELECT DISTINCT l FROM Location l LEFT JOIN FETCH l.users WHERE SIZE(l.users) > 0")
    org.springframework.data.domain.Page<Location> findAllWithUsers(org.springframework.data.domain.Pageable pageable);

    @Query("SELECT DISTINCT l FROM Location l LEFT JOIN FETCH l.users WHERE SIZE(l.users) > 0 AND " +
           "(LOWER(l.provinceName) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(l.districtName) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(l.sectorName) LIKE LOWER(CONCAT('%', :q, '%')))")
    org.springframework.data.domain.Page<Location> findUserLocationsWithSearch(@Param("q") String q, org.springframework.data.domain.Pageable pageable);
}


