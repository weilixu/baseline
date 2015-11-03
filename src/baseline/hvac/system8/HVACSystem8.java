package baseline.hvac.system8;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import baseline.generator.EplusObject;
import baseline.generator.KeyValuePair;
import baseline.hvac.HVACSystemImplUtil;
import baseline.idfdata.EnergyPlusBuilding;
import baseline.idfdata.ThermalZone;

public class HVACSystem8 implements SystemType8{
    // recording all the required data for HVAC system type 8
    private HashMap<String, ArrayList<EplusObject>> objectLists;
    // building object contains building information and energyplus data
    private EnergyPlusBuilding building;
    
    // supply air connection list
    private ArrayList<String> zoneSplitterList;
    private ArrayList<String> zoneMixerList;
    
    // plant demand side list
    private ArrayList<String> systemCoolingCoilList;
    
    // plant supply side list
    private ArrayList<String> chillerList;
    private ArrayList<String> towerList;
    
    // pump selection
    private String coolingPump;
    
    // flag indicates whether boiler, chiller or tower have been modified in the
    // chekcing system
    private boolean changedChiller;
    private boolean changedTower;
    
    // threshold for determine the HVAC components.
    private static final double coolingLoadThreshold = 10550558;// watt
    private int numberOfChiller = 1;
    
    public HVACSystem8(HashMap<String, ArrayList<EplusObject>> objects,
	    EnergyPlusBuilding bldg){
	objectLists = objects;
	building = bldg;

	// Set-up all the data structures
	zoneSplitterList = new ArrayList<String>();
	zoneMixerList = new ArrayList<String>();
	systemCoolingCoilList = new ArrayList<String>();
	chillerList = new ArrayList<String>();
	towerList = new ArrayList<String>();
	// initialize pump name
	coolingPump = "HeaderedPumps:ConstantSpeed";

	changedChiller = false;
	changedTower = false;
	
	processSystems();
    }

    @Override
    public HashMap<String, ArrayList<EplusObject>> getSystemData() {
	return objectLists;
    }
    
    
    /**
     * Process the zones in the building
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
	// record the connection links in the HVAC system
	String zoneSplitter = zone + " Splitter Outlet Node";
	String zoneMixer = zone + " Return Outlet";
	// this is only for system type 8
	// add the connection links to another data lists for later
	// processing
	zoneSplitterList.add(zoneSplitter);
	zoneMixerList.add(zoneMixer);
	return demandTemp;
    }
    
    /**
     * process the HVAC supply air side system
     * 
     * @param floor
     * @param supplySideSystemTemplate
     * @return
     */
    private ArrayList<EplusObject> processSupplyTemp(String floor,
	    ArrayList<EplusObject> supplySideSystemTemplate) {
	ArrayList<EplusObject> supplyTemp = new ArrayList<EplusObject>();

	for (EplusObject eo : supplySideSystemTemplate) {
	    EplusObject temp = eo.clone();

	    /*
	     * replace the special characters that contains floors
	     */
	    if (temp.hasSpecialCharacters()) {
		temp.replaceSpecialCharacters(floor);
	    }

	    /*
	     * find the name of the coils' branch for plant connection purpose.
	     */
	    if (temp.getObjectName().equals("Branch")) {
		String name = temp.getKeyValuePair(0).getValue();
		if (name.contains("Cooling Coil")) {
		    systemCoolingCoilList.add(name);
		}
	    }

	    // check if this is the connection between supply side and demand
	    // side systems
	    if (temp.getObjectName().equalsIgnoreCase(
		    "AirLoopHVAC:ZoneSplitter")) {
		for (String s : zoneSplitterList) {
		    KeyValuePair splitterPair = new KeyValuePair(
			    "Outlet Node Name", s);
		    temp.addField(splitterPair);
		}
	    }

	    // check if this is the connection between supply side and demand
	    // side systems
	    if (temp.getObjectName().equalsIgnoreCase("AirLoopHVAC:ZoneMixer")) {
		for (String s : zoneMixerList) {
		    KeyValuePair mixerPair = new KeyValuePair(
			    "Intlet Node Name", s);
		    temp.addField(mixerPair);
		}
	    }
	    supplyTemp.add(temp);
	}
	return supplyTemp;
    }  
    
    /**
     * process the HVAC plant side system
     * 
     * @param plantSideTemp
     * @return
     */
    private ArrayList<EplusObject> processPlantTemp(
	    ArrayList<EplusObject> plantSideTemp) {
	double coolingLoad = building.getTotalCoolingLoad();// G3.1.3.7

	// calculate the number of chillers
	numberOfChiller = HVACSystemImplUtil.chillerNumberCalculation(
		coolingLoad, building.getTotalFloorArea());

	System.out.println("We Found " + numberOfChiller + " Chillers");

	ArrayList<EplusObject> plantTemp = new ArrayList<EplusObject>();

	// we use iterator because we will delete some objects in this loop
	// (pumps)
	Iterator<EplusObject> eoIterator = plantSideTemp.iterator();
	while (eoIterator.hasNext()) {
	    EplusObject temp = eoIterator.next().clone();

	    // choose chilled water pumps
	    if (temp.getKeyValuePair(0).getValue()
		    .equals("Chilled Water Loop ChW Secondary Pump")) {
		if (coolingLoad < coolingLoadThreshold) {
		    // smaller than threshold, remove the variable speed
		    if (temp.getObjectName().equalsIgnoreCase(
			    "HeaderedPumps:VariableSpeed")) {
			eoIterator.remove();
			coolingPump = "HeaderedPumps:ConstantSpeed";
			continue;
		    }
		} else {
		    if (temp.getObjectName().equalsIgnoreCase(
			    "HeaderedPumps:ConstantSpeed")) {
			eoIterator.remove();
			continue;
		    }
		}
	    }

	    // update chilled water loop branch information
	    if (temp.getKeyValuePair(0).getValue()
		    .equals("Chilled Water Loop ChW Demand Inlet Branch")) {
		// this is the number of the component 1 object type in branch
		temp.getKeyValuePair(3).setValue(coolingPump);
	    }

	    if (numberOfChiller > 1) {
		plantTemp.addAll(processChillers(numberOfChiller, temp));
		// change the template to the real chiller name
		if (changedChiller) {
		    temp.replaceSpecialCharacters("Chiller1");
		}
		// decide the number of tower
		plantTemp.addAll(processTowers(temp));
		if (changedTower) {
		    temp.replaceSpecialCharacters("Tower1");
		}
	    }
	    plantTemp.add(temp);
	}
	return plantTemp;
    }
    
    /**
     * process the HVAC plant side system
     * 
     * @param plantSideTemp
     * @return
     */
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
	
	int roomCounter = 0;
	while(floorMapIterator.hasNext()){
	    zoneSplitterList.clear();
	    zoneMixerList.clear();
	    String floor = floorMapIterator.next();
	    // first process the demand side system and their connection to
	    // plant and supply side system
	    ArrayList<ThermalZone> zones = floorMap.get(floor);
	    for(ThermalZone zone: zones){
		demandSideSystem.addAll(processDemandTemp(zone.getFullName(),
			demandSideSystemTemplate));
		// add the outdoor air object for demand zone
		demandSideSystem.add(zone.getOutdoorAirObject());
		roomCounter++;
	    }
	    // then process the supply side system and their connections to
	    // plant
	    supplySideSystem.addAll(processSupplyTemp(floor,
		    supplySideSystemTemplate));
	}
	plantSystem.addAll(processPlantTemp(plantSystemTemplate));
	System.out.println("Counting the rooms: " + roomCounter);
	objectLists.put("Supply Side System", supplySideSystem);
	objectLists.put("Demand Side System", demandSideSystem);
	objectLists.put("Plant", plantSystem);
	System.out.println("Re-tunning the supply side system...");
	checkSupplySideSystem();
	System.out.println("Connect plans");
	processConnections();
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
	for (EplusObject eo : supplySystem) {
	    if (eo.getObjectName().equalsIgnoreCase("Controller:OutdoorAir")) {
		if (economizer > -1) {
		    HVACSystemImplUtil.economizer(eo, economizer);
		}
	    } else if (eo.getObjectName()
		    .equalsIgnoreCase("Fan:VariableVolume")) {
		// get the floor name
		String floor = eo.getKeyValuePair(0).getValue().split(" ")[0];
		HVACSystemImplUtil.updateFanPowerforSystem5To8(eo,
			building.getFloorMaximumFlowRate(floor));
	    }
	}
    }
    
    /**
     * Precondition, Chiller is processed and chillerList is not empty. process
     * towers and their branches.
     * 
     * @param temp
     * @param plantTemp
     */
    private ArrayList<EplusObject> processTowers(EplusObject temp) {
	ArrayList<EplusObject> tempList = new ArrayList<EplusObject>();

	String name = temp.getKeyValuePair(0).getValue();
	if (temp.getObjectName().equalsIgnoreCase("CoolingTower:TwoSpeed")) {
	    changedTower = true;
	    towerList.add("Tower1");
	    for (int i = 1; i < numberOfChiller; i++) {
		// System.out.println(chillerList.get(i));
		EplusObject anotherTower = temp.clone();
		String towerCount = i + 1 + "";
		String towerName = "Tower" + towerCount;
		anotherTower.replaceSpecialCharacters(towerName);
		towerList.add(towerName);
		tempList.add(anotherTower);
	    }
	} else if (name.equalsIgnoreCase("Tower% CndW Branch")
		|| name.equals("Tower% Cooling Tower Outdoor Air Inlet Node")) {
	    changedTower = true;
	    for (int i = 1; i < numberOfChiller; i++) {
		// System.out.println(chillerList.get(i));
		EplusObject anotherTower = temp.clone();
		String towerCount = i + 1 + "";
		String towerName = "Tower" + towerCount;
		anotherTower.replaceSpecialCharacters(towerName);
		tempList.add(anotherTower);
	    }
	} else {
	    changedTower = false;
	}
	return tempList;
    }

    /**
     * This method process chillers and their branches
     * 
     * @param coolingLoad
     * @param temp
     * @param plantTemp
     */
    private ArrayList<EplusObject> processChillers(int numberOfChiller,
	    EplusObject temp) {
	ArrayList<EplusObject> tempList = new ArrayList<EplusObject>();
	// insert Chiller 1 branch into the chiller list first
	String name = temp.getKeyValuePair(0).getValue();
	if (temp.getObjectName().equalsIgnoreCase("Chiller:Electric:EIR")) {
	    chillerList.add("Chiller1");
	    changedChiller = true;
	    for (int i = 1; i < numberOfChiller; i++) {
		// clone the template
		EplusObject anotherChiller = temp.clone();
		String chillerCount = i + 1 + ""; // get the chiller counts
		// rename chiller
		String chillerName = "Chiller" + chillerCount;
		// fix the chiller name from the template
		anotherChiller.replaceSpecialCharacters(chillerName);
		// add the branch into the chiller list
		// System.out.println(chillerName);
		chillerList.add(chillerName);
		// add it to the plant plant temp list
		tempList.add(anotherChiller);
	    }
	} else if (name.equalsIgnoreCase("Chiller% ChW Branch")
		|| name.equalsIgnoreCase("Chiller% CndW Branch")) {
	    changedChiller = true;
	    for (int i = 1; i < numberOfChiller; i++) {
		EplusObject anotherBranch = temp.clone();
		String chillerCount = i + 1 + "";
		String chillerName = "Chiller" + chillerCount;
		anotherBranch.replaceSpecialCharacters(chillerName);
		tempList.add(anotherBranch);
	    }
	} else {
	    changedChiller = false;
	}
	return tempList;
    }
    
    /**
     * A method to process the system connections
     */
    private void processConnections() {
	ArrayList<EplusObject> plantSystem = objectLists.get("Plant");
	HVACSystemImplUtil.plantConnectionForSys8(plantSystem, chillerList,
		towerList, systemCoolingCoilList);
    }
}
