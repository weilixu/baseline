package baseline.idfdata;

import baseline.generator.EplusObject;
/**
 * This class represents the Asset Score thermal zone (conditioned zones) naming convention.
 * 
 * Block_ZoneType_ZoneIdentififier_ZoneHVAC
 * This class will process the zone name in the above format and save it into 4 pieces of information
 * 
 * @author Weili
 *
 */
public class DesignBuilderThermalZone implements ThermalZone{
    private final static int blockIndex = 0;
    private final static int zoneTypeIndex = 0;
    private final static int zoneIdentificationIndex = 1;
    //private final static int zoneHVACIndex = 2;
    
    private final static String seperator = "X";
    private final static String blockSeperator = ":";
    
    private String block;
    private String zoneType;
    private String floor;
    private String zoneIdentification;
    private String hvac;
    private String originalZoneName;
    private EplusObject mechanicalVentilationRequirement;
    
    private Double coolingLoad;
    private Double heatingLoad;    
    private Double coolingAirFlow;
    private Double heatingAirFlow;
    private Double minimumVentilation;
    
    
    public DesignBuilderThermalZone(String zoneName){
	originalZoneName = zoneName;
	String[] zoneBlockLevel = zoneName.split(blockSeperator);
	block = zoneBlockLevel[blockIndex];
	floor = block;
	String[] zoneCharacters = zoneBlockLevel[1].split(seperator);
	zoneType = zoneCharacters[zoneTypeIndex];
	zoneIdentification = zoneCharacters[zoneIdentificationIndex];
    }
    

    @Override
    public String getFullName() {
	return originalZoneName;
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
    public void setCoolingLoad(Double load) {
	this.coolingLoad = load;
	
    }

    @Override
    public void setHeaingLoad(Double load) {
	this.heatingLoad = load;
	
    }

    @Override
    public void setMechanicalVentilation(Double vent) {
	minimumVentilation = vent;
	
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
    public void setOAVentilation(EplusObject OAObject) {
	mechanicalVentilationRequirement = OAObject;
	
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
    public Double getCoolingLoad() {
	return coolingLoad;
    }

    @Override
    public Double getHeatingLoad() {
	return heatingLoad;
    }

    @Override
    public Double getMinimumVentilation() {
	return minimumVentilation;
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
    public EplusObject getOutdoorAirObject() {
	return mechanicalVentilationRequirement;
    }

}
