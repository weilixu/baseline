package baseline.hvac.system1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import baseline.hvac.HVACSystem;
import baseline.hvac.HVACSystemImplUtil;
import baseline.idfdata.EplusObject;
import baseline.idfdata.building.EnergyPlusBuilding;
import baseline.hvac.manufacturer.Manufacturer;

public class DistrictCoolHVACSystem1 implements SystemType1 {

    // recoding all the required data for HVAC System type 1
    private HashMap<String, ArrayList<EplusObject>> objectLists;

    private HashMap<String, ArrayList<EplusObject>> fanCoilLists;

    private HashMap<String, ArrayList<EplusObject>> plantObjects;

    private HashMap<String, ArrayList<EplusObject>> combinedObjects;

    private HVACSystem system;

    private final EnergyPlusBuilding building;

    private static final double coolingLoadThreshold = 10550558;// watt

    // threshold for determine the HVAC components.
    private String coolingPump = "HeaderedPumps:ConstantSpeed";

    public DistrictCoolHVACSystem1(SystemType1 sys, EnergyPlusBuilding bldg) {
	system = sys;
	objectLists = system.getSystemData();
	building = bldg;

	// remove original systems
	removeCoolingSystem();

	// inquire fan coil system manufacturer
	fanCoilLists = processTemplate(
		Manufacturer.generateSystem("Fan Coil Unit"));

	// inquire district system manufacturer
	plantObjects = processTemplate(
		Manufacturer.generateSystem("District Cool"));

	// combine systems
	combineSystems();
	// insert the new system
	insertNewSystem();

    }

    @Override
    public HashMap<String, ArrayList<EplusObject>> getSystemData() {
	return objectLists;
    }

    /**
     * Remove the original packaged terminal air conditioner systems this
     * include remove: cooling coils, heating coils, fan, and connections
     */
    private void removeCoolingSystem() {
	ArrayList<EplusObject> demandSystem = objectLists
		.get("Demand Side System");
	Iterator<EplusObject> demandSystemIterator = demandSystem.iterator();
	while (demandSystemIterator.hasNext()) {
	    EplusObject eo = demandSystemIterator.next();
	    // delete the original terminal unit
	    if (eo.getObjectName()
		    .equals("ZoneHVAC:PackagedTerminalAirConditioner")) {
		demandSystemIterator.remove();
	    } else if (eo.getObjectName().equals("Fan:OnOff")) {
		demandSystemIterator.remove();
	    } else if (eo.getObjectName()
		    .equals("Coil:Cooling:DX:SingleSpeed")) {
		demandSystemIterator.remove();
	    } else if (eo.getObjectName().equals("Coil:Heating:Water")) {
		demandSystemIterator.remove();
	    } else if (eo.getObjectName().equals("ZoneHVAC:EquipmentList")) {
		eo.getKeyValuePair(1).setValue("ZoneHVAC:FourPipeFanCoil");
		String name = eo.getKeyValuePair(2).getValue();
		name = name.split(" ")[0] + " FC";
		eo.getKeyValuePair(2).setValue(name);
	    } else if (eo.getObjectName().equals("OutdoorAir:Mixer") || eo
		    .getObjectName().equals("ZoneHVAC:EquipmentConnections") ||
		    eo.getObjectName().equals("OutdoorAir:NodeList")) {
		for (int i = 0; i < eo.getSize(); i++) {
		    String field = eo.getKeyValuePair(i).getValue();
		    if (field.contains("PTAC")) {
			field = field.replace("PTAC", "FC");
			//System.out.println(field);
			eo.getKeyValuePair(i).setValue(field);
		    }
		}
	    }
	}
	ArrayList<EplusObject> supplySystem = objectLists
		.get("Supply Side System");
	Iterator<EplusObject> supplySystemIterator = supplySystem.iterator();
	while (supplySystemIterator.hasNext()){
	    EplusObject eo = supplySystemIterator.next();
	    if (eo.getObjectName()
		    .equals("OutdoorAir:NodeList")){
		    supplySystemIterator.remove();
	    }
	}
    }

    private void combineSystems() {
	// delete the airloop elements in district cool system
	ArrayList<EplusObject> plantSupplySystem = plantObjects
		.get("Supply Side System");
	Iterator<EplusObject> supplySystemIterator = plantSupplySystem
		.iterator();
	while (supplySystemIterator.hasNext()) {
	    EplusObject eo = supplySystemIterator.next();
	    // delete the components
	    if (eo.getObjectName().equals("Coil:Cooling:Water")) {
		supplySystemIterator.remove();
	    } else if (eo.getObjectName().equals("Controller:WaterCoil")) {
		supplySystemIterator.remove();
	    } else if (eo.getObjectName()
		    .equals("AirLoopHVAC:ControllerList")) {
		supplySystemIterator.remove();
	    }
	}
	// merge two database
	combinedObjects = new HashMap<String, ArrayList<EplusObject>>();
	combinedObjects.put("Supply Side System",
		plantObjects.get("Supply Side System"));
	combinedObjects.get("Supply Side System")
		.addAll(fanCoilLists.get("Demand Side System"));

	combinedObjects.put("Demand Side System",
		fanCoilLists.get("Demand Side System"));
	combinedObjects.put("Plant", plantObjects.get("Plant"));

	// general
	ArrayList<EplusObject> supplySystem = combinedObjects
		.get("Supply Side System");
	ArrayList<EplusObject> demandSystem = combinedObjects
		.get("Demand Side System");

	ArrayList<EplusObject> supplyTemp = new ArrayList<EplusObject>();
	ArrayList<EplusObject> demandTemp = new ArrayList<EplusObject>();

	ArrayList<String> zoneCoolCoilBranchList = new ArrayList<String>();

	for (int i = 0; i < building.getNumberOfZone(); i++) {
	    String zone = building.getZoneNamebyIndex(i);
	    for (EplusObject eo : supplySystem) {
		EplusObject cl = eo.clone();

		if (cl.hasSpecialCharacters()) {
		    cl.replaceSpecialCharacters(zone);
		}
		if (cl.getObjectName().equals("Branch")) {
		    String name = cl.getKeyValuePair(0).getValue();
		    zoneCoolCoilBranchList.add(name);
		}

		supplyTemp.add(cl);
	    }

	    for (EplusObject eo : demandSystem) {
		EplusObject cl = eo.clone();

		if (cl.hasSpecialCharacters()) {
		    cl.replaceSpecialCharacters(zone);
		}
		demandTemp.add(cl);
	    }
	}
	combinedObjects.put("Supply Side System", supplyTemp);
	combinedObjects.put("Demand Side System", demandTemp);
	// deal with plant connection
	double coolLoad = building.getTotalCoolingLoad();
	ArrayList<EplusObject> plantSideTemp = combinedObjects.get("Plant");
	ArrayList<EplusObject> plantTemp = new ArrayList<EplusObject>();
	// we use iterator because we will delete some objects in this loop
	Iterator<EplusObject> eoIterator = plantSideTemp.iterator();
	while (eoIterator.hasNext()) {
	    EplusObject temp = eoIterator.next().clone();
	    if (temp.getKeyValuePair(0).getValue()
		    .equals("Chilled Water Loop CHW Supply Pump")) {
		if (coolLoad <= coolingLoadThreshold) {
		    // smaller than threshold, remove the variable speed
		    if (temp.getObjectName()
			    .equalsIgnoreCase("HeaderedPumps:VariableSpeed")) {
			eoIterator.remove();
			continue;
		    }
		} else {
		    if (temp.getObjectName()
			    .equalsIgnoreCase("HeaderedPumps:ConstantSpeed")) {
			eoIterator.remove();
			coolingPump = "HeaderedPumps:VariableSpeed";
			continue;
		    }
		}
	    }
	    plantTemp.add(temp);
	}
	for (int j = 0; j < plantTemp.size(); j++) {
	    if (plantTemp.get(j).getKeyValuePair(0).getValue()
		    .equals("Chilled Water Loop CHW Supply Inlet Branch")) {
		plantTemp.get(j).getKeyValuePair(3).setValue(coolingPump);
	    }
	}
	HVACSystemImplUtil.plantConnectionForDistrictCooling(plantTemp,
		zoneCoolCoilBranchList);
	combinedObjects.put("Plant", plantTemp);
    }

    private void insertNewSystem() {
	objectLists.get("Supply Side System")
		.addAll(combinedObjects.get("Supply Side System"));

	if (objectLists.get("Plant") == null) {
	    // if correct, this line will never be executed because standard
	    // system 1 with district cooling has plant
	    objectLists.put("Plant", combinedObjects.get("Plant"));
	} else {
	    objectLists.get("Plant").addAll(combinedObjects.get("Plant"));
	}
    }

    /**
     * Separate the three systems into three data lists.
     */
    private HashMap<String, ArrayList<EplusObject>> processTemplate(
	    ArrayList<EplusObject> template) {
	HashMap<String, ArrayList<EplusObject>> map = new HashMap<String, ArrayList<EplusObject>>();
	for (EplusObject eo : template) {
	    // System.out.println(eo.getObjectName());
	    if (eo.getReference().equals("Supply Side System")) {
		if (!map.containsKey("Supply Side System")) {
		    map.put("Supply Side System", new ArrayList<EplusObject>());
		}
		map.get("Supply Side System").add(eo);
	    } else if (eo.getReference().equals("Demand Side System")) {
		if (!map.containsKey("Demand Side System")) {
		    map.put("Demand Side System", new ArrayList<EplusObject>());
		}
		map.get("Demand Side System").add(eo);
	    } else if (eo.getReference().equals("Plant")) {
		if (!map.containsKey("Plant")) {
		    map.put("Plant", new ArrayList<EplusObject>());
		}
		map.get("Plant").add(eo);
	    } else if (eo.getReference().equals("Schedule")) {
		if (!map.containsKey("Schedule")) {
		    map.put("Schedule", new ArrayList<EplusObject>());
		}
		map.get("Schedule").add(eo);
	    } else if (eo.getReference().equals("Global")) {
		if (!map.containsKey("Global")) {
		    map.put("Global", new ArrayList<EplusObject>());
		}
		map.get("Global").add(eo);
	    }
	}
	return map;
    }

}
