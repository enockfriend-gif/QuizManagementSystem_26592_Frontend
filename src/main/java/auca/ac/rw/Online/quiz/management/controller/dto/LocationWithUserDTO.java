package auca.ac.rw.Online.quiz.management.controller.dto;

import auca.ac.rw.Online.quiz.management.model.Location;
import auca.ac.rw.Online.quiz.management.model.User;

public class LocationWithUserDTO {
    private Long id;
    private String username;
    private String role;
    private Long provinceId;
    private String provinceName;
    private Long districtId;
    private String districtName;
    private Long sectorId;
    private String sectorName;
    private Long cellId;
    private String cellName;
    private Long villageId;
    private String villageName;
    private String locationType;

    public LocationWithUserDTO() {}

    public LocationWithUserDTO(Location location, User user) {
        this.id = location.getId();
        this.username = user != null ? user.getUsername() : null;
        this.role = user != null && user.getRole() != null ? user.getRole().name() : null;
        this.provinceId = location.getProvinceId();
        this.provinceName = location.getProvinceName();
        this.districtId = location.getDistrictId();
        this.districtName = location.getDistrictName();
        this.sectorId = location.getSectorId();
        this.sectorName = location.getSectorName();
        this.cellId = location.getCellId();
        this.cellName = location.getCellName();
        this.villageId = location.getVillageId();
        this.villageName = location.getVillageName();
        this.locationType = location.getLocationType() != null ? location.getLocationType().name() : null;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

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

    public String getLocationType() { return locationType; }
    public void setLocationType(String locationType) { this.locationType = locationType; }
}

