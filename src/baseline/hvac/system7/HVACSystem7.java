package baseline.hvac.system7;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import baseline.generator.EplusObject;
import baseline.generator.KeyValuePair;
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
    private HashMap<String, ArrayList<EplusObject>> objectLists;
    private EnergyPlusBuilding building;

    private ArrayList<String> zoneSplitterList;
    private ArrayList<String> zoneMixerList;

    private ArrayList<String> systemCoolingCoilList;
    private ArrayList<String> systemHeatingCoilList;

    private ArrayList<String> zoneHeatingCoilList;

    private ArrayList<String> boilerList;
    private ArrayList<String> chillerList;
    private ArrayList<String> towerList;

    private String heatingPump;
    private String coolingPump;

    private static final double heatingFloorThreshold = 11150; // m2
    private static final double heatingBoilerThreshold = 1393.55;// m2
    private static final double coolingLoadThreshold = 10550558;// watt
    private static final double coolingChillerSmallThreshold = 10550558;// watt
    private static final double coolingChillerLargeThreshold = 21101115;// watt

    public HVACSystem7(HashMap<String, ArrayList<EplusObject>> objects,
	    EnergyPlusBuilding bldg) {
	objectLists = objects;
	building = bldg;

	zoneSplitterList = new ArrayList<String>();
	zoneMixerList = new ArrayList<String>();
	systemCoolingCoilList = new ArrayList<String>();
	systemHeatingCoilList = new ArrayList<String>();
	zoneHeatingCoilList = new ArrayList<String>();
	boilerList = new ArrayList<String>();
	chillerList = new ArrayList<String>();

	heatingPump = "HeaderedPumps:ConstantSpeed";
	coolingPump = "HeaderedPumps:ConstantSpeed";

	processSystems();
    }

    @Override
    public HashMap<String, ArrayList<EplusObject>> getSystemData() {
	return objectLists;
    }

    private int chillerCalculator(double coolingLoad) {
	double singleChillerLimit = 28134820;// watt
	int numberOfChiller = 2; // minimum is two chillers;
	boolean converged = false;
	while (!converged) {
	    double singleChillerCapacity = coolingLoad / numberOfChiller;
	    if (singleChillerCapacity < singleChillerLimit) {
		converged = true;
	    } else {
		numberOfChiller++;
	    }
	}
	return numberOfChiller;
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
	int numberOfBoiler = 1;
	if (floorArea > heatingBoilerThreshold) {
	    numberOfBoiler = 2;
	}

	// calculate the number of chillers
	int numberOfChiller = 1;
	if (coolingLoad > coolingChillerSmallThreshold
		&& coolingLoad < coolingChillerLargeThreshold) {
	    numberOfChiller = 2;
	} else if (coolingLoad >= coolingChillerLargeThreshold) {
	    numberOfChiller = chillerCalculator(coolingLoad);
	}

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
		    }
		} else {
		    if (temp.getObjectName().equalsIgnoreCase(
			    "HeaderedPumps:ConstantSpeed")) {
			eoIterator.remove();
			heatingPump = "HeaderedPumps:VariableSpeed";
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
		    }
		} else {
		    if (temp.getObjectName().equalsIgnoreCase(
			    "HeaderedPumps:ConstantSpeed")) {
			eoIterator.remove();
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
		processBoilers(temp, plantTemp);
	    }

	    if (numberOfChiller > 1) {
		processChillers(numberOfChiller, temp, plantTemp);
		// decide the number of tower
		processTowers(temp, plantTemp);
	    }

	    processAirToPlantConnections(temp);
	    plantTemp.add(temp);
	}
	return plantTemp;
    }

    /**
     * Precondition, Chiller is processed and chillerList is not empty.
     * 
     * @param temp
     * @param plantTemp
     */
    private void processTowers(EplusObject temp,
	    ArrayList<EplusObject> plantTemp) {
	if (temp.getObjectName().equalsIgnoreCase("CoolingTower:TwoSpeed")) {
	    towerList.add("Tower1 CndW Branch");
	    for (int i = 1; i < chillerList.size(); i++) {
		EplusObject anotherTower = temp.clone();
		String towerCount = i + 1 + "";
		String towerName = "Tower"+towerCount;
		anotherTower.replaceSpecialCharacters(towerName);
		towerList.add(towerName + " CndW Branch");
		plantTemp.add(anotherTower);
	    }
	    temp.replaceSpecialCharacters("Tower1");
	}
    }

    /**
     * This method process chillers
     * 
     * @param coolingLoad
     * @param temp
     * @param plantTemp
     */
    private void processChillers(int numberOfChiller, EplusObject temp,
	    ArrayList<EplusObject> plantTemp) {
	// insert Chiller 1 branch into the chiller list first
	if (temp.getObjectName().equalsIgnoreCase("Chiller:Electric:EIR")) {
	    chillerList.add("Chiller1 ChW Branch");
	    for (int i = 1; i < numberOfChiller; i++) {
		// clone the template
		EplusObject anotherChiller = temp.clone();
		String chillerCount = i + 1 + ""; // get the chiller counts
		// rename chiller
		String chillerName = "Chiller" + chillerCount;
		// fix the chiller name from the template
		anotherChiller.replaceSpecialCharacters(chillerName);
		// add the branch into the chiller list
		chillerList.add(chillerName + " ChW Branch");
		// add it to the plant plant temp list
		plantTemp.add(anotherChiller);
	    }
	    // change the template to the real chiller name
	    temp.replaceSpecialCharacters("Chiller1");
	}
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
    private void processBoilers(EplusObject temp,
	    ArrayList<EplusObject> plantTemp) {
	if (temp.getObjectName().equalsIgnoreCase("Boiler:HotWater")) {
	    // if greater than the threshold, increase the number of boiler
	    // to 2
	    EplusObject anotherBoiler = temp.clone();
	    temp.replaceSpecialCharacters("Boiler1");
	    anotherBoiler.replaceSpecialCharacters("Boiler2");
	    plantTemp.add(anotherBoiler);
	    boilerList.add("Boiler1 HW Branch");
	    boilerList.add("Boiler2 HW Branch");
	}
    }

    /**
     * Write the branch elements in the plant, connecting the coils from supply
     * and demand system at air side
     * 
     * @param eo
     */
    private void processAirToPlantConnections(EplusObject eo) {
	if (eo.getObjectName().equalsIgnoreCase("Connector:Splitter")
		|| eo.getObjectName().equalsIgnoreCase("Connector:Mixer")) {
	    String name = eo.getKeyValuePair(0).getValue();
	    if (name.equals("Hot Water Loop HW Demand Splitter")
		    || name.equals("Hot Water Loop HW Demand Mixer")) {
		for (String s : systemHeatingCoilList) {
		    KeyValuePair newPair = new KeyValuePair("Outlet Branch", s);
		    eo.addField(newPair);
		}
		for (String s : zoneHeatingCoilList) {
		    KeyValuePair newPair = new KeyValuePair("Outlet Branch", s);
		    eo.addField(newPair);
		}
	    } else if (name.equals("Chilled Water Loop ChW Demand Splitter")
		    || name.equals("Chilled Water Loop ChW Demand Mixer")) {
		for (String s : systemCoolingCoilList) {
		    KeyValuePair newPair = new KeyValuePair("Outlet Branch", s);
		    eo.addField(newPair);
		}
	    }
	} else if (eo.getObjectName().equalsIgnoreCase("BranchList")) {
	    String branchName = eo.getKeyValuePair(0).getValue();
	    if (branchName.equals("Hot Water Loop HW Demand Side Branches")) {
		for (String s : zoneHeatingCoilList) {
		    KeyValuePair newPair = new KeyValuePair("Branch Name", s);
		    eo.insertFiled(2, newPair);
		}
		for (String s : systemHeatingCoilList) {
		    KeyValuePair newPair = new KeyValuePair("Branch Name", s);
		    eo.insertFiled(2, newPair);
		}
	    } else if (branchName
		    .equals("Chilled Water Loop ChW Demand Side Branches")) {
		for (String s : systemCoolingCoilList) {
		    KeyValuePair newPair = new KeyValuePair("Branch Name", s);
		    eo.insertFiled(2, newPair);
		}
	    }
	}
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
	}
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

	while (floorMapIterator.hasNext()) {
	    String floor = floorMapIterator.next();
	    // first process the demand side system and their connection to
	    // plant and supply side system
	    ArrayList<ThermalZone> zones = floorMap.get(floor);
	    for (ThermalZone zone : zones) {
		demandSideSystem.addAll(processDemandTemp(zone.getFullName(),
			demandSideSystemTemplate));
	    }
	    // then process the supply side system and their connections to
	    // plant
	    supplySideSystem.addAll(processSupplyTemp(floor,
		    supplySideSystemTemplate));
	}

	plantSystem.addAll(processPlantTemp(plantSystemTemplate));

	objectLists.put("Supply Side System", supplySideSystem);
	objectLists.put("Demand Side System", demandSideSystem);
	objectLists.put("Plant", plantSystem);
    }
}
