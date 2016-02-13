package baseline.idfdata.thermalzone;

import baseline.idfdata.EplusObject;

/**
 * This class represents the Asset score thermal zone (conditioned zones) naming convention.
 * 
 * BLOCK_ZONETYPE_FLOOR_ZONEIDENTIFIER
 * This class will process the zone name in the above format and save it into 4 pieces of information
 * 
 * @author Weili
 *
 */
public class AssetScoreThermalZone implements ThermalZone{
    private final static int blockIndex = 0;
    private final static int zoneTypeIndex = 1;
    private final static int floorIndex = 2;
    private final static int zoneIdentificationIndex = 3;
    
    private final static String seperator = "_";
    
    private String block;
    private String zoneType;
    private String floor;
    private String zoneIdentification;
    private String hvac;
    private String vent;
    private String originalZoneName;
    private EplusObject mechanicalVentilationRequirement;
    
    /*
     * Zone HVAC parameters
     */
    private Double coolingLoad;
    private Double heatingLoad;    
    private Double coolingAirFlow;
    private Double heatingAirFlow;
    private Double minimumVentilation;
    
    /*
     * Zone parameters
     */
    private Double area;
    private Double grossWallArea;
    private Double occupants;
    private Double lpd;
    private Double epd;
    
    
    public AssetScoreThermalZone(String zoneName){
	originalZoneName = zoneName;
	String[] zoneCharacters = zoneName.split(seperator);
	block = zoneCharacters[blockIndex];
	zoneType = zoneCharacters[zoneTypeIndex];
	floor = zoneCharacters[floorIndex];
	zoneIdentification = zoneCharacters[zoneIdentificationIndex];
    }

    @Override
    public void setBlock(String block) {
	this.block = block;
    }

    @Override
    public void setFloor(String floor) {
	this.floor = floor;
    }

    @Override
    public void setZoneType(String zoneType) {
	this.zoneType = zoneType;
    }

    @Override
    public void setZoneIdentification(String zoneIdentification) {
	this.zoneIdentification = zoneIdentification;
    }

    @Override
    public void setZoneCoolHeat(String hvacZone) {
	this.hvac = hvacZone;
	
    }

    @Override
    public String getBlock() {
	return block;
    }

    @Override
    public String getFloor() {
	return floor;
    }

    @Override
    public String getZoneType() {
	return zoneType;
    }

    @Override
    public String getZoneIdentification() {
	return zoneIdentification;
    }

    @Override
    public String getZoneCoolHeat() {
	return hvac;
    }

    @Override
    public String getFullName() {
	return originalZoneName;
    }

    @Override
    public void setCoolingLoad(Double load) {
	coolingLoad = load;
    }

    @Override
    public void setHeaingLoad(Double load) {
	heatingLoad = load;
    }
    
    @Override
    public void setMechanicalVentilation(Double vent) {
	minimumVentilation = vent;
    }

    @Override
    public Double getCoolingLoad() {
	return coolingLoad;
    }

    @Override
    public Double getHeatingLoad() {
	return heatingLoad;
    }

    @Override
    public EplusObject getOutdoorAirObject() {
	if(mechanicalVentilationRequirement==null){
	    return null;
	}
	return mechanicalVentilationRequirement;
    }

    @Override
    public void setOAVentilation(EplusObject OAObject) {
	mechanicalVentilationRequirement = OAObject;
    }

    @Override
    public void setCoolingAirFlow(Double airflow) {
	coolingAirFlow = airflow;
    }

    @Override
    public void setHeatingAirFlow(Double airflow) {
	heatingAirFlow = airflow;
    }

    @Override
    public Double getCoolingAirFlow() {
	return coolingAirFlow;
    }

    @Override
    public Double getHeatingAirFlow() {
	return heatingAirFlow;
    }

    @Override
    public Double getMinimumVentilation() {
	return minimumVentilation;
    }

    @Override
    public Double getZoneArea() {
	return area;
    }


    @Override
    public Double getZoneGrossWallArea() {
	return grossWallArea;
    }


    @Override
    public Double getZoneOccupants() {
	return occupants;
    }


    @Override
    public Double getZoneLPD() {
	return lpd;
    }


    @Override
    public Double getZoneEPD() {
	return epd;
    }

    @Override
    public void setZoneArea(Double area) {
	this.area = area;
    }


    @Override
    public void setZoneGrossWallArea(Double area) {
	grossWallArea = area;
    }


    @Override
    public void setZoneOccupants(Double occupants) {
	this.occupants = occupants;
	
    }


    @Override
    public void setZoneLPD(Double lpd) {
	this.lpd = lpd;
	
    }


    @Override
    public void setZoneEPD(Double epd) {
	this.epd = epd;
    }

    @Override
    public void setZoneVentilate(String zoneVent) {
	vent = zoneVent;
    }

    @Override
    public String getZoneVent() {
	return vent;
    }
}
