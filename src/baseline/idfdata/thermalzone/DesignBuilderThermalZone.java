package baseline.idfdata.thermalzone;

import baseline.idfdata.EplusObject;

/**
 * This class represents the Asset Score thermal zone (conditioned zones) naming
 * convention.
 * 
 * Block_ZoneType_ZoneIdentififier_ZoneHVAC This class will process the zone
 * name in the above format and save it into 4 pieces of information
 * 
 * @author Weili
 *
 */
public class DesignBuilderThermalZone implements ThermalZone {
    private final static int blockIndex = 0;
    private final static int zoneTypeIndex = 1;
    private final static int zoneIdentificationIndex = 2;
    private final static int zoneHVACOrVentIndex = 3;
    private final static int zoneHVACIndex = 4;
    // private final static int zoneHVACIndex = 3;

    private final static String seperator = "%";
    private final static String defaultSeperator = ":";

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

    public DesignBuilderThermalZone(String zoneName) {
	originalZoneName = zoneName;
	String[] zoneCharacters = originalZoneName.split(seperator);
	// ventilation is in the same system with heating and cooling
	if (zoneCharacters.length == 1) {
	    String[] defaultSetting = originalZoneName.split(defaultSeperator);
	    if (defaultSetting.length == 1) {
		block = zoneCharacters[blockIndex];
		floor = null;
		zoneType = null;
		zoneIdentification = null;
		hvac = null;
		vent = null;
	    } else if(defaultSetting.length > 1) {
		// in this condition, assume the zone name can only be seperated
		// into two: block and rest
		block = defaultSetting[blockIndex];
		floor = null;
		zoneType = defaultSetting[zoneTypeIndex];
		zoneIdentification = null;
		hvac = null;
		vent = null;
	    }
	} else if (zoneCharacters.length == 2) {
	    block = zoneCharacters[blockIndex];
	    floor = null;
	    zoneType = zoneCharacters[zoneTypeIndex];
	    zoneIdentification = null;
	    hvac = null;
	    vent = null;
	} else if (zoneCharacters.length == 3) {
	    block = zoneCharacters[blockIndex];
	    floor = null;
	    zoneType = zoneCharacters[zoneTypeIndex];
	    zoneIdentification = zoneCharacters[zoneIdentificationIndex];
	    hvac = null;
	    vent = null;
	} else if (zoneCharacters.length == 4) {
	    block = zoneCharacters[blockIndex];
	    floor = null;
	    zoneType = zoneCharacters[zoneTypeIndex];
	    zoneIdentification = zoneCharacters[zoneIdentificationIndex];
	    hvac = zoneCharacters[zoneHVACOrVentIndex];
	    vent = null;
	} else if (zoneCharacters.length == 5) {
	    block = zoneCharacters[blockIndex];
	    floor = null;
	    zoneType = zoneCharacters[zoneTypeIndex];
	    zoneIdentification = zoneCharacters[zoneIdentificationIndex];
	    vent = zoneCharacters[zoneHVACOrVentIndex];
	    hvac = zoneCharacters[zoneHVACIndex];
	} else {
	    // plenum zone condition
	    block = null;
	    zoneType = null;
	    floor = null;
	    zoneIdentification = null;
	    vent = null;
	    hvac = null;
	}
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
    public void setZoneCoolHeat(String zonehvac) {
	this.hvac = zonehvac;

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
    public String getZoneCoolHeat() {
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
