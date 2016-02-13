package baseline.idfdata.building;

public interface BuildingLight {
    /**
     * retrieve building type of the model.
     * This can be used for determine lighting power density using
     * building area method 
     * The building area method is recommended only if the zone type
     * information is missing in this practice
     * @return building types subject to TABLE 9.5.1 ASHRAE 90.1 2010
     */
    public String getBuildingType();
    
    /**
     * retrieve zone type of a particular zone
     * This can be used for determine lighting power density using
     * space-by-space method
     * The space-by-space method is preferred method when specifying
     * lighting power density. If the zone type information is missing,
     * this method will return null.
     * @return zone type subject to TABLE 9.6.1 ASHRAE 90.1 2010 or NULL, if the
     * 		information is missing
     */
    public String getZoneType(String zoneName);
    
    /**
     * set a specific zone's lpd. The lpd value should always in W/m2 unit.
     * and this has to be mapped to either ASHRAE 90.1 Table 9.5.1 or Table 9.6.1
     * @param zoneName
     * @param lpd
     */
    public void setZoneLPDinSpaceBySpace(String zoneName, Double lpd);
    
    /**
     * set a zone's lpd. The lpd value should always in W/m2 unit.
     * and this has to be mapped to either ASHRAE 90.1 Table 9.5.1 or Table 9.6.1
     * @param zoneName
     * @param lpd
     */
    public void setZoneLPDinBuildingTypeMethod(Double lpd);
    
    /**
     * get the total number of zones in this model
     * @return
     */
    public int getNumberOfZone();
    
    /**
     * get the zone's full name
     * @param index
     * @return
     */
    public String getZoneNamebyIndex(int index);
    
}
