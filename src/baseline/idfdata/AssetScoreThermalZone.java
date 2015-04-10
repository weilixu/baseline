package baseline.idfdata;

import baseline.generator.EplusObject;
import baseline.generator.KeyValuePair;

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
    
    private String block;
    private String zoneType;
    private String floor;
    private String zoneIdentification;
    private String hvac;
    private String originalZoneName;
    private EplusObject mechanicalVentilationRequirement;
    
    private Double coolingLoad;
    private Double heatingLoad;
    
    
    public AssetScoreThermalZone(String zoneName){
	originalZoneName = zoneName;
	String[] zoneCharacters = zoneName.split("_");
	block = zoneCharacters[blockIndex];
	zoneType = zoneCharacters[zoneTypeIndex];
	floor = zoneCharacters[floorIndex];
	zoneIdentification = zoneCharacters[zoneIdentificationIndex];
	//initiate the mechanical ventional requirements for the thermal zone
	mechanicalVentilationRequirement = new EplusObject("DesignSpecification:OutdoorAir",zoneName);
	mechanicalVentilationRequirement.addField(new KeyValuePair("Name",zoneName+" OutdoorAir"));//add the name of the eplus object
	
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
    public void setHVACZone(String hvacZone) {
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
    public String getHVACZone() {
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
    public Double getCoolingLoad() {
	return coolingLoad;
    }

    @Override
    public Double getHeatingLoad() {
	return heatingLoad;
    }

    @Override
    public void setVentilationMethod(String method) {
	mechanicalVentilationRequirement.addField(new KeyValuePair("Outdoor Air Method",method));
    }

    @Override
    public void setVentilationRate(String method, String rate) {
	mechanicalVentilationRequirement.addField(new KeyValuePair(method,rate));
    }

    @Override
    public EplusObject getOutdoorAirObject() {
	return mechanicalVentilationRequirement;
    }
}
