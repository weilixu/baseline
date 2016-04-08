package baseline.hvac.system1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import baseline.hvac.HVACSystemImplUtil;
import baseline.idfdata.EplusObject;
import baseline.idfdata.KeyValuePair;
import baseline.idfdata.building.EnergyPlusBuilding;
import baseline.idfdata.thermalzone.ThermalZone;

public class HVACSystem1 implements SystemType1{
    // recording all the required data for HVAC system type 3
    private HashMap<String, ArrayList<EplusObject>> objectLists;
    
    private ArrayList<String> zoneHeatingCoilList;

    // building object contains building information and energyplus data
    private EnergyPlusBuilding building;
    
    // plant supply side list
    private ArrayList<String> boilerList;
    
    // pump selection
    private String heatingPump;
    
    // flag indicates whether boiler have been modified in the
    // chekcing system
    private boolean changedBoiler;
    
    // threshold for determine the HVAC components.
    private static final double heatingFloorThreshold = 11150; // m2.
    
    public HVACSystem1(HashMap<String, ArrayList<EplusObject>> objects, EnergyPlusBuilding bldg){
	objectLists = objects;
	building = bldg;
	
	//Set-up all the data structures
	zoneHeatingCoilList = new ArrayList<String>();
	boilerList = new ArrayList<String>();
	
	//initialize pump name
	heatingPump = "HeaderedPumps:ConstantSpeed";

	changedBoiler = false;
	
	
	processSystems();
    }

    @Override
    public HashMap<String, ArrayList<EplusObject>> getSystemData() {
	return objectLists;
    }
    
    private void processSystems(){
	ArrayList<EplusObject> supplySideSystem = new ArrayList<EplusObject>();
	ArrayList<EplusObject> demandSideSystem = new ArrayList<EplusObject>();
	ArrayList<EplusObject> plantSystem = new ArrayList<EplusObject>();	

	ArrayList<EplusObject> supplySideSystemTemplate = objectLists
		.get("Supply Side System");
	ArrayList<EplusObject> demandSideSystemTemplate = objectLists
		.get("Demand Side System");
	ArrayList<EplusObject> plantSystemTemplate = objectLists.get("Plant");
	

	HashMap<String, ArrayList<ThermalZone>> floorMap = building
		.getFloorMap();
	
	Set<String> floorMapSet = floorMap.keySet();
	Iterator<String> floorMapIterator = floorMapSet.iterator();
	
	int zoneCounter = 0;
	while(floorMapIterator.hasNext()){
	    String floor = floorMapIterator.next();
	    //first process the demand side system and their connection to
	    // supply side system
	    ArrayList<ThermalZone> zones = floorMap.get(floor);
	    for(ThermalZone zone: zones){
		zoneCounter ++;
		demandSideSystem.addAll(processDemandTemp(zone.getFullName(), demandSideSystemTemplate));
		// add the outdoor air object for demand zone
		demandSideSystem.add(zone.getOutdoorAirObject());
		supplySideSystem.addAll(processSupplyTemp(zone.getFullName(),supplySideSystemTemplate));
	    }
	}
	if(building.getInfoObject()!=null){
	    building.getInfoObject().setNumOfSystem(zoneCounter);
	}
	plantSystem.addAll(processPlantTemp(plantSystemTemplate));
	System.out.println("Counting the rooms: " + zoneCounter);
	objectLists.put("Supply Side System", supplySideSystem);
	objectLists.put("Demand Side System", demandSideSystem);
	objectLists.put("Plant", plantSystem);
	System.out.println("Re-tunning the supply side system...");
	checkSupplySideSystem();
	System.out.println("Connect plans");
	processConnections();
    }
    
    /**
     * A method to process the system connections
     */
    private void processConnections() {
	ArrayList<EplusObject> plantSystem = objectLists.get("Plant");
	HVACSystemImplUtil.plantConnectionForSys1(plantSystem,
		boilerList,
		zoneHeatingCoilList);
    }
    
    
    /**
     * updates the supply side system parameters. Check economizer, fan power
     */
    private void checkSupplySideSystem() {
	ArrayList<EplusObject> supplySystem = objectLists
		.get("Supply Side System");
	// determine the economizers.
	double economizer = building.getClimateZone()
		.getEconomizerShutoffLimit();
	building.getInfoObject().setHasEconomizer(economizer);
	double totalFanPower = 0;
	for (EplusObject eo : supplySystem) {
	    if (eo.getObjectName().equalsIgnoreCase("Controller:OutdoorAir")) {
		if (economizer > -1) {
		    HVACSystemImplUtil.economizer(eo, economizer);
		}
	    } else if (eo.getObjectName()
		    .equalsIgnoreCase("Fan:ConstantVolume")) {
		// get the floor name
		String zone = eo.getKeyValuePair(0).getValue().split(" ")[0];
		HVACSystemImplUtil.updateFanPowerforSystem3To4(eo,
			building.getZoneMaximumFlowRate(zone));
		for (int i = 0; i < eo.getSize(); i++) {
		    if (eo.getKeyValuePair(i).getKey()
			    .equals("Pressure Rise")) {
			double pressureRise = Double
				.parseDouble(eo.getKeyValuePair(i).getValue());
			totalFanPower += (pressureRise / 0.6
				* building.getZoneMaximumFlowRate(zone));
		    }
		}
	    }
	    building.getInfoObject().setFanPower(totalFanPower);
	}
    }

    /**
     * process the HVAC supply air side system
     * 
     * @param zone
     * @param supplySideSystemTemplate
     * @return
     */
    private ArrayList<EplusObject> processSupplyTemp(String zone,
	    ArrayList<EplusObject> supplySideSystemTemplate) {
	ArrayList<EplusObject> supplyTemp = new ArrayList<EplusObject>();
	for (EplusObject eo : supplySideSystemTemplate) {
	    EplusObject temp = eo.clone();

	    /*
	     * replace the special characters that contains floors
	     */
	    if (temp.hasSpecialCharacters()) {
		temp.replaceSpecialCharacters(zone);
	    }

	    // check if this is the connection between supply side and demand
	    // side systems
	    if (temp.getObjectName()
		    .equalsIgnoreCase("AirLoopHVAC:ZoneSplitter")) {
		KeyValuePair splitterPair = new KeyValuePair("Outlet Node Name",
			zone + " Zone Equip Inlet");
		temp.addField(splitterPair);
	    }

	    // check if this is the connection between supply side and demand
	    // side systems
	    if (temp.getObjectName()
		    .equalsIgnoreCase("AirLoopHVAC:ZoneMixer")) {

		KeyValuePair mixerPair = new KeyValuePair("Intlet Node Name",
			zone + " Return Outlet");
		temp.addField(mixerPair);
	    }
	    supplyTemp.add(temp);
	}
	return supplyTemp;
    }

    /**
     * process the demand side system
     * 
     * @param zone
     * @param zoneTemp
     * @return
     */
    private ArrayList<EplusObject> processDemandTemp(String zone,
	    ArrayList<EplusObject> zoneTemp) {
	ArrayList<EplusObject> demandTemp = new ArrayList<EplusObject>();
	for (EplusObject eo : zoneTemp) {
	    EplusObject temp = eo.clone();
	    // check special characters to avoid useless loop inside the replace
	    // special characters
	    if (temp.hasSpecialCharacters()) {
		temp.replaceSpecialCharacters(zone);
	    }
	    demandTemp.add(temp);
	}
	zoneHeatingCoilList.add(zone + " Heating Coil HW Branch");
	return demandTemp;
    }
    
    
    private ArrayList<EplusObject> processPlantTemp(ArrayList<EplusObject> plantSideTemp){
	double floorArea = building.getConditionedFloorArea();//G3.1.3.5,G3.1.3.2
	
	//calculate the number of boilers
	int numberOfBoiler = HVACSystemImplUtil.boilerNumberCalculation(floorArea);
	System.out.println("We Found " + numberOfBoiler + "Boilers");
	if(numberOfBoiler==1){
	    boilerList.add("Boiler%");
	}
	
	ArrayList<EplusObject> plantTemp = new ArrayList<EplusObject>();
	
	// we use iterator because we will delete some objects in this loop
	// (pumps)
	Iterator<EplusObject> eoIterator = plantSideTemp.iterator();
	while(eoIterator.hasNext()){
	    EplusObject temp = eoIterator.next().clone();
	    
	    //select pumps from Templates based on the inputs
	    //choose hot water loop pumps
	    if(temp.getKeyValuePair(0).getValue().equals("Hot Water Loop HW Supply Pump")){
		if(floorArea <= heatingFloorThreshold){
		    //smaller than threshold, remove the variable speed
		    if(temp.getObjectName().equalsIgnoreCase("HeaderedPumps:VariableSpeed")){
			eoIterator.remove();
			continue;
		    }
		}else{
		    if (temp.getObjectName().equalsIgnoreCase(
			    "HeaderedPumps:ConstantSpeed")) {
			eoIterator.remove();
			heatingPump = "HeaderedPumps:VariableSpeed";
			continue;
		    }
		}
	    }
	    
	    //this should be remove to the next loop later update the hot water
	    //loop branch information
	    if(temp.getKeyValuePair(0).getValue().equals("Hot Water Loop HW Supply Inlet Branch")){
		//this is the number of the component 1 object type in branch
		temp.getKeyValuePair(3).setValue(heatingPump);
	    }
	    
	    //decide the number of boiler. It possibly add more boilers
	    if(numberOfBoiler > 1){
		plantTemp.addAll(processBoilers(temp));
		if(changedBoiler){
		    temp.replaceSpecialCharacters("Boiler1");
		}
	    }
	    plantTemp.add(temp);
	}
	return plantTemp;
    }
    
    /**
     * find out the number of boilers and add them into the system The maximum
     * number of boilers is 2. Precondition: the maximum of boiler = 2; the
     * boiler connection branch name is Boiler% HW Branch
     * 
     * postcondtion: add additional boiler to the plant temp, note the branch is
     * not added yet, this will be process in the second loop
     * 
     * @param heatingLoad
     * @param temp
     */
    private ArrayList<EplusObject> processBoilers(EplusObject temp) {
	ArrayList<EplusObject> tempList = new ArrayList<EplusObject>();
	if (temp.getObjectName().equalsIgnoreCase("Boiler:HotWater")) {
	    // if greater than the threshold, increase the number of boiler
	    // to 2
	    changedBoiler = true;
	    EplusObject anotherBoiler = temp.clone();
	    anotherBoiler.replaceSpecialCharacters("Boiler2");
	    tempList.add(anotherBoiler);
	    boilerList.add("Boiler1");
	    boilerList.add("Boiler2");
	} else if (temp.getObjectName().equalsIgnoreCase("Branch")
		&& temp.getKeyValuePair(0).getValue()
			.equals("Boiler% HW Branch")) {
	    changedBoiler = true;
	    EplusObject anotherBoilerBranch = temp.clone();
	    temp.replaceSpecialCharacters("Boiler1");
	    anotherBoilerBranch.replaceSpecialCharacters("Boiler2");
	    tempList.add(anotherBoilerBranch);
	} else {
	    changedBoiler = false;
	}
	return tempList;
    }
    
}
