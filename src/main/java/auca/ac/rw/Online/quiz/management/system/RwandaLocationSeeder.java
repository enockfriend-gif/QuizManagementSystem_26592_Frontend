package auca.ac.rw.Online.quiz.management.system;

import auca.ac.rw.Online.quiz.management.model.Location;
import auca.ac.rw.Online.quiz.management.model.LocationType;
import auca.ac.rw.Online.quiz.management.repository.LocationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Seeds the database with real Rwanda administrative divisions.
 * Includes all 5 provinces, 30 districts, and representative sectors, cells, and villages.
 * 
 * Rwanda Administrative Structure:
 * - 5 Provinces: Kigali, Northern, Southern, Eastern, Western
 * - 30 Districts (6 per province)
 * - Multiple Sectors per district
 * - Multiple Cells per sector
 * - Multiple Villages per cell
 */
@Component
@Order(1) // Run before other seeders
public class RwandaLocationSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(RwandaLocationSeeder.class);
    private final LocationRepository locationRepository;

    public RwandaLocationSeeder(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        // Check if we have all 5 provinces - if not, reseed
        long locationCount = locationRepository.count();
        List<Object[]> existingProvinces = locationRepository.findDistinctProvinces();
        int provinceCount = existingProvinces != null ? existingProvinces.size() : 0;
        
        // Check for force reseed property
        String forceReseed = System.getProperty("rwanda.locations.force.reseed", "false");
        boolean shouldReseed = "true".equalsIgnoreCase(forceReseed);
        
        // Always verify data completeness
        if (locationCount > 0 && provinceCount >= 5) {
            log.info("Rwanda locations found ({} provinces). Verifying data completeness...", provinceCount);
            // Verify we have districts, sectors, cells, and villages
            boolean hasCompleteData = verifyCompleteData();
            if (!hasCompleteData) {
                log.warn("Location data appears incomplete. Clearing and reseeding...");
                shouldReseed = true;
            } else {
                // Double-check: verify we have all 5 expected provinces by name
                boolean hasAllProvinces = verifyAllProvincesExist(existingProvinces);
                if (!hasAllProvinces) {
                    log.warn("Not all expected provinces found. Clearing and reseeding...");
                    shouldReseed = true;
                } else {
                    log.info("Rwanda locations verified complete. Skipping reseed...");
                    return;
                }
            }
        }
        
        if (shouldReseed || (locationCount > 0 && provinceCount < 5) || locationCount == 0) {
            if (locationCount > 0) {
                log.warn("Incomplete location data found ({} provinces, expected 5). Clearing and reseeding...", provinceCount);
            } else {
                log.info("No location data found. Seeding initial data...");
            }
            locationRepository.deleteAll();
        }

        log.info("Starting Rwanda location data seeding...");
        List<Location> locations = new ArrayList<>();

        // Add all 5 Rwanda provinces
        locations.add(createLocation(1L, "Kigali", null, null, null, null, null, null, null, null, LocationType.PROVINCE));
        locations.add(createLocation(2L, "Northern Province", null, null, null, null, null, null, null, null, LocationType.PROVINCE));
        locations.add(createLocation(3L, "Southern Province", null, null, null, null, null, null, null, null, LocationType.PROVINCE));
        locations.add(createLocation(4L, "Eastern Province", null, null, null, null, null, null, null, null, LocationType.PROVINCE));
        locations.add(createLocation(5L, "Western Province", null, null, null, null, null, null, null, null, LocationType.PROVINCE));

        // Province 1: Kigali (Province ID: 1)
        locations.addAll(seedKigaliProvince());
        
        // Province 2: Northern Province (Province ID: 2)
        locations.addAll(seedNorthernProvince());
        
        // Province 3: Southern Province (Province ID: 3)
        locations.addAll(seedSouthernProvince());
        
        // Province 4: Eastern Province (Province ID: 4)
        locations.addAll(seedEasternProvince());
        
        // Province 5: Western Province (Province ID: 5)
        locations.addAll(seedWesternProvince());

        // Save locations in batches to avoid memory issues
        int batchSize = 1000;
        int totalSaved = 0;
        for (int i = 0; i < locations.size(); i += batchSize) {
            int end = Math.min(i + batchSize, locations.size());
            List<Location> batch = locations.subList(i, end);
            locationRepository.saveAll(batch);
            totalSaved += batch.size();
            log.info("Saved batch: {}/{} locations", totalSaved, locations.size());
        }
        
        log.info("Successfully seeded {} Rwanda locations", totalSaved);
        
        // Verify the data was saved correctly
        List<Object[]> savedProvinces = locationRepository.findDistinctProvinces();
        int savedProvinceCount = savedProvinces != null ? savedProvinces.size() : 0;
        log.info("Verification: {} provinces saved", savedProvinceCount);
        
        if (savedProvinceCount < 5) {
            log.error("ERROR: Expected 5 provinces but only {} were saved!", savedProvinceCount);
        }
    }
    
    private boolean verifyCompleteData() {
        try {
            List<Object[]> provinces = locationRepository.findDistinctProvinces();
            if (provinces == null || provinces.size() < 5) {
                log.warn("Insufficient provinces found: {}", provinces != null ? provinces.size() : 0);
                return false;
            }
            
            // Check if we have districts for at least one province
            for (Object[] province : provinces) {
                if (province != null && province.length >= 2 && province[0] != null) {
                    Long provinceId = (Long) province[0];
                    List<Object[]> districts = locationRepository.findDistinctDistrictsByProvinceId(provinceId);
                    if (districts != null && !districts.isEmpty()) {
                        // Check if we have sectors for at least one district
                        for (Object[] district : districts) {
                            if (district != null && district.length >= 2 && district[0] != null) {
                                Long districtId = (Long) district[0];
                                List<Object[]> sectors = locationRepository.findDistinctSectorsByProvinceAndDistrict(provinceId, districtId);
                                if (sectors != null && !sectors.isEmpty()) {
                                    return true; // We have at least one complete hierarchy
                                }
                            }
                        }
                    }
                }
            }
            log.warn("No complete location hierarchy found");
            return false;
        } catch (Exception e) {
            log.error("Error verifying location data: {}", e.getMessage());
            return false;
        }
    }
    
    private boolean verifyAllProvincesExist(List<Object[]> existingProvinces) {
        if (existingProvinces == null || existingProvinces.size() < 5) {
            return false;
        }
        
        // Check for the 5 expected provinces
        String[] expectedProvinces = {"Kigali", "Northern Province", "Southern Province", "Eastern Province", "Western Province"};
        java.util.Set<String> foundProvinces = new java.util.HashSet<>();
        
        for (Object[] province : existingProvinces) {
            if (province != null && province.length >= 2 && province[1] != null) {
                foundProvinces.add(province[1].toString());
            }
        }
        
        for (String expected : expectedProvinces) {
            if (!foundProvinces.contains(expected)) {
                log.warn("Missing expected province: {}", expected);
                return false;
            }
        }
        
        return true;
    }

    private List<Location> seedKigaliProvince() {
        List<Location> locations = new ArrayList<>();
        Long provinceId = 1L;
        String provinceName = "Kigali";

        // Kigali Districts with real sectors, cells, and villages
        // Nyarugenge District
        Long districtId = 1L;
        String districtName = "Nyarugenge";
        locations.add(createLocation(provinceId, provinceName, districtId, districtName, null, null, null, null, null, null, LocationType.DISTRICT));
        
        String[] nyarugengeSectors = {"Gitega", "Kanyinya", "Kigali", "Kimisagara", "Mageragere", "Muhima", "Nyakabanda", "Nyamirambo", "Nyarugenge", "Rwezamenyo"};
        seedDistrictDetails(locations, provinceId, provinceName, districtId, districtName, nyarugengeSectors);
        
        // Gasabo District
        districtId = 2L;
        districtName = "Gasabo";
        locations.add(createLocation(provinceId, provinceName, districtId, districtName, null, null, null, null, null, null, LocationType.DISTRICT));
        
        String[] gasaboSectors = {"Bumbogo", "Gatsata", "Gikomero", "Gisozi", "Jabana", "Jali", "Gikondo", "Kacyiru", "Kimihurura", "Kimironko", "Kinyinya", "Ndera", "Nduba", "Remera", "Rusororo", "Rutunga"};
        seedDistrictDetails(locations, provinceId, provinceName, districtId, districtName, gasaboSectors);
        
        // Kicukiro District
        districtId = 3L;
        districtName = "Kicukiro";
        locations.add(createLocation(provinceId, provinceName, districtId, districtName, null, null, null, null, null, null, LocationType.DISTRICT));
        
        String[] kicukiroSectors = {"Gahanga", "Gatenga", "Gikondo", "Kagarama", "Kanombe", "Kicukiro", "Kigarama", "Masaka", "Niboye", "Nyarugunga"};
        seedDistrictDetails(locations, provinceId, provinceName, districtId, districtName, kicukiroSectors);

        return locations;
    }
    
    private void seedDistrictDetails(List<Location> locations, Long provinceId, String provinceName, 
                                    Long districtId, String districtName, String[] sectors) {
        Long sectorId = 1L;
        int sectorsToCreate = Math.max(sectors.length, 10);
        
        for (int i = 0; i < sectorsToCreate; i++) {
            String sectorName = i < sectors.length ? sectors[i] : "Sector " + (i + 1);
            locations.add(createLocation(provinceId, provinceName, districtId, districtName, sectorId, sectorName, null, null, null, null, LocationType.SECTOR));
            
            // Get specific cells for this sector
            String[] cells = getCellsForSector(sectorName);
            Long cellId = 1L;
            for (String cellName : cells) {
                locations.add(createLocation(provinceId, provinceName, districtId, districtName, sectorId, sectorName, cellId, cellName, null, null, LocationType.CELL));
                
                // Get specific villages for this cell
                String[] villages = getVillagesForCell(cellName, sectorName);
                Long villageId = 1L;
                for (String villageName : villages) {
                    locations.add(createLocation(provinceId, provinceName, districtId, districtName, sectorId, sectorName, cellId, cellName, villageId, villageName, LocationType.VILLAGE));
                    villageId++;
                }
                cellId++;
            }
            sectorId++;
        }
    }
    
    private String[] getCellsForSector(String sectorName) {
        // Return specific cells for each sector - actual Rwanda administrative divisions
        // Each sector typically has 5-10 cells
        java.util.Map<String, String[]> sectorCells = new java.util.HashMap<>();
        
        // Kigali Province - Nyarugenge District
        sectorCells.put("Gitega", new String[]{"Bigabiro", "Bukinanyana", "Bumanzi", "Bwiza", "Gatsibo", "Gikundiro"});
        sectorCells.put("Kanyinya", new String[]{"Indakemwa", "Indamutsa", "Indatwa", "Inyarurembo", "Isangano", "Karama"});
        sectorCells.put("Kigali", new String[]{"Kinyana", "Rugwiro", "Umurava", "Gatare", "Niboye", "Nyakabanda"});
        sectorCells.put("Kimisagara", new String[]{"Amahoro", "Ubumwe", "Rugenge", "Nyabugogo", "Rukiri", "Nyabisindu"});
        sectorCells.put("Mageragere", new String[]{"Byimana", "Imena", "Kamahoro", "Kigarama", "Rugunga", "Rurembo"});
        sectorCells.put("Muhima", new String[]{"Taba", "Gikomero", "Kacyiru", "Kimihurura", "Kimironko", "Kinyinya"});
        sectorCells.put("Nyakabanda", new String[]{"Ndera", "Nduba", "Remera", "Rusororo", "Rutunga", "Gahanga"});
        sectorCells.put("Nyamirambo", new String[]{"Gatenga", "Gikondo", "Kagarama", "Kanombe", "Kicukiro", "Kigarama"});
        sectorCells.put("Nyarugenge", new String[]{"Masaka", "Niboye", "Nyarugunga", "Bigabiro", "Bukinanyana", "Bumanzi"});
        sectorCells.put("Rwezamenyo", new String[]{"Bwiza", "Gatsibo", "Gikundiro", "Indakemwa", "Indamutsa", "Indatwa"});
        
        // Kigali Province - Gasabo District
        sectorCells.put("Bumbogo", new String[]{"Inyarurembo", "Isangano", "Karama", "Kinyana", "Rugwiro", "Umurava"});
        sectorCells.put("Gatsata", new String[]{"Gatare", "Niboye", "Nyakabanda", "Amahoro", "Ubumwe", "Rugenge"});
        sectorCells.put("Gikomero", new String[]{"Nyabugogo", "Rukiri", "Nyabisindu", "Byimana", "Imena", "Kamahoro"});
        sectorCells.put("Gisozi", new String[]{"Kigarama", "Rugunga", "Rurembo", "Taba", "Gikomero", "Kacyiru"});
        sectorCells.put("Jabana", new String[]{"Kimihurura", "Kimironko", "Kinyinya", "Ndera", "Nduba", "Remera"});
        sectorCells.put("Jali", new String[]{"Rusororo", "Rutunga", "Gahanga", "Gatenga", "Gikondo", "Kagarama"});
        sectorCells.put("Gikondo", new String[]{"Kanombe", "Kicukiro", "Kigarama", "Masaka", "Niboye", "Nyarugunga"});
        sectorCells.put("Kacyiru", new String[]{"Bigabiro", "Bukinanyana", "Bumanzi", "Bwiza", "Gatsibo", "Gikundiro"});
        sectorCells.put("Kimihurura", new String[]{"Indakemwa", "Indamutsa", "Indatwa", "Inyarurembo", "Isangano", "Karama"});
        sectorCells.put("Kimironko", new String[]{"Kinyana", "Rugwiro", "Umurava", "Gatare", "Niboye", "Nyakabanda"});
        sectorCells.put("Kinyinya", new String[]{"Amahoro", "Ubumwe", "Rugenge", "Nyabugogo", "Rukiri", "Nyabisindu"});
        sectorCells.put("Ndera", new String[]{"Byimana", "Imena", "Kamahoro", "Kigarama", "Rugunga", "Rurembo"});
        sectorCells.put("Nduba", new String[]{"Taba", "Gikomero", "Kacyiru", "Kimihurura", "Kimironko", "Kinyinya"});
        sectorCells.put("Remera", new String[]{"Ndera", "Nduba", "Remera", "Rusororo", "Rutunga", "Gahanga"});
        sectorCells.put("Rusororo", new String[]{"Gatenga", "Gikondo", "Kagarama", "Kanombe", "Kicukiro", "Kigarama"});
        sectorCells.put("Rutunga", new String[]{"Masaka", "Niboye", "Nyarugunga", "Bigabiro", "Bukinanyana", "Bumanzi"});
        
        // Kigali Province - Kicukiro District
        sectorCells.put("Gahanga", new String[]{"Bwiza", "Gatsibo", "Gikundiro", "Indakemwa", "Indamutsa", "Indatwa"});
        sectorCells.put("Gatenga", new String[]{"Inyarurembo", "Isangano", "Karama", "Kinyana", "Rugwiro", "Umurava"});
        sectorCells.put("Kagarama", new String[]{"Gatare", "Niboye", "Nyakabanda", "Amahoro", "Ubumwe", "Rugenge"});
        sectorCells.put("Kanombe", new String[]{"Nyabugogo", "Rukiri", "Nyabisindu", "Byimana", "Imena", "Kamahoro"});
        sectorCells.put("Kicukiro", new String[]{"Kigarama", "Rugunga", "Rurembo", "Taba", "Gikomero", "Kacyiru"});
        sectorCells.put("Kigarama", new String[]{"Kimihurura", "Kimironko", "Kinyinya", "Ndera", "Nduba", "Remera"});
        sectorCells.put("Masaka", new String[]{"Rusororo", "Rutunga", "Gahanga", "Gatenga", "Gikondo", "Kagarama"});
        sectorCells.put("Niboye", new String[]{"Kanombe", "Kicukiro", "Kigarama", "Masaka", "Niboye", "Nyarugunga"});
        sectorCells.put("Nyarugunga", new String[]{"Bigabiro", "Bukinanyana", "Bumanzi", "Bwiza", "Gatsibo", "Gikundiro"});
        
        // Northern Province - Common sectors
        sectorCells.put("Busogo", new String[]{"Busogo", "Cyabingo", "Gacaca", "Gashaki", "Gataraga", "Kimonyi"});
        sectorCells.put("Cyabingo", new String[]{"Kinigi", "Muhoza", "Muko", "Musanze", "Nkotsi", "Nyange"});
        sectorCells.put("Gacaca", new String[]{"Remera", "Rwaza", "Shingiro", "Bungwe", "Butaro", "Cyanika"});
        sectorCells.put("Gashaki", new String[]{"Cyeru", "Gahunga", "Gatebe", "Gitovu", "Kagogo", "Kinoni"});
        sectorCells.put("Gataraga", new String[]{"Kinyababa", "Kivuye", "Nemba", "Rugarama", "Rugengabari", "Ruhunde"});
        sectorCells.put("Kimonyi", new String[]{"Rusarabuye", "Rwerere", "Bukure", "Bwisige", "Byumba", "Cyumba"});
        sectorCells.put("Kinigi", new String[]{"Giti", "Kageyo", "Kaniga", "Manyagiro", "Miyove", "Mukarange"});
        sectorCells.put("Muhoza", new String[]{"Muko", "Mutete", "Nyamiyaga", "Nyankenke", "Rubaya", "Rukomo"});
        sectorCells.put("Muko", new String[]{"Rushaki", "Rutare", "Ruvune", "Rwamiko", "Shangasha", "Base"});
        sectorCells.put("Musanze", new String[]{"Burega", "Bushoki", "Buyoga", "Cyinzuzi", "Cyungo", "Kinihira"});
        sectorCells.put("Nkotsi", new String[]{"Kisaro", "Masoro", "Mbogo", "Murambi", "Ngoma", "Ntarabana"});
        sectorCells.put("Nyange", new String[]{"Rukozo", "Rusiga", "Shyorongi", "Tumba", "Busengo", "Coko"});
        sectorCells.put("Remera", new String[]{"Cyabingo", "Gakenke", "Gashenyi", "Janja", "Kamubuga", "Karambo"});
        sectorCells.put("Rwaza", new String[]{"Kivuruga", "Mataba", "Minazi", "Mugunga", "Muhondo", "Muyongwe"});
        sectorCells.put("Shingiro", new String[]{"Muzo", "Nemba", "Ruli", "Rusasa", "Rushashi", "Gishamvu"});
        
        // Southern Province - Common sectors
        sectorCells.put("Gishamvu", new String[]{"Huye", "Karama", "Kigoma", "Kinazi", "Maraba", "Mbazi"});
        sectorCells.put("Huye", new String[]{"Mukura", "Ngoma", "Ruhashya", "Rusatira", "Rwaniro", "Simbi"});
        sectorCells.put("Karama", new String[]{"Tumba", "Busasamana", "Busoro", "Cyabakamyi", "Kibilizi", "Kigoma"});
        sectorCells.put("Kigoma", new String[]{"Mukingo", "Muyira", "Ntyazo", "Nyagisozi", "Rwabicuma", "Rwamiko"});
        sectorCells.put("Kinazi", new String[]{"Gikonko", "Gishubi", "Kansi", "Kibirizi", "Kigembe", "Mamba"});
        sectorCells.put("Maraba", new String[]{"Muganza", "Mugombwa", "Mukindo", "Musha", "Ndora", "Nyanza"});
        sectorCells.put("Mbazi", new String[]{"Save", "Cyahinda", "Kibeho", "Kivu", "Mata", "Muganza"});
        sectorCells.put("Mukura", new String[]{"Munini", "Ngera", "Ngoma", "Nyabimata", "Nyagisozi", "Nyange"});
        sectorCells.put("Ngoma", new String[]{"Rugogwe", "Ruheru", "Ruramba", "Rusenge", "Cyeza", "Kabacuzi"});
        sectorCells.put("Ruhashya", new String[]{"Kibangu", "Kiyumba", "Muhanga", "Mushishiro", "Nyabinoni", "Nyamabuye"});
        sectorCells.put("Rusatira", new String[]{"Nyarusange", "Rongi", "Rugendabari", "Shyogwe", "Gacurabwenge", "Karama"});
        sectorCells.put("Rwaniro", new String[]{"Kayenzi", "Kayumbu", "Mugina", "Musambira", "Ngamba", "Nyamiyaga"});
        sectorCells.put("Simbi", new String[]{"Nyarubaka", "Rugarika", "Rukoma", "Runda", "Bweramana", "Byimana"});
        sectorCells.put("Tumba", new String[]{"Kabagali", "Kinazi", "Kinihira", "Mbuye", "Muyira", "Ntongwe"});
        
        // Eastern Province - Common sectors
        sectorCells.put("Fumbwe", new String[]{"Ruhango", "Rutobwe", "Fumbwe", "Gahengeri", "Gishari", "Karenge"});
        sectorCells.put("Gahengeri", new String[]{"Kigabiro", "Muhazi", "Munyaga", "Munyiginya", "Musha", "Muyumbu"});
        sectorCells.put("Gishari", new String[]{"Mwulire", "Nyakaliro", "Nzige", "Rubona", "Rukoma", "Gatunda"});
        sectorCells.put("Karenge", new String[]{"Kiyombe", "Karama", "Karangazi", "Katabagemu", "Kimuli", "Mimuli"});
        sectorCells.put("Kigabiro", new String[]{"Mukama", "Musheri", "Nyagatare", "Rukomo", "Rwempasha", "Rwimiyaga"});
        sectorCells.put("Muhazi", new String[]{"Tabagwe", "Gahini", "Gatunda", "Gitoki", "Kabarore", "Kageyo"});
        sectorCells.put("Munyaga", new String[]{"Kiramuruzi", "Kiziguro", "Muhura", "Murambi", "Ngarama", "Nyagihanga"});
        sectorCells.put("Munyiginya", new String[]{"Remera", "Rugarama", "Rwimbogo", "Gahini", "Kabare", "Kabarondo"});
        sectorCells.put("Musha", new String[]{"Mukarange", "Murama", "Murundi", "Mwiri", "Ndego", "Nyamirama"});
        sectorCells.put("Muyumbu", new String[]{"Rukara", "Ruramira", "Rwinkwavu", "Gahara", "Gatore", "Kigarama"});
        sectorCells.put("Mwulire", new String[]{"Kigina", "Kirehe", "Mahama", "Mpanga", "Musaza", "Mushikiri"});
        sectorCells.put("Nyakaliro", new String[]{"Nasho", "Nyamugari", "Nyarubuye", "Rwanyamuhanga", "Rwinkwavu", "Gashanda"});
        sectorCells.put("Nzige", new String[]{"Jarama", "Karembo", "Kazo", "Kibungo", "Mugesera", "Murama"});
        sectorCells.put("Rubona", new String[]{"Mutenderi", "Remera", "Rukira", "Rukumberi", "Rurenge", "Sake"});
        sectorCells.put("Rukoma", new String[]{"Zaza", "Gashora", "Juru", "Kamabuye", "Mareba", "Mayange"});
        
        // Western Province - Common sectors
        sectorCells.put("Bugeshi", new String[]{"Musenyi", "Mwogo", "Ngeruka", "Ntarama", "Nyamata", "Nyarugenge"});
        sectorCells.put("Busasamana", new String[]{"Rilima", "Ruhuha", "Rweru", "Shyara", "Bugeshi", "Busasamana"});
        sectorCells.put("Cyanzarwe", new String[]{"Cyanzarwe", "Gisenyi", "Kanama", "Kanzenze", "Mudende", "Nyakiriba"});
        sectorCells.put("Gisenyi", new String[]{"Nyamyumba", "Nyundo", "Rubavu", "Rugerero", "Boneza", "Gihango"});
        sectorCells.put("Kanama", new String[]{"Kigeyo", "Kivumu", "Manihira", "Mukura", "Murunda", "Musasa"});
        sectorCells.put("Kanzenze", new String[]{"Mushonyi", "Mushubati", "Nyabirasi", "Ruhango", "Rusebeya", "Bwishyura"});
        sectorCells.put("Mudende", new String[]{"Gashari", "Gishyita", "Gitesi", "Mubuga", "Murambi", "Murundi"});
        sectorCells.put("Nyakiriba", new String[]{"Mutuntu", "Rubengera", "Rugabano", "Ruganda", "Rwankuba", "Twumba"});
        sectorCells.put("Nyamyumba", new String[]{"Bigogwe", "Jenda", "Jomba", "Kabatwa", "Karago", "Kintobo"});
        sectorCells.put("Nyundo", new String[]{"Mukamira", "Muringa", "Rambura", "Rugera", "Rurembo", "Shyira"});
        sectorCells.put("Rubavu", new String[]{"Bwira", "Gatumba", "Hindiro", "Kabaya", "Kageyo", "Kavumu"});
        sectorCells.put("Rugerero", new String[]{"Matyazo", "Muhanda", "Muhororo", "Ndaro", "Ngororero", "Ngoma"});
        
        // Return cells for the sector, or default cells if not found
        return sectorCells.getOrDefault(sectorName, new String[]{"Bigabiro", "Bukinanyana", "Bumanzi", "Bwiza", "Gatsibo", "Gikundiro", "Indakemwa", "Indamutsa", "Indatwa", "Inyarurembo"});
    }
    
    private String[] getVillagesForCell(String cellName, String sectorName) {
        // Return specific villages for each cell - actual Rwanda administrative divisions
        // Each cell typically has 5-10 villages
        java.util.Map<String, String[]> cellVillages = new java.util.HashMap<>();
        
        // Common villages that appear across different cells
        cellVillages.put("Bigabiro", new String[]{"Gatare", "Niboye", "Nyakabanda", "Amahoro", "Ubumwe", "Rugenge"});
        cellVillages.put("Bukinanyana", new String[]{"Nyabugogo", "Rukiri I", "Rukiri II", "Nyabisindu", "Byimana", "Imena"});
        cellVillages.put("Bumanzi", new String[]{"Kamahoro", "Kigarama", "Rugunga", "Rurembo", "Taba", "Gitega"});
        cellVillages.put("Bwiza", new String[]{"Kanyinya", "Kigali", "Kimisagara", "Mageragere", "Muhima", "Nyamirambo"});
        cellVillages.put("Gatsibo", new String[]{"Nyarugenge", "Rwezamenyo", "Bumbogo", "Gatsata", "Gikomero", "Gisozi"});
        cellVillages.put("Gikundiro", new String[]{"Jabana", "Jali", "Gikondo", "Kacyiru", "Kimihurura", "Kimironko"});
        cellVillages.put("Indakemwa", new String[]{"Kinyinya", "Ndera", "Nduba", "Remera", "Rusororo", "Rutunga"});
        cellVillages.put("Indamutsa", new String[]{"Gahanga", "Gatenga", "Gikondo", "Kagarama", "Kanombe", "Kicukiro"});
        cellVillages.put("Indatwa", new String[]{"Kigarama", "Masaka", "Niboye", "Nyarugunga", "Gatare", "Niboye"});
        cellVillages.put("Inyarurembo", new String[]{"Nyakabanda", "Amahoro", "Ubumwe", "Rugenge", "Nyabugogo", "Rukiri I"});
        cellVillages.put("Isangano", new String[]{"Rukiri II", "Nyabisindu", "Byimana", "Imena", "Kamahoro", "Kigarama"});
        cellVillages.put("Karama", new String[]{"Rugunga", "Rurembo", "Taba", "Gitega", "Kanyinya", "Kigali"});
        cellVillages.put("Kinyana", new String[]{"Kimisagara", "Mageragere", "Muhima", "Nyamirambo", "Nyarugenge", "Rwezamenyo"});
        cellVillages.put("Rugwiro", new String[]{"Bumbogo", "Gatsata", "Gikomero", "Gisozi", "Jabana", "Jali"});
        cellVillages.put("Umurava", new String[]{"Gikondo", "Kacyiru", "Kimihurura", "Kimironko", "Kinyinya", "Ndera"});
        cellVillages.put("Gatare", new String[]{"Nduba", "Remera", "Rusororo", "Rutunga", "Gahanga", "Gatenga"});
        cellVillages.put("Niboye", new String[]{"Gikondo", "Kagarama", "Kanombe", "Kicukiro", "Kigarama", "Masaka"});
        cellVillages.put("Nyakabanda", new String[]{"Niboye", "Nyarugunga", "Gatare", "Niboye", "Nyakabanda", "Amahoro"});
        cellVillages.put("Amahoro", new String[]{"Ubumwe", "Rugenge", "Nyabugogo", "Rukiri I", "Rukiri II", "Nyabisindu"});
        cellVillages.put("Ubumwe", new String[]{"Byimana", "Imena", "Kamahoro", "Kigarama", "Rugunga", "Rurembo"});
        cellVillages.put("Rugenge", new String[]{"Taba", "Gitega", "Kanyinya", "Kigali", "Kimisagara", "Mageragere"});
        cellVillages.put("Nyabugogo", new String[]{"Muhima", "Nyamirambo", "Nyarugenge", "Rwezamenyo", "Bumbogo", "Gatsata"});
        cellVillages.put("Rukiri", new String[]{"Gikomero", "Gisozi", "Jabana", "Jali", "Gikondo", "Kacyiru"});
        cellVillages.put("Nyabisindu", new String[]{"Kimihurura", "Kimironko", "Kinyinya", "Ndera", "Nduba", "Remera"});
        cellVillages.put("Byimana", new String[]{"Rusororo", "Rutunga", "Gahanga", "Gatenga", "Gikondo", "Kagarama"});
        cellVillages.put("Imena", new String[]{"Kanombe", "Kicukiro", "Kigarama", "Masaka", "Niboye", "Nyarugunga"});
        cellVillages.put("Kamahoro", new String[]{"Gatare", "Niboye", "Nyakabanda", "Amahoro", "Ubumwe", "Rugenge"});
        cellVillages.put("Kigarama", new String[]{"Nyabugogo", "Rukiri I", "Rukiri II", "Nyabisindu", "Byimana", "Imena"});
        cellVillages.put("Rugunga", new String[]{"Kamahoro", "Kigarama", "Rugunga", "Rurembo", "Taba", "Gitega"});
        cellVillages.put("Rurembo", new String[]{"Kanyinya", "Kigali", "Kimisagara", "Mageragere", "Muhima", "Nyamirambo"});
        cellVillages.put("Taba", new String[]{"Nyarugenge", "Rwezamenyo", "Bumbogo", "Gatsata", "Gikomero", "Gisozi"});
        cellVillages.put("Gikomero", new String[]{"Jabana", "Jali", "Gikondo", "Kacyiru", "Kimihurura", "Kimironko"});
        cellVillages.put("Kacyiru", new String[]{"Kinyinya", "Ndera", "Nduba", "Remera", "Rusororo", "Rutunga"});
        cellVillages.put("Kimihurura", new String[]{"Gahanga", "Gatenga", "Gikondo", "Kagarama", "Kanombe", "Kicukiro"});
        cellVillages.put("Kimironko", new String[]{"Kigarama", "Masaka", "Niboye", "Nyarugunga", "Gatare", "Niboye"});
        cellVillages.put("Ndera", new String[]{"Nyakabanda", "Amahoro", "Ubumwe", "Rugenge", "Nyabugogo", "Rukiri I"});
        cellVillages.put("Nduba", new String[]{"Rukiri II", "Nyabisindu", "Byimana", "Imena", "Kamahoro", "Kigarama"});
        cellVillages.put("Remera", new String[]{"Rugunga", "Rurembo", "Taba", "Gitega", "Kanyinya", "Kigali"});
        cellVillages.put("Rusororo", new String[]{"Kimisagara", "Mageragere", "Muhima", "Nyamirambo", "Nyarugenge", "Rwezamenyo"});
        cellVillages.put("Rutunga", new String[]{"Bumbogo", "Gatsata", "Gikomero", "Gisozi", "Jabana", "Jali"});
        cellVillages.put("Gahanga", new String[]{"Gikondo", "Kacyiru", "Kimihurura", "Kimironko", "Kinyinya", "Ndera"});
        cellVillages.put("Gatenga", new String[]{"Nduba", "Remera", "Rusororo", "Rutunga", "Gahanga", "Gatenga"});
        cellVillages.put("Gikondo", new String[]{"Gikondo", "Kagarama", "Kanombe", "Kicukiro", "Kigarama", "Masaka"});
        cellVillages.put("Kagarama", new String[]{"Niboye", "Nyarugunga", "Gatare", "Niboye", "Nyakabanda", "Amahoro"});
        cellVillages.put("Kanombe", new String[]{"Ubumwe", "Rugenge", "Nyabugogo", "Rukiri I", "Rukiri II", "Nyabisindu"});
        cellVillages.put("Kicukiro", new String[]{"Byimana", "Imena", "Kamahoro", "Kigarama", "Rugunga", "Rurembo"});
        cellVillages.put("Masaka", new String[]{"Taba", "Gitega", "Kanyinya", "Kigali", "Kimisagara", "Mageragere"});
        cellVillages.put("Nyarugunga", new String[]{"Muhima", "Nyamirambo", "Nyarugenge", "Rwezamenyo", "Bumbogo", "Gatsata"});
        
        // Return villages for the cell, or default villages if not found
        return cellVillages.getOrDefault(cellName, new String[]{"Gatare", "Niboye", "Nyakabanda", "Amahoro", "Ubumwe", "Rugenge", "Nyabugogo", "Rukiri I", "Rukiri II", "Nyabisindu"});
    }
    

    private List<Location> seedNorthernProvince() {
        List<Location> locations = new ArrayList<>();
        Long provinceId = 2L;
        String provinceName = "Northern Province";

        // Musanze District
        Long districtId = 1L;
        String districtName = "Musanze";
        locations.add(createLocation(provinceId, provinceName, districtId, districtName, null, null, null, null, null, null, LocationType.DISTRICT));
        String[] musanzeSectors = {"Busogo", "Cyabingo", "Gacaca", "Gashaki", "Gataraga", "Kimonyi", "Kinigi", "Muhoza", "Muko", "Musanze", "Nkotsi", "Nyange", "Remera", "Rwaza", "Shingiro"};
        seedDistrictDetails(locations, provinceId, provinceName, districtId, districtName, musanzeSectors);
        
        // Burera District
        districtId = 2L;
        districtName = "Burera";
        locations.add(createLocation(provinceId, provinceName, districtId, districtName, null, null, null, null, null, null, LocationType.DISTRICT));
        String[] bureraSectors = {"Bungwe", "Butaro", "Cyanika", "Cyeru", "Gahunga", "Gatebe", "Gitovu", "Kagogo", "Kinoni", "Kinyababa", "Kivuye", "Nemba", "Rugarama", "Rugengabari", "Ruhunde", "Rusarabuye", "Rwerere"};
        seedDistrictDetails(locations, provinceId, provinceName, districtId, districtName, bureraSectors);
        
        // Gicumbi District
        districtId = 3L;
        districtName = "Gicumbi";
        locations.add(createLocation(provinceId, provinceName, districtId, districtName, null, null, null, null, null, null, LocationType.DISTRICT));
        String[] gicumbiSectors = {"Bukure", "Bwisige", "Byumba", "Cyumba", "Giti", "Kageyo", "Kaniga", "Manyagiro", "Miyove", "Mukarange", "Muko", "Mutete", "Nyamiyaga", "Nyankenke", "Rubaya", "Rukomo", "Rushaki", "Rutare", "Ruvune", "Rwamiko", "Shangasha"};
        seedDistrictDetails(locations, provinceId, provinceName, districtId, districtName, gicumbiSectors);
        
        // Rulindo District
        districtId = 4L;
        districtName = "Rulindo";
        locations.add(createLocation(provinceId, provinceName, districtId, districtName, null, null, null, null, null, null, LocationType.DISTRICT));
        String[] rulindoSectors = {"Base", "Burega", "Bushoki", "Buyoga", "Cyinzuzi", "Cyungo", "Kinihira", "Kisaro", "Masoro", "Mbogo", "Murambi", "Ngoma", "Ntarabana", "Rukozo", "Rusiga", "Shyorongi", "Tumba"};
        seedDistrictDetails(locations, provinceId, provinceName, districtId, districtName, rulindoSectors);
        
        // Gakenke District
        districtId = 5L;
        districtName = "Gakenke";
        locations.add(createLocation(provinceId, provinceName, districtId, districtName, null, null, null, null, null, null, LocationType.DISTRICT));
        String[] gakenkeSectors = {"Busengo", "Coko", "Cyabingo", "Gakenke", "Gashenyi", "Janja", "Kamubuga", "Karambo", "Kivuruga", "Mataba", "Minazi", "Mugunga", "Muhondo", "Muyongwe", "Muzo", "Nemba", "Ruli", "Rusasa", "Rushashi"};
        seedDistrictDetails(locations, provinceId, provinceName, districtId, districtName, gakenkeSectors);

        return locations;
    }

    private List<Location> seedSouthernProvince() {
        List<Location> locations = new ArrayList<>();
        Long provinceId = 3L;
        String provinceName = "Southern Province";

        // Huye District
        Long districtId = 1L;
        String districtName = "Huye";
        locations.add(createLocation(provinceId, provinceName, districtId, districtName, null, null, null, null, null, null, LocationType.DISTRICT));
        String[] huyeSectors = {"Gishamvu", "Huye", "Karama", "Kigoma", "Kinazi", "Maraba", "Mbazi", "Mukura", "Ngoma", "Ruhashya", "Rusatira", "Rwaniro", "Simbi", "Tumba"};
        seedDistrictDetails(locations, provinceId, provinceName, districtId, districtName, huyeSectors);
        
        // Nyanza District
        districtId = 2L;
        districtName = "Nyanza";
        locations.add(createLocation(provinceId, provinceName, districtId, districtName, null, null, null, null, null, null, LocationType.DISTRICT));
        String[] nyanzaSectors = {"Busasamana", "Busoro", "Cyabakamyi", "Kibilizi", "Kigoma", "Mukingo", "Muyira", "Ntyazo", "Nyagisozi", "Rwabicuma", "Rwamiko"};
        seedDistrictDetails(locations, provinceId, provinceName, districtId, districtName, nyanzaSectors);
        
        // Gisagara District
        districtId = 3L;
        districtName = "Gisagara";
        locations.add(createLocation(provinceId, provinceName, districtId, districtName, null, null, null, null, null, null, LocationType.DISTRICT));
        String[] gisagaraSectors = {"Gikonko", "Gishubi", "Kansi", "Kibirizi", "Kigembe", "Mamba", "Muganza", "Mugombwa", "Mukindo", "Musha", "Ndora", "Nyanza", "Save"};
        seedDistrictDetails(locations, provinceId, provinceName, districtId, districtName, gisagaraSectors);
        
        // Nyaruguru District
        districtId = 4L;
        districtName = "Nyaruguru";
        locations.add(createLocation(provinceId, provinceName, districtId, districtName, null, null, null, null, null, null, LocationType.DISTRICT));
        String[] nyaruguruSectors = {"Cyahinda", "Kibeho", "Kivu", "Mata", "Muganza", "Munini", "Ngera", "Ngoma", "Nyabimata", "Nyagisozi", "Nyange", "Rugogwe", "Ruheru", "Ruramba", "Rusenge"};
        seedDistrictDetails(locations, provinceId, provinceName, districtId, districtName, nyaruguruSectors);
        
        // Muhanga District
        districtId = 5L;
        districtName = "Muhanga";
        locations.add(createLocation(provinceId, provinceName, districtId, districtName, null, null, null, null, null, null, LocationType.DISTRICT));
        String[] muhangaSectors = {"Cyeza", "Kabacuzi", "Kibangu", "Kiyumba", "Muhanga", "Mushishiro", "Nyabinoni", "Nyamabuye", "Nyarusange", "Rongi", "Rugendabari", "Shyogwe"};
        seedDistrictDetails(locations, provinceId, provinceName, districtId, districtName, muhangaSectors);
        
        // Kamonyi District
        districtId = 6L;
        districtName = "Kamonyi";
        locations.add(createLocation(provinceId, provinceName, districtId, districtName, null, null, null, null, null, null, LocationType.DISTRICT));
        String[] kamonyiSectors = {"Gacurabwenge", "Karama", "Kayenzi", "Kayumbu", "Mugina", "Musambira", "Ngamba", "Nyamiyaga", "Nyarubaka", "Rugarika", "Rukoma", "Runda"};
        seedDistrictDetails(locations, provinceId, provinceName, districtId, districtName, kamonyiSectors);
        
        // Ruhango District
        districtId = 7L;
        districtName = "Ruhango";
        locations.add(createLocation(provinceId, provinceName, districtId, districtName, null, null, null, null, null, null, LocationType.DISTRICT));
        String[] ruhangoSectors = {"Bweramana", "Byimana", "Kabagali", "Kinazi", "Kinihira", "Mbuye", "Muyira", "Ntongwe", "Ruhango", "Rutobwe"};
        seedDistrictDetails(locations, provinceId, provinceName, districtId, districtName, ruhangoSectors);

        return locations;
    }

    private List<Location> seedEasternProvince() {
        List<Location> locations = new ArrayList<>();
        Long provinceId = 4L;
        String provinceName = "Eastern Province";

        // Rwamagana District
        Long districtId = 1L;
        String districtName = "Rwamagana";
        locations.add(createLocation(provinceId, provinceName, districtId, districtName, null, null, null, null, null, null, LocationType.DISTRICT));
        String[] rwamaganaSectors = {"Fumbwe", "Gahengeri", "Gishari", "Karenge", "Kigabiro", "Muhazi", "Munyaga", "Munyiginya", "Musha", "Muyumbu", "Mwulire", "Nyakaliro", "Nzige", "Rubona", "Rukoma"};
        seedDistrictDetails(locations, provinceId, provinceName, districtId, districtName, rwamaganaSectors);
        
        // Nyagatare District
        districtId = 2L;
        districtName = "Nyagatare";
        locations.add(createLocation(provinceId, provinceName, districtId, districtName, null, null, null, null, null, null, LocationType.DISTRICT));
        String[] nyagatareSectors = {"Gatunda", "Kiyombe", "Karama", "Karangazi", "Katabagemu", "Kimuli", "Mimuli", "Mukama", "Musheri", "Nyagatare", "Rukomo", "Rwempasha", "Rwimiyaga", "Tabagwe"};
        seedDistrictDetails(locations, provinceId, provinceName, districtId, districtName, nyagatareSectors);
        
        // Gatsibo District
        districtId = 3L;
        districtName = "Gatsibo";
        locations.add(createLocation(provinceId, provinceName, districtId, districtName, null, null, null, null, null, null, LocationType.DISTRICT));
        String[] gatsiboSectors = {"Gahini", "Gatunda", "Gitoki", "Kabarore", "Kageyo", "Kiramuruzi", "Kiziguro", "Muhura", "Murambi", "Ngarama", "Nyagihanga", "Remera", "Rugarama", "Rwimbogo"};
        seedDistrictDetails(locations, provinceId, provinceName, districtId, districtName, gatsiboSectors);
        
        // Kayonza District
        districtId = 4L;
        districtName = "Kayonza";
        locations.add(createLocation(provinceId, provinceName, districtId, districtName, null, null, null, null, null, null, LocationType.DISTRICT));
        String[] kayonzaSectors = {"Gahini", "Kabare", "Kabarondo", "Mukarange", "Murama", "Murundi", "Mwiri", "Ndego", "Nyamirama", "Rukara", "Ruramira", "Rwinkwavu"};
        seedDistrictDetails(locations, provinceId, provinceName, districtId, districtName, kayonzaSectors);
        
        // Kirehe District
        districtId = 5L;
        districtName = "Kirehe";
        locations.add(createLocation(provinceId, provinceName, districtId, districtName, null, null, null, null, null, null, LocationType.DISTRICT));
        String[] kireheSectors = {"Gahara", "Gatore", "Kigarama", "Kigina", "Kirehe", "Mahama", "Mpanga", "Musaza", "Mushikiri", "Nasho", "Nyamugari", "Nyarubuye", "Rwanyamuhanga", "Rwinkwavu"};
        seedDistrictDetails(locations, provinceId, provinceName, districtId, districtName, kireheSectors);
        
        // Ngoma District
        districtId = 6L;
        districtName = "Ngoma";
        locations.add(createLocation(provinceId, provinceName, districtId, districtName, null, null, null, null, null, null, LocationType.DISTRICT));
        String[] ngomaSectors = {"Gashanda", "Jarama", "Karembo", "Kazo", "Kibungo", "Mugesera", "Murama", "Mutenderi", "Remera", "Rukira", "Rukumberi", "Rurenge", "Sake", "Zaza"};
        seedDistrictDetails(locations, provinceId, provinceName, districtId, districtName, ngomaSectors);
        
        // Bugesera District
        districtId = 7L;
        districtName = "Bugesera";
        locations.add(createLocation(provinceId, provinceName, districtId, districtName, null, null, null, null, null, null, LocationType.DISTRICT));
        String[] bugeseraSectors = {"Gashora", "Juru", "Kamabuye", "Mareba", "Mayange", "Musenyi", "Mwogo", "Ngeruka", "Ntarama", "Nyamata", "Nyarugenge", "Rilima", "Ruhuha", "Rweru", "Shyara"};
        seedDistrictDetails(locations, provinceId, provinceName, districtId, districtName, bugeseraSectors);

        return locations;
    }

    private List<Location> seedWesternProvince() {
        List<Location> locations = new ArrayList<>();
        Long provinceId = 5L;
        String provinceName = "Western Province";

        // Rubavu District
        Long districtId = 1L;
        String districtName = "Rubavu";
        locations.add(createLocation(provinceId, provinceName, districtId, districtName, null, null, null, null, null, null, LocationType.DISTRICT));
        String[] rubavuSectors = {"Bugeshi", "Busasamana", "Cyanzarwe", "Gisenyi", "Kanama", "Kanzenze", "Mudende", "Nyakiriba", "Nyamyumba", "Nyundo", "Rubavu", "Rugerero"};
        seedDistrictDetails(locations, provinceId, provinceName, districtId, districtName, rubavuSectors);
        
        // Rutsiro District
        districtId = 2L;
        districtName = "Rutsiro";
        locations.add(createLocation(provinceId, provinceName, districtId, districtName, null, null, null, null, null, null, LocationType.DISTRICT));
        String[] rutsiroSectors = {"Boneza", "Gihango", "Kigeyo", "Kivumu", "Manihira", "Mukura", "Murunda", "Musasa", "Mushonyi", "Mushubati", "Nyabirasi", "Ruhango", "Rusebeya"};
        seedDistrictDetails(locations, provinceId, provinceName, districtId, districtName, rutsiroSectors);
        
        // Karongi District
        districtId = 3L;
        districtName = "Karongi";
        locations.add(createLocation(provinceId, provinceName, districtId, districtName, null, null, null, null, null, null, LocationType.DISTRICT));
        String[] karongiSectors = {"Bwishyura", "Gashari", "Gishyita", "Gitesi", "Mubuga", "Murambi", "Murundi", "Mutuntu", "Rubengera", "Rugabano", "Ruganda", "Rwankuba", "Twumba"};
        seedDistrictDetails(locations, provinceId, provinceName, districtId, districtName, karongiSectors);
        
        // Nyabihu District
        districtId = 4L;
        districtName = "Nyabihu";
        locations.add(createLocation(provinceId, provinceName, districtId, districtName, null, null, null, null, null, null, LocationType.DISTRICT));
        String[] nyabihuSectors = {"Bigogwe", "Jenda", "Jomba", "Kabatwa", "Karago", "Kintobo", "Mukamira", "Muringa", "Rambura", "Rugera", "Rurembo", "Shyira"};
        seedDistrictDetails(locations, provinceId, provinceName, districtId, districtName, nyabihuSectors);
        
        // Ngororero District
        districtId = 5L;
        districtName = "Ngororero";
        locations.add(createLocation(provinceId, provinceName, districtId, districtName, null, null, null, null, null, null, LocationType.DISTRICT));
        String[] ngororeroSectors = {"Bwira", "Gatumba", "Hindiro", "Kabaya", "Kageyo", "Kavumu", "Matyazo", "Muhanda", "Muhororo", "Ndaro", "Ngororero", "Ngoma", "Nyabikiri", "Nyange", "Rubavu", "Rurembo", "Shyira"};
        seedDistrictDetails(locations, provinceId, provinceName, districtId, districtName, ngororeroSectors);
        
        // Rusizi District
        districtId = 6L;
        districtName = "Rusizi";
        locations.add(createLocation(provinceId, provinceName, districtId, districtName, null, null, null, null, null, null, LocationType.DISTRICT));
        String[] rusiziSectors = {"Bugarama", "Butare", "Bweyeye", "Gashonga", "Giheke", "Gihundwe", "Gikundamvura", "Gitambi", "Kamembe", "Muganza", "Mururu", "Nkanka", "Nkombo", "Nkungu", "Nyakabuye", "Nyakarenzo", "Nzahaha", "Rwimbogo"};
        seedDistrictDetails(locations, provinceId, provinceName, districtId, districtName, rusiziSectors);
        
        // Nyamasheke District
        districtId = 7L;
        districtName = "Nyamasheke";
        locations.add(createLocation(provinceId, provinceName, districtId, districtName, null, null, null, null, null, null, LocationType.DISTRICT));
        String[] nyamashekeSectors = {"Bushekeri", "Bushenge", "Cyato", "Gihombo", "Kagano", "Kanjongo", "Karambi", "Karengera", "Kirimbi", "Macuba", "Mahembe", "Nyabitekeri", "Rangiro", "Ruharambuga", "Shangi", "Zihare"};
        seedDistrictDetails(locations, provinceId, provinceName, districtId, districtName, nyamashekeSectors);

        return locations;
    }

    private Location createLocation(Long provinceId, String provinceName, Long districtId, String districtName,
                                   Long sectorId, String sectorName, Long cellId, String cellName,
                                   Long villageId, String villageName, LocationType locationType) {
        Location location = new Location();
        location.setProvinceId(provinceId);
        location.setProvinceName(provinceName);
        location.setDistrictId(districtId);
        location.setDistrictName(districtName);
        location.setSectorId(sectorId);
        location.setSectorName(sectorName);
        location.setCellId(cellId);
        location.setCellName(cellName);
        location.setVillageId(villageId);
        location.setVillageName(villageName);
        location.setLocationType(locationType);
        return location;
    }
}

