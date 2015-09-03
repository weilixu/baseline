package baseline.idfdata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import baseline.generator.EplusObject;
import baseline.generator.IdfReader;
import baseline.generator.IdfReader.ValueNode;
import baseline.generator.OutdoorDesignSpecification;
import baseline.util.ClimateZone;

public class EnergyPlusBuilding {

    /**
     * basic information about the building
     */
    private Double totalFloorArea;
    private Double conditionedFloorArea;
    private Set<String> floorSet;
    private boolean electricHeating;

    /**
     * set point not met
     */
    private Double heatingSetPointNotMet;
    private Double coolingSetPointNotMet;

    /**
     * the required cooling and heating loads
     */
    private Double totalCoolingLoad;
    private Double totalHeatingLoad;

    /**
     * climate zone
     */
    private ClimateZone cZone;

    /**
     * the building thermal zone lists
     */
    private List<ThermalZone> thermalZoneList;
    // for creating HVAC system
    private HashMap<String, ArrayList<ThermalZone>> floorMap;
    private HashMap<String, Boolean> returnFanMap;

    /**
     * EnergyPlus data
     */
    private IdfReader baselineModel;

    public EnergyPlusBuilding(ClimateZone zone, IdfReader baselineModel) {
	thermalZoneList = new ArrayList<ThermalZone>();
	floorMap = new HashMap<String, ArrayList<ThermalZone>>();
	returnFanMap = new HashMap<String, Boolean>();
	totalCoolingLoad = 0.0;
	totalHeatingLoad = 0.0;
	cZone = zone;
	this.baselineModel = baselineModel;
	electricHeating = false;
    }

    /**
     * Reload the data from energyplus output.
     */
    public void initializeBuildingData() {
	thermalZoneList.clear();
	floorMap.clear();
	floorSet = new HashSet<String>();
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

    public void setElectricHeating() {
	electricHeating = true;
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
     * This method must be called prior to get floorMap, get total cooling load
     * and get total heating load and then check return fans
     * 
     * @return
     */
    public void processModelInfo() {
	// building the thermal zones
	for (ThermalZone zone : thermalZoneList) {
	    String block = zone.getBlock();
	    String floor = zone.getFloor();
	    floorSet.add(floor);
	    String level = block + ":" + floor;
	    if (!floorMap.containsKey(level)) {
		floorMap.put(level, new ArrayList<ThermalZone>());
	    }
	    zone.setOAVentilation(getDesignOutdoorAir(zone.getFullName()));
	    floorMap.get(level).add(zone);
	    totalCoolingLoad += zone.getCoolingLoad();
	    totalHeatingLoad += zone.getHeatingLoad();
	}
	checkForReturnFans();
    }

    /**
     * get the zone maximum flow rate
     * 
     * @param zoneName
     * @return
     */
    public Double getZoneMaximumFlowRate(String zoneName) {
	Double coolingFlowRate = 0.0;
	Double heatingFlowRate = 0.0;
	for (ThermalZone zone : thermalZoneList) {
	    if (zone.getFullName().equalsIgnoreCase(zoneName)) {
		coolingFlowRate = zone.getCoolingAirFlow();
		heatingFlowRate = zone.getHeatingAirFlow();
	    }
	}
	return Math.max(coolingFlowRate, heatingFlowRate);
    }

    /**
     * get the floor maximum flow rate from the database
     * 
     * @param floor
     * @return
     */
    public Double getFloorMaximumFlowRate(String floor) {
	ArrayList<ThermalZone> zoneList = floorMap.get(floor);
	Double coolingFlowRate = 0.0;
	Double heatingFlowRate = 0.0;
	
	for (ThermalZone zone : zoneList) {
	    coolingFlowRate += zone.getCoolingAirFlow();
	    heatingFlowRate += zone.getHeatingAirFlow();
	}
	return Math.max(coolingFlowRate, heatingFlowRate);
    }
    
    public Double getFloorMinimumVentilationRate(String floor){
	ArrayList<ThermalZone> zoneList = floorMap.get(floor);
	Double flowVent = 0.0;
	
	for (ThermalZone zone : zoneList) {
	    flowVent += zone.getMinimumVentilation();
	}
	return flowVent;
    }

    /**
     * get the floor map
     * 
     * @return
     */
    public HashMap<String, ArrayList<ThermalZone>> getFloorMap() {
	return floorMap;
    }

    public IdfReader getBaselineModel() {
	return baselineModel;
    }

    public ClimateZone getClimateZone() {
	return cZone;
    }

    public boolean getHeatingMethod() {
	return electricHeating;
    }
    
    public boolean hasReturnFan(){
	Set<String> returnFan = returnFanMap.keySet();
	Iterator<String> returnFanIterator = returnFan.iterator();
	while(returnFanIterator.hasNext()){
	    String fan = returnFanIterator.next();
	    if(returnFanMap.get(fan)){
		return true;
	    }
	}
	return false;
    }

    /**
     * get the total cooling load
     * 
     * @return
     */
    public Double getTotalCoolingLoad() {
	return Math.round(totalCoolingLoad * 100.0) / 100.0;
    }

    /**
     * get the total heating load
     * 
     * @return
     */
    public Double getTotalHeatingLoad() {
	return Math.round(totalHeatingLoad * 100.0) / 100.;
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

    public Integer getNumberOfFloor() {
	return floorSet.size();
    }

    public Double getHeatingSetPointNotMet() {
	return heatingSetPointNotMet;
    }

    public Double getCoolingSetPointNotMet() {
	return coolingSetPointNotMet;
    }

    private void checkForReturnFans() {
	HashMap<String, ArrayList<ValueNode>> airLoops = baselineModel
		.getObjectList("AirLoopHVAC").get("AirLoopHVAC");
	Set<String> airloopList = airLoops.keySet();
	Iterator<String> airLoopIterator = airloopList.iterator();
	while (airLoopIterator.hasNext()) {
	    String airloop = airLoopIterator.next();
	    String branchListName = "";
	    String demandSideOutletName = "";
	    for (int i = 0; i < airLoops.get(airloop).size(); i++) {
		if (airLoops.get(airloop).get(i).getDescription()
			.equals("Branch List Name")) {
		    branchListName = airLoops.get(airloop).get(i)
			    .getAttribute();
		} else if (airLoops.get(airloop).get(i).getDescription()
			.equals("Demand Side Outlet Node Name")) {
		    demandSideOutletName = airLoops.get(airloop).get(i)
			    .getAttribute();
		}
	    }
	    // branch list to check system return fan
	    Boolean returnFan = hasReturnFan(branchListName);
	    // demand side check thermal zones
	    processFloorReturnFanMap(demandSideOutletName, returnFan);
	}
    }
    
    
    private void processFloorReturnFanMap(String demandOutlet, Boolean returnFan) {
	HashMap<String, ArrayList<ValueNode>> mixerList = baselineModel
		.getObjectList("AirLoopHVAC:ZoneMixer").get(
			"AirLoopHVAC:ZoneMixer");
	Set<String> mixerSet = mixerList.keySet();
	Iterator<String> mixerIterator = mixerSet.iterator();
	while (mixerIterator.hasNext()) {
	    String mixer = mixerIterator.next();
	    ArrayList<ValueNode> mixerObject = mixerList.get(mixer);
	    // demand outlet is always at Outlet Node Name field
	    if (mixerObject.get(1).getAttribute().equals(demandOutlet)) {
		for (int i = 2; i < mixerObject.size(); i++) {
		    String zoneName = baselineModel.getValue(
			    "ZoneHVAC:EquipmentConnections", mixerObject.get(i)
				    .getAttribute(), "Zone Name");
		    for (ThermalZone tz : thermalZoneList) {
			if (tz.getFullName().equals(zoneName)) {
			    if (!returnFanMap.get(tz.getFloor())) {
				returnFanMap.put(tz.getFloor(), returnFan);
			    }// if
			}// if
		    }// for
		}// for
	    }// if
	}// while
    }

    private Boolean hasReturnFan(String BranchList) {
	Boolean returnFan = false;
	// 1. get the air loop branch name from branchlist
	String branchName = baselineModel.getValue("BranchList", BranchList,
		"Branch 1 Name");
	// 2. check fan object at first component listed on branch
	String componentName = baselineModel.getValue("Branch", branchName,
		"Component Object Type 1");
	if (componentName.contains("Fan")) {
	    returnFan = true;
	}
	return returnFan;
    }

    private EplusObject getDesignOutdoorAir(String zoneName) {
	String oaObject = baselineModel.getValue("Sizing:Zone", zoneName,
		"Design Specification Outdoor Air Object Name");
	if (oaObject == null) {
	    String zoneListName = baselineModel.getZoneListName(zoneName);
	    oaObject = baselineModel.getValue("Sizing:Zone", zoneListName,
		    "Design Specification Outdoor Air Object Name");
	}
	OutdoorDesignSpecification oa = new OutdoorDesignSpecification(zoneName);

	setUpOAObject(oa, oaObject);
	return oa;
    }

    private void setUpOAObject(OutdoorDesignSpecification oa, String oaObject) {
	// this means we found the outdoor air object for this zone
	// for asset score tool, this is far than enough
	String method = baselineModel.getValue(
		"DesignSpecification:OutdoorAir", oaObject,
		"Outdoor Air Method");
	if (method != null) {
	    oa.changeOutdoorAirMethod(method);
	}
	String oaPerson = baselineModel.getValue(
		"DesignSpecification:OutdoorAir", oaObject,
		"Outdoor Air Flow per Person");
	if (oaPerson != null) {
	    oa.changeAirFlowperPerson(oaPerson);
	}
	String oaFloorArea = baselineModel.getValue(
		"DesignSpecification:OutdoorAir", oaObject,
		"Outdoor Air Flow per Zone Floor Area");
	if (oaFloorArea != null) {
	    oa.changeAirFlowperFloorArea(oaFloorArea);
	}
	String oaZone = baselineModel.getValue(
		"DesignSpecification:OutdoorAir", oaObject,
		"Outdoor Air Flow per Zone");
	if (oaZone != null) {
	    oa.changeAirFlowperZone(oaZone);
	}
	String oaACH = baselineModel.getValue("DesignSpecification:OutdoorAir",
		oaObject, "Outdoor Air Flow Air Changes per Hours");
	if (oaACH != null) {
	    oa.changeAirFlowperACH(oaACH);
	}
	String oaSchedule = baselineModel.getValue(
		"DesignSpecification:OutdoorAir", oaObject,
		"Outdoor Air Flow Rate Fraction Schedule Name");
	if (oaSchedule != null) {
	    oa.changeAirFlowSchedule(oaSchedule);
	}
    }
}
