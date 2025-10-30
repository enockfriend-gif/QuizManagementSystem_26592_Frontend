package auca.ac.rw.Online.quiz.management.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "locations",
    indexes = {
        @Index(name = "idx_location_province", columnList = "province_id, province_name"),
        @Index(name = "idx_location_district", columnList = "district_id, district_name"),
        @Index(name = "idx_location_sector", columnList = "sector_id, sector_name"),
        @Index(name = "idx_location_cell", columnList = "cell_id, cell_name"),
        @Index(name = "idx_location_village", columnList = "village_id, village_name")
    }
)
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "province_id", nullable = false)
    private Long provinceId;

    @Column(name = "province_name", nullable = false)
    private String provinceName;


    @Column(name = "district_id")
    private Long districtId;

    @Column(name = "district_name")
    private String districtName;


    @Column(name = "sector_id")
    private Long sectorId;

    @Column(name = "sector_name")
    private String sectorName;


    @Column(name = "cell_id")
    private Long cellId;

    @Column(name = "cell_name")
    private String cellName;


    @Column(name = "village_id")
    private Long villageId;

    @Column(name = "village_name")
    private String villageName;

    @Enumerated(EnumType.STRING)
    @Column(name = "location_type", nullable = false)
    private LocationType locationType;

    @OneToMany(mappedBy = "location")
    @JsonManagedReference(value = "location-users")
    private List<User> users = new ArrayList<>();

    public Location() {}

    public Location(Long id, Long provinceId, String provinceName, Long districtId, String districtName, Long sectorId, String sectorName, Long cellId, String cellName, Long villageId, String villageName, LocationType locationType) {
        this.id = id;
        this.provinceId = provinceId;
        this.provinceName = provinceName;
        this.districtId = districtId;
        this.districtName = districtName;
        this.sectorId = sectorId;
        this.sectorName = sectorName;
        this.cellId = cellId;
        this.cellName = cellName;
        this.villageId = villageId;
        this.villageName = villageName;
        this.locationType = locationType;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProvinceId() { return provinceId; }
    public void setProvinceId(Long provinceId) { this.provinceId = provinceId; }

    public String getProvinceName() { return provinceName; }
    public void setProvinceName(String provinceName) { this.provinceName = provinceName; }

    public Long getDistrictId() { return districtId; }
    public void setDistrictId(Long districtId) { this.districtId = districtId; }

    public String getDistrictName() { return districtName; }
    public void setDistrictName(String districtName) { this.districtName = districtName; }

    public Long getSectorId() { return sectorId; }
    public void setSectorId(Long sectorId) { this.sectorId = sectorId; }

    public String getSectorName() { return sectorName; }
    public void setSectorName(String sectorName) { this.sectorName = sectorName; }

    public Long getCellId() { return cellId; }
    public void setCellId(Long cellId) { this.cellId = cellId; }

    public String getCellName() { return cellName; }
    public void setCellName(String cellName) { this.cellName = cellName; }

    public Long getVillageId() { return villageId; }
    public void setVillageId(Long villageId) { this.villageId = villageId; }

    public String getVillageName() { return villageName; }
    public void setVillageName(String villageName) { this.villageName = villageName; }

    public LocationType getLocationType() { return locationType; }
    public void setLocationType(LocationType locationType) { this.locationType = locationType; }
}


