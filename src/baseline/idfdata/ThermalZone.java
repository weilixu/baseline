package baseline.idfdata;

/**
 * a generic thermal zone interface that represents different types of thermal zone representations defined in EnergyPlus
 * 
 * @author Weili
 *
 */
public interface ThermalZone {
    
    /**
     * set the block of the thermal zone
     * @param block
     */
    public void setBlock(String block);
    
    /**
     * set the floor of the thermal zone
     * @param block
     */
    public void setFloor(String floor);
    
    /**
     * set the zone type of the thermal zone
     * @param block
     */
    public void setZoneType(String zoneType);
    
    /**
     * set the zone identification of the thermal zone
     * @param block
     */
    public void setZoneIdentification(String zoneIdentification);
    
    /**
     * set the hvac system of the thermal zone
     * @param block
     */
    public void setHVACZone(String hvacZone);
    
    /**
     * get the block of the thermal zone
     * @param block
     */
    public String getBlock();
    
    /**
     * get the floor of the thermal zone
     * @param block
     */
    public String getFloor();
    
    /**
     * get the zone type of the thermal zone
     * @param block
     */
    public String getZoneType();
        
    /**
     * get the zone identification of the thermal zone
     * @param block
     */
    public String getZoneIdentification();
    
    /**
     * get the hvac system of the thermal zone
     * @param block
     */
    public String getHVACZone();
}
