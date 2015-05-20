package baseline.hvac.system7;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import baseline.generator.EplusObject;
import baseline.generator.KeyValuePair;
import baseline.hvac.HVACSystemImplUtil;
import baseline.idfdata.EnergyPlusBuilding;
import baseline.idfdata.ThermalZone;

/**
 * The class creates the HVAC System 7. It provides the basic linkages among
 * three systems namely: Supply Side System, Demand Side System and Plant
 * 
 * @author Weili
 *
 */
public class HVACSystem7 implements SystemType7 {
    // recording all the required data for HVAC system type 7
    private HashMap<String, ArrayList<EplusObject>> objectLists;
    // building object contains building information and energyplus data
    private EnergyPlusBuilding building;

    // supply air connection list
    private ArrayList<String> zoneSplitterList;
    private ArrayList<String> zoneMixerList;

    // plant demand side list
    private ArrayList<String> systemCoolingCoilList;
    private ArrayList<String> systemHeatingCoilList;
    private ArrayList<String> zoneHeatingCoilList;

    // plant supply side list
    private ArrayList<String> boilerList;
    private ArrayList<String> chillerList;
    private ArrayList<String> towerList;

    // pump selection
    private String heatingPump;
    private String coolingPump;

    // flag indicates whether boiler, chiller or tower have been modified in the
    // chekcing system
    private boolean changedBoiler;
    private boolean changedChiller;
    private boolean changedTower;

    // threshold for determine the HVAC components.
    private static final double heatingFloorThreshold = 11150; // m2
    private static final double coolingLoadThreshold = 10550558;// watt

    public HVACSystem7(HashMap<String, ArrayList<EplusObject>> objects,
	    EnergyPlusBuilding bldg) {
	objectLists = objects;
	building = bldg;

	// Set-up all the data structures
	zoneSplitterList = new ArrayList<String>();
	zoneMixerList = new ArrayList<String>();
	systemCoolingCoilList = new ArrayList<String>();
	systemHeatingCoilList = new ArrayList<String>();
	zoneHeatingCoilList = new ArrayList<String>();
	boilerList = new ArrayList<String>();
	chillerList = new ArrayList<String>();
	towerList = new ArrayList<String>();
	// initialize pump name
	heatingPump = "HeaderedPumps:ConstantSpeed";
	coolingPump = "HeaderedPumps:ConstantSpeed";

	changedBoiler = false;
	changedChiller = false;
	changedTower = false;

	processSystems();
    }

    @Override
    public HashMap<String, ArrayList<EplusObject>> getSystemData() {
	return objectLists;
    }

    /**
     * process the HVAC plant side system
     * 
     * @param plantSideTemp
     * @return
     */
    private ArrayList<EplusObject> processPlantTemp(
	    ArrayList<EplusObject> plantSideTemp) {
	double floorArea = building.getConditionedFloorArea(); // G3.1.3.5,
							       // G3.1.3.2
	double coolingLoad = building.getTotalCoolingLoad();// G3.1.3.7

	// calculate the number of boilers
	int numberOfBoiler = HVACSystemImplUtil.boilerNumberCalculation(floorArea);
	System.out.println("We Found " + numberOfBoiler + "Boilers");

	// calculate the number of chillers
	int numberOfChiller = HVACSystemImplUtil
		    .chillerNumberCalculation(coolingLoad);;

	System.out.println("We Found " + numberOfChiller + " Chillers");

	ArrayList<EplusObject> plantTemp = new ArrayList<EplusObject>();

	// we use iterator because we will delete some objects in this loop
	// (pumps)
	Iterator<EplusObject> eoIterator = plantSideTemp.iterator();
	while (eoIterator.hasNext()) {
	    EplusObject temp = eoIterator.next().clone();

	    // select pumps from Templates based on the inputs
	    // choose hot water loop pumps
	    if (temp.getKeyValuePair(0).getValue()
		    .equals("Hot Water Loop HW Supply Pump")) {
		if (floorArea <= heatingFloorThreshold) {
		    // smaller than thresh hold, remove the variable speed
		    if (temp.getObjectName().equalsIgnoreCase(
			    "HeaderedPumps:VariableSpeed")) {
			eoIterator.remove();
			continue;
		    }
		} else {
		    if (temp.getObjectName().equalsIgnoreCase(
			    "HeaderedPumps:ConstantSpeed")) {
			eoIterator.remove();
			heatingPump = "HeaderedPumps:VariableSpeed";
			continue;
		    }
		}
	    }
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

	    // this should be remove to the next loop later update the hot water
	    // loop branch information
	    if (temp.getKeyValuePair(0).getValue()
		    .equals("Hot Water Loop HW Supply Inlet Branch")) {
		// this is the number of the component 1 object type in branch
		temp.getKeyValuePair(3).setValue(heatingPump);
	    }
	    // update chilled water loop branch information
	    if (temp.getKeyValuePair(0).getValue()
		    .equals("Chilled Water Loop ChW Demand Inlet Branch")) {
		// this is the number of the component 1 object type in branch
		temp.getKeyValuePair(3).setValue(coolingPump);
	    }

	    // decide the number of boiler. It possibly add more boilers
	    if (numberOfBoiler > 1) {
		plantTemp.addAll(processBoilers(temp));
		if (changedBoiler) {
		    temp.replaceSpecialCharacters("Boiler1");
		}
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
     * Precondition, Chiller is processed and chillerList is not empty. process
     * towers and their branches.
     * 
     * @param temp
     * @param plantTemp
     */
    private ArrayList<EplusObject> processTowers(EplusObject temp) {
	ArrayList<EplusObject> tempList = new ArrayList<EplusObject>();

	String name = temp.getKeyValuePair(0).getValue();
	if (temp.getObjectName().equalsIgnoreCase("CoolingTower:TwoSpeed")
		|| name.equalsIgnoreCase("Tower% CndW Branch")
		|| name.equals("Tower% Cooling Tower Outdoor Air Inlet Node")) {
	    changedTower = true;
	    towerList.add("Tower1");
	    for (int i = 1; i < chillerList.size(); i++) {
		EplusObject anotherTower = temp.clone();
		String towerCount = i + 1 + "";
		String towerName = "Tower" + towerCount;
		anotherTower.replaceSpecialCharacters(towerName);
		towerList.add(towerName);
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
	if (temp.getObjectName().equalsIgnoreCase("Chiller:Electric:EIR")
		|| name.equalsIgnoreCase("Chiller% ChW Branch")
		|| name.equalsIgnoreCase("Chiller% CndW Branch")) {
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
		chillerList.add(chillerName);
		// add it to the plant plant temp list
		tempList.add(anotherChiller);
	    }
	} else {
	    changedChiller = false;
	}
	return tempList;
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
	String zoneSplitter = zone + " Zone Equip Inlet";
	String zoneMixer = zone + " Return Outlet";
	// this is only for system type 7
	String reheatCoil = zone + " Reheat Coil HW Branch";

	// add the connection links to another data lists for later
	// processing
	zoneSplitterList.add(zoneSplitter);
	zoneMixerList.add(zoneMixer);
	zoneHeatingCoilList.add(reheatCoil);
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
		} else if (name.contains("Heating Coil")) {
		    systemHeatingCoilList.add(name);
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

    private void processSystems() {
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
	while (floorMapIterator.hasNext()) {
	    zoneSplitterList.clear();
	    zoneMixerList.clear();
	    String floor = floorMapIterator.next();
	    // first process the demand side system and their connection to
	    // plant and supply side system
	    ArrayList<ThermalZone> zones = floorMap.get(floor);
	    for (ThermalZone zone : zones) {
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
		//get the floor name
		String floor = eo.getKeyValuePair(0).getValue().split(" ")[0];
		HVACSystemImplUtil.updateFanPowerforSystem5To8(eo,
			building.getFloorMaximumFlowRate(floor));
	    }
	}
    }

    /**
     * A method to process the system connections
     */
    private void processConnections() {
	ArrayList<EplusObject> plantSystem = objectLists.get("Plant");
	HVACSystemImplUtil.plantConnection(plantSystem, chillerList, towerList,
		boilerList, systemCoolingCoilList, systemHeatingCoilList,
		zoneHeatingCoilList);
    }
}
