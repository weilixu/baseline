package baseline.idfdata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EnergyPlusBuilding {

    private Double totalFloorArea;
    private Double conditionedFloorArea;

    private Double heatingSetPointNotMet;
    private Double coolingSetPointNotMet;

    private Double totalCoolingLoad;
    private Double totalHeatingLoad;

    private List<ThermalZone> thermalZoneList;
    private HashMap<String, ArrayList<ThermalZone>> floorMap;

    public EnergyPlusBuilding() {
	thermalZoneList = new ArrayList<ThermalZone>();
	floorMap = new HashMap<String, ArrayList<ThermalZone>>();
	totalCoolingLoad = 0.0;
	totalHeatingLoad = 0.0;
    }

    /*
     * All Setter Methods
     */
    public void setTotalFloorArea(Double area) {
	totalFloorArea = area;
    }

    public void setConditionedFloorArea(Double area) {
	conditionedFloorArea = area;
    }

    public void setHeatTimeSetPointNotMet(Double hr) {
	heatingSetPointNotMet = hr;
    }

    public void setCoolTimeSetPointNotMet(Double hr) {
	coolingSetPointNotMet = hr;
    }

    /**
     * add thermal zones to the data structure
     * 
     * @param zone
     */
    public void addThermalZone(ThermalZone zone) {
	thermalZoneList.add(zone);
    }

    /**
     * This method must be called prior to get floorMap, get totalcoolingload
     * and get total heating load
     * 
     * @return
     */
    public void getThermalZoneInfo() {
	// building the thermal zones
	for (ThermalZone zone : thermalZoneList) {
	    if (!floorMap.containsKey(zone.getFloor())) {
		floorMap.put(zone.getFloor(), new ArrayList<ThermalZone>());
	    }
	    floorMap.get(zone.getFloor()).add(zone);
	    totalCoolingLoad += zone.getCoolingLoad();
	    totalHeatingLoad += zone.getHeatingLoad();
	}
    }
    
    /**
     * get the floor map
     * @return
     */
    public HashMap<String, ArrayList<ThermalZone>> getFloorMap(){
	return floorMap;
    }
    
    /**
     * get the total cooling load
     * @return
     */
    public Double getTotalCoolingLoad() {
	return Math.round(totalCoolingLoad*100.0)/100.0;
    }
    
    /**
     * get the total heating load
     * @return
     */
    public Double getTotalHeatingLoad() {
	return Math.round(totalHeatingLoad*100.0)/100.;
    }

    /*
     * All getter methods
     */
    public Double getTotalFloorArea() {
	return totalFloorArea;
    }

    public Double getConditionedFloorArea() {
	return conditionedFloorArea;
    }

    public Double getHeatingSetPointNotMet() {
	return heatingSetPointNotMet;
    }

    public Double getCoolingSetPointNotMet() {
	return coolingSetPointNotMet;
    }
}
