package baseline.idfdata.building;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import baseline.construction.opaque.OpaqueEnvelopeParser;
import baseline.exception.detector.Detector;
import baseline.exception.detector.DistrictCoolingDetector;
import baseline.exception.detector.DistrictHeatingDetector;
import baseline.exception.detector.ReturnFanDetector;
import baseline.idfdata.BaselineInfo;
import baseline.idfdata.EplusObject;
import baseline.idfdata.IdfReader;
import baseline.idfdata.OutdoorDesignSpecification;
import baseline.idfdata.IdfReader.ValueNode;
import baseline.idfdata.thermalzone.ThermalZone;
import baseline.util.ClimateZone;

public class EnergyPlusBuilding implements BuildingLight, BuildingConstruction {

    /**
     * basic information about the building
     */
    private String buildingType;
    private Double totalFloorArea;
    private Double conditionedFloorArea;
    private Set<String> floorSet;
    private boolean electricHeating;
    
    /**
     * Exception detector plugin
     */
    List<Detector> detectorList;
    
    //return fan information
    private double numberOfSystem = 0.0;
    private double supplyReturnRatio = 0.0;
    private boolean hasReturnFan = false;
    
    //District System
    private boolean districtHeat = false;
    private boolean districtCool = false;
    
    private BaselineInfo info;

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
    private HashMap<String, HashMap<String, ArrayList<ValueNode>>> serviceHotWater;
    private boolean addedServiceWater = false;

    /**
     * EnergyPlus data
     */
    private IdfReader baselineModel;

    /**
     * Lighting Module data
     */
    private final static String LIGHT = "Lights";
    //private final static String ZONELIST = "ZoneList";

    /**
     * Construction Module Data
     */
    // EnergyPlus Objects that require to be removed
    private static final String MATERIAL = "Material";
    private static final String MATERIALMASS = "Material:NoMass";
    private static final String CONSTRUCTION = "Construction";
    private static final String SIMPLE_WINDOW = "WindowMaterial:SimpleGlazingSystem";

    // Baseline Construction Name (to replace the original constructions)
    private static final String EXTERNAL_WALL = "Project Wall";
    private static final String ROOF = "Project Roof";
    private static final String PARTITION = "Project Partition";
    private static final String INTERNAL_FLOOR = "Project Internal Floor";
    private static final String BG_WALL = "Project Below Grade Wall";
    private static final String EXTERNAL_FLOOR = "Project External Floor";
    private static final String SOG_FLOOR = "Project Slab On Grade Floor";
    private static final String WINDOW = "Project Window";
    // private static final String CURTAIN_WALL = "Project Curtain Wall";
    private static final String SKYLIGHT = "Project Skylight";

    // EnergyPlus objects that relates to the construction changes
    private static final String BLDG_SURFACE = "BuildingSurface:Detailed";
    private static final String BLDG_FENE = "FenestrationSurface:Detailed";
    private static final String BLDG_INTERNAL_MASS = "InternalMass";

    public EnergyPlusBuilding(String bldgType, ClimateZone zone,
	    IdfReader baselineModel, BaselineInfo infomation) {
	buildingType = bldgType;
	thermalZoneList = new ArrayList<ThermalZone>();
	floorMap = new HashMap<String, ArrayList<ThermalZone>>();
	serviceHotWater = new HashMap<String, HashMap<String, ArrayList<ValueNode>>>();
	totalCoolingLoad = 0.0;
	totalHeatingLoad = 0.0;
	cZone = zone;
	this.baselineModel = baselineModel;
	electricHeating = false;
	this.info = infomation;
	if (info != null) {
	    info.setHeatSource("NaturalGas");
	}

	// remove unnecessary objects in the model
	this.baselineModel.removeEnergyPlusObject("Daylighting:Controls");
	
	// register exception detectors plug-ins
	registerExceptionDetectors();
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

    public void initialInfoForSystem(String system) {
	if (system.equals("System Type 7")) {
	    info.setSystemType("System Type 7");
	    info.setFanControlType("Variable Control Fans");
	    info.setChillerCOP(6.1);
	    info.setChillerIPLV(6.4);
	    info.setChillerCapacity(info.getCoolingCapacity());
	    info.setBoilerCapacity(info.getHeatingCpacity());
	} else if (system.equals("System Type 5")) {
	    info.setSystemType("System Type 5");
	    info.setFanControlType("Variable Control Fans");
	    info.setCoolingEER(10.78);
	    info.setCoolingIEER(11.15);
	    info.setBoilerEfficiency(0);
	} else if (system.equals("System Type 8")) {
	    info.setSystemType("System Type 8");
	    info.setFanControlType("Variable Control Fans");
	    info.setChillerCOP(6.1);
	    info.setChillerIPLV(6.4);
	    info.setChillerCapacity(info.getCoolingCapacity());
	} else if (system.equals("System Type 6")) {
	    info.setSystemType("System Type 6");
	    info.setFanControlType("Variable Control Fans");
	    info.setCoolingEER(10.78);
	} else if (system.equals("System Type 3")) {
	    info.setSystemType("System Type 3");
	    info.setFanControlType("Constant Control Fans");
	    info.setCoolingEER(10.78);
	    info.setUnitaryHeatingCOP(3.2);
	}else if(system.equals("System Type 4")){
	    info.setSystemType("System Type 4");
	    info.setFanControlType("Constant Control Fans");
	    info.setCoolingEER(10.78);
	    info.setUnitaryHeatingCOP(3.2);
	}else if(system.equals("System Type 1")){
	    info.setSystemType("System Type 1");
	    info.setFanControlType("Constant Control Fans");
	    info.setCoolingEER(10.78);
	    info.setUnitaryHeatingCOP(3.2);
	}else if(system.equals("System Type 2")){
	    info.setSystemType("System Type 2");
	    info.setFanControlType("Constant Control Fans");
	    info.setCoolingEER(10.78);
	    info.setUnitaryHeatingCOP(3.2); 
	}
	info.setSupplyAirFlow(getSupplyAirFlow());
	info.setOutdoorAirFlow(getOutdoorAir());
    }

    public BaselineInfo getInfoObject() {
	return info;
    }

    /*
     * All Setter Methods
     */
    public void setTotalFloorArea(Double area) {
	totalFloorArea = area;
	if (info != null) {
	    info.setBuildingArea(area);
	}
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
	if (info != null) {
	    info.setHeatSource("Electric");
	}
    }
    
    public void setSupplyReturnRatio(double ratio){
	supplyReturnRatio = ratio;
    }
    
    public void setNumberOfSystem(double number){
	numberOfSystem = number;
    }
    
    public void setReturnFanIndicator(boolean has){
	hasReturnFan = has;
    }
    
    public void setDistrictHeat(boolean dist){
	districtHeat = dist;
    }
    
    public void setDistrictCool(boolean dist){
	districtCool = dist;
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
	    String level = null;
	    if (floor == null && block == null) {
		level = null;
	    } else if (floor == null && block != null) {
		// plenum condition
		floorSet.add(block);
		level = block;
	    } else {
		floorSet.add(floor);
		level = block + ":" + floor;
	    }

	    if (level != null) {
		if (!floorMap.containsKey(level)) {
		    floorMap.put(level, new ArrayList<ThermalZone>());
		}
		zone.setOAVentilation(getDesignOutdoorAir(zone.getFullName()));
		floorMap.get(level).add(zone);
		totalCoolingLoad += zone.getCoolingLoad();
		totalHeatingLoad += zone.getHeatingLoad();
	    }
	}
	if (info != null) {
	    info.setCoolingCapacity(totalCoolingLoad);
	    info.setHeatingCpacity(totalHeatingLoad);
	}
	//search for exceptions
	for(Detector detector: detectorList){
	    detector.foundException(this);
	}
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
	// System.out.println(coolingFlowRate + " " + heatingFlowRate);
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

    public Double getFloorMinimumVentilationRate(String floor) {
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

    public boolean getHeatingMethod() {
	return electricHeating;
    }

    public boolean hasReturnFan() {
	return hasReturnFan;
    }
    
    public boolean isDistrictHeat(){
	return districtHeat;
    }
    
    public boolean isDistrictCool(){
	return districtCool;
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

    public Double getSupplyReturnFanRatio() {
	return supplyReturnRatio / numberOfSystem;
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

    /**
     * API Methods for lighting module
     */
    @Override
    public String getBuildingType() {
	return buildingType;
    }

    @Override
    public String getZoneType(String zoneName) {
	for (ThermalZone zone : thermalZoneList) {
	    if (zone.getFullName().equals(zoneName)) {
		if (zone.getZoneType() != null) {
		    return zone.getZoneType();
		}
	    }
	}
	return null;
    }

    @Override
    public void setZoneLPDinBuildingTypeMethod(Double lpd) {
	// 1. Loop over lights object to find zones
	HashMap<String, HashMap<String, ArrayList<ValueNode>>> lights = baselineModel
		.getObjectList(LIGHT);
	// HashMap<String, HashMap<String, ArrayList<ValueNode>>> zoneList =
	// baselineModel
	// .getObjectList(ZONELIST);
	if (lights != null) {
	    Set<String> elementCount = lights.get(LIGHT).keySet();
	    Iterator<String> elementIterator = elementCount.iterator();
	    // boolean zoneExist = false; // flag indicate whether it uses zone
	    // // name or zone list name
	    while (elementIterator.hasNext()) {
		// if there is still element left and we haven't find the zone,
		// continue loop
		String count = elementIterator.next();
		ArrayList<ValueNode> lightsList = lights.get(LIGHT).get(count);
		for (ValueNode v : lightsList) {

		    if (v.getDescription().equalsIgnoreCase(
			    "DESIGN LEVEL CALCULATION METHOD")) {
			v.setAttribute("Watts/Area");
		    }
		    if (v.getDescription().equalsIgnoreCase(
			    "Watts per Zone Floor Area")) {
			v.setAttribute(lpd.toString());
		    }
		}
	    }
	    // // 2. Once we confirm we cannot find the lights object for this
	    // // particular zone
	    // // we need to look for the correspondent light object in zonelist
	    // // object
	    // if (!zoneExist && zoneList != null) {
	    // // lets first loop through zone list, zone by zone
	    // Set<String> zoneListElement = lights.get(ZONELIST).keySet();
	    // Iterator<String> zoneListElementIterator = zoneListElement
	    // .iterator();
	    // String targetZoneListName = null;
	    // while (zoneListElementIterator.hasNext()) {
	    // String count = zoneListElementIterator.next();
	    // ArrayList<ValueNode> zoneListList = lights.get(ZONELIST)
	    // .get(count);
	    // String zoneListName = zoneListList.get(0).getAttribute();
	    // for (ValueNode v : zoneListList) {
	    // // we find the zone in a particular zone list
	    // if (v.getAttribute().equals(zoneName)) {
	    // targetZoneListName = zoneListName;
	    // }
	    // }
	    // }
	    // if (targetZoneListName != null) {// this means we find the
	    // // zonelist
	    // // look at the lights objects again to find correspondent
	    // // zoneList name
	    // elementIterator = elementCount.iterator();
	    // while (elementIterator.hasNext()) {
	    // // if there is still element left and we haven't find
	    // // the zone, continue loop
	    // String count = elementIterator.next();
	    // ArrayList<ValueNode> lightsList = lights.get(LIGHT)
	    // .get(count);
	    // boolean find = false;
	    // for (ValueNode v : lightsList) {
	    // if (v.getDescription().equalsIgnoreCase(
	    // "ZONE OR ZONELIST NAME")
	    // && v.getAttribute().equals(
	    // targetZoneListName)) {
	    // // if we find zone name matches, we need to
	    // // revise its lighting power density
	    // // so we turn the flag to true
	    // find = true;
	    // zoneExist = true;
	    // } else if (find
	    // && v.getDescription().equalsIgnoreCase(
	    // "DESIGN LEVEL CALCULATION METHOD")) {
	    // v.setAttribute("Watts/Area");
	    // } else if (find
	    // && v.getDescription().contains(
	    // "WATTS PER ZONE FLOOR AREA")) {
	    // v.setAttribute(lpd.toString());
	    // }// if
	    // }// for
	    // }// while
	    // }// if
	    // }// if
	}// if
	baselineModel.replaceEnergyPlusObjects(lights);
    }

    @Override
    public void setZoneLPDinSpaceBySpace(String zoneName, Double lpd) {

    }

    @Override
    public int getNumberOfZone() {
	return thermalZoneList.size();
    }

    @Override
    public String getZoneNamebyIndex(int index) {
	return thermalZoneList.get(index).getFullName();
    }

    /**
     * API method for constructions
     */
    @Override
    public void replaceConstruction(OpaqueEnvelopeParser envelope) {
	// remove all the building envelope related objects
	baselineModel.removeEnergyPlusObject(MATERIAL);
	baselineModel.removeEnergyPlusObject(MATERIALMASS);
	baselineModel.removeEnergyPlusObject(CONSTRUCTION);
	baselineModel.removeEnergyPlusObject(SIMPLE_WINDOW);

	// add new building envelope related objects
	ArrayList<EplusObject> objects = envelope.getObjects(); // retrieve the
								// data from
								// database
	for (EplusObject o : objects) {
	    String[] objectValues = new String[o.getSize()];
	    String[] objectDes = new String[o.getSize()];
	    // loop over the key-value pairs
	    for (int i = 0; i < objectValues.length; i++) {
		objectValues[i] = o.getKeyValuePair(i).getValue();
		objectDes[i] = o.getKeyValuePair(i).getKey();
	    }
	    // add the object to the baseline model
	    baselineModel.addNewEnergyPlusObject(o.getObjectName(),
		    objectValues, objectDes);
	}

	// change the EnergyPlus object that relates to the constructions
	System.out.println("Start replacing the building surfaces...");
	replaceBuildingSurface();
	System.out.println("Start replacing the fenestration surfaces...");
	replaceFenestrationSurface();
	System.out.println("Start replacing the internal mass...");
	replaceInternalMass();
    }

    @Override
    public ClimateZone getClimateZone() {
	return cZone;
    }

    /**
     * removes the HVAC objects and build service hot water model This method
     * firstly will trace any inputs that relates to service hot water system
     * Note the name of components in the service hot water must contain DHWSys
     * strings. then this method will remove the whole objects.
     * 
     * @param s
     */
    public void removeHVACObject(String s) {
	HashMap<String, HashMap<String, ArrayList<ValueNode>>> objectList = baselineModel
		.getObjectList(s);

	if (objectList != null) {
	    Set<String> elementCount = objectList.get(s).keySet();
	    Iterator<String> elementIterator = elementCount.iterator();
	    while (elementIterator.hasNext()) {
		String count = elementIterator.next();
		ArrayList<ValueNode> object = objectList.get(s).get(count);
		for (ValueNode v : object) {
		    if (v.getDescription().contains("Name")
			    && v.getAttribute().contains("SHWSys")) {
			if (!serviceHotWater.containsKey(s)) {
			    serviceHotWater
				    .put(s,
					    new HashMap<String, ArrayList<ValueNode>>());
			}
			serviceHotWater.get(s).put(count, object);
		    }
		}
	    }
	}

	addedServiceWater = false;
	baselineModel.removeEnergyPlusObject(s);
    }

    /**
     * allow other method to insert energyplus object to the model
     * 
     * @param name
     * @param objectValues
     * @param objectDes
     */
    public void insertEnergyPlusObject(String name, String[] objectValues,
	    String[] objectDes) {
	baselineModel.addNewEnergyPlusObject(name, objectValues, objectDes);
    }

    public void generateEnergyPlusModel(String filePath, String fileName,
	    String degree) {
	// merge the all the information before write out
	// 1. add service hot water back to the model
	if (!addedServiceWater) {
	    Set<String> objectList = serviceHotWater.keySet();
	    Iterator<String> objectIterator = objectList.iterator();
	    while (objectIterator.hasNext()) {
		String objectName = objectIterator.next();
		HashMap<String, ArrayList<ValueNode>> elementList = serviceHotWater
			.get(objectName);
		Set<String> elementSet = elementList.keySet();
		Iterator<String> elementIterator = elementSet.iterator();
		while (elementIterator.hasNext()) {
		    String element = elementIterator.next();
		    ArrayList<ValueNode> object = elementList.get(element);
		    baselineModel.addNewEnergyPlusObject(objectName, object);
		}
	    }
	    addedServiceWater = true;
	}

	HashMap<String, ArrayList<ValueNode>> buildingObject = baselineModel
		.getObjectListCopy("Building");
	buildingObject.get("0").get(1).setAttribute(degree);

	// 2. write out the model
	baselineModel.WriteIdf(filePath, fileName);
    }

    private double getSupplyAirFlow() {
	double supplyAir = 0.0;
	Set<String> floors = floorMap.keySet();
	Iterator<String> floorIterator = floors.iterator();
	while (floorIterator.hasNext()) {
	    String floor = floorIterator.next();
	    supplyAir = getFloorMaximumFlowRate(floor);
	}
	return supplyAir;
    }

    private double getOutdoorAir() {
	double outdoorAir = 0.0;
	Set<String> floors = floorMap.keySet();
	Iterator<String> floorIterator = floors.iterator();
	while (floorIterator.hasNext()) {
	    String floor = floorIterator.next();
	    outdoorAir = getFloorMinimumVentilationRate(floor);
	}
	return outdoorAir;
    }

    /**
     * replace the internal mass objects with updated constructions. So far this
     * method only replace the constructions with internal walls (partitions)
     * regard to the generated idf file from Asset Score Tool
     */
    private void replaceInternalMass() {
	HashMap<String, HashMap<String, ArrayList<ValueNode>>> mass = baselineModel
		.getObjectList(BLDG_INTERNAL_MASS);
	if (mass != null) {
	    Set<String> elementCount = mass.get(BLDG_INTERNAL_MASS).keySet();
	    Iterator<String> elementIterator = elementCount.iterator();
	    while (elementIterator.hasNext()) {
		String count = elementIterator.next();
		ArrayList<ValueNode> massList = mass.get(BLDG_INTERNAL_MASS)
			.get(count);

		for (ValueNode v : massList) {
		    if (v.getDescription()
			    .equalsIgnoreCase("CONSTRUCTION NAME")) {
			v.setAttribute(PARTITION);
		    }
		}
	    }
	    baselineModel.replaceEnergyPlusObjects(mass);
	}
    }

    /**
     * replace the fenestration surface. The checking algorithm depends on the
     * name generated from asset score tool. fenestration --> windows skylight
     * -->Skylight
     */
    private void replaceFenestrationSurface() {
	HashMap<String, HashMap<String, ArrayList<ValueNode>>> surfaces = baselineModel
		.getObjectList(BLDG_FENE);
	if (surfaces != null) {
	    Set<String> elementCount = surfaces.get(BLDG_FENE).keySet();
	    Iterator<String> elementIterator = elementCount.iterator();
	    while (elementIterator.hasNext()) {
		String count = elementIterator.next();
		ArrayList<ValueNode> fenestrationList = surfaces.get(BLDG_FENE)
			.get(count);
		String surfaceType = null;

		// first loop, find the criteria for the selection
		for (ValueNode v : fenestrationList) {
		    if (v.getDescription().equalsIgnoreCase("Surface Type")) {
			String construction = v.getAttribute();
			if (construction.equalsIgnoreCase("window")) {
			    surfaceType = "window";
			} else if (construction.contains("Daylight")) {
			    surfaceType = "skylight";
			}
		    }
		}

		// set the construction name for the fenestration
		String constructionName = null;
		if (surfaceType != null) {
		    if (surfaceType.equalsIgnoreCase("window")) {
			constructionName = WINDOW;
		    } else if (surfaceType.equalsIgnoreCase("skylight")) {
			constructionName = SKYLIGHT;
		    }
		}

		// second loop, change the construction name
		for (ValueNode v : fenestrationList) {
		    if (v.getDescription()
			    .equalsIgnoreCase("CONSTRUCTION NAME")) {
			v.setAttribute(constructionName);
			break;
		    }
		}
	    }
	    baselineModel.replaceEnergyPlusObjects(surfaces);
	}
    }

    /**
     * replace the building surface constructions
     */
    private void replaceBuildingSurface() {
	HashMap<String, HashMap<String, ArrayList<ValueNode>>> surfaces = baselineModel
		.getObjectList(BLDG_SURFACE);
	if (surfaces != null) {
	    Set<String> elementCount = surfaces.get(BLDG_SURFACE).keySet();
	    Iterator<String> elementIterator = elementCount.iterator();
	    while (elementIterator.hasNext()) {
		String count = elementIterator.next();
		ArrayList<ValueNode> surfaceList = surfaces.get(BLDG_SURFACE)
			.get(count);
		String surfaceType = null;
		String outsideBoundary = null;

		// first loop, find the criteria for the selection
		for (ValueNode v : surfaceList) {
		    if (v.getDescription().equalsIgnoreCase("SURFACE TYPE")) {
			surfaceType = v.getAttribute();
		    }
		    if (v.getDescription().equalsIgnoreCase(
			    "OUTSIDE BOUNDARY CONDITION")) {
			outsideBoundary = v.getAttribute();
		    }
		}

		String constructionName = null;
		if (surfaceType != null && outsideBoundary != null) {
		    if (surfaceType.equalsIgnoreCase("WALL")
			    && outsideBoundary.equalsIgnoreCase("OUTDOORS")) {
			constructionName = EXTERNAL_WALL;
		    } else if (surfaceType.equalsIgnoreCase("WALL")
			    && outsideBoundary.equalsIgnoreCase("SURFACE")) {
			constructionName = PARTITION;
		    } else if (surfaceType.equalsIgnoreCase("WALL")
			    && outsideBoundary.equalsIgnoreCase("GROUND")) {
			constructionName = BG_WALL;
		    } else if (surfaceType.equalsIgnoreCase("FLOOR")
			    && outsideBoundary.equalsIgnoreCase("SURFACE")) {
			constructionName = INTERNAL_FLOOR;
		    } else if (surfaceType.equalsIgnoreCase("FLOOR")
			    && outsideBoundary.equalsIgnoreCase("OUTDOORS")) {
			constructionName = EXTERNAL_FLOOR;
		    } else if (surfaceType.equalsIgnoreCase("FLOOR")
			    && outsideBoundary.equalsIgnoreCase("GROUND")) {
			constructionName = SOG_FLOOR;
		    } else if (surfaceType.equalsIgnoreCase("CEILING")) {
			constructionName = INTERNAL_FLOOR;
		    } else if (surfaceType.equalsIgnoreCase("ROOF")) {
			constructionName = ROOF;
		    } else {
			constructionName = ""; // hopefully never gets to this
					       // point
		    }
		}

		// second loop, modify the construction name
		for (ValueNode v : surfaceList) {
		    if (v.getDescription()
			    .equalsIgnoreCase("CONSTRUCTION NAME")) {
			v.setAttribute(constructionName);
			break;
		    }
		}
	    }
	    baselineModel.replaceEnergyPlusObjects(surfaces);
	}
    }
    
    private void registerExceptionDetectors(){
	detectorList = new ArrayList<Detector>();
	detectorList.add(new ReturnFanDetector());
	detectorList.add(new DistrictHeatingDetector());
	detectorList.add(new DistrictCoolingDetector());
    }

}
