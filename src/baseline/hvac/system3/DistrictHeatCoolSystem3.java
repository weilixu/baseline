package baseline.hvac.system3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import baseline.hvac.HVACSystem;
import baseline.hvac.HVACSystemImplUtil;
import baseline.idfdata.EplusObject;
import baseline.idfdata.KeyValuePair;
import baseline.idfdata.building.EnergyPlusBuilding;
import baseline.hvac.manufacturer.Manufacturer;

/**
 * This class modifies the standard ASHRAE HVAC System Type 3's heating source
 * to district heating and cooling source to district cooling. This involves two
 * step constructions: First: remove the original coil:heating:gas object and
 * coil:cooling:dx:singlespeed object and its related objects Second: insert a
 * plant side system as heating source AND a plant side system as cooling source
 * 
 * @author Weili
 *
 */
public class DistrictHeatCoolSystem3 implements SystemType3 {
    // recording all the required data for HVAC system type 3
    private HashMap<String, ArrayList<EplusObject>> objectLists;
    private HashMap<String, ArrayList<EplusObject>> plantObjects;

    private HVACSystem system;

    private final EnergyPlusBuilding building;

    private static final double coolingLoadThreshold = 10550558;// watt
    private final static int objectSizeLimit = 13; // differentiate the air loop
						   // branch with other branches
    // threshold for determine the HVAC components.
    private static final double heatingFloorThreshold = 11150; // m2
    private String heatingPump = "HeaderedPumps:ConstantSpeed";
    // threshold for determine the HVAC components.
    private String coolingPump = "HeaderedPumps:ConstantSpeed";

    public DistrictHeatCoolSystem3(SystemType3 sys, EnergyPlusBuilding bldg) {
	system = sys;
	objectLists = system.getSystemData();
	building = bldg;

	// remove the old systems
	removeOldSystem();

	plantObjects = new HashMap<String, ArrayList<EplusObject>>();
	ArrayList<EplusObject> template = new ArrayList<EplusObject>();
	template.addAll(Manufacturer.generateSystem("District Heat"));
	template.addAll(Manufacturer.generateSystem("District Cool"));
	processTemplate(template);

	// insert systems
	insertDistrictSystems();

    }

    private void insertDistrictSystems() {
	// now we should have plant template, include supply system and plant
	// system,
	// lets deal with supply system first, also record the necessary parts
	// for future connection
	// with plant system
	ArrayList<EplusObject> supplySystem = plantObjects
		.get("Supply Side System");
	ArrayList<EplusObject> supplyTemp = new ArrayList<EplusObject>();
	// data use for plant system connection
	ArrayList<String> zoneHeatCoilBranchList = new ArrayList<String>();
	ArrayList<String> zoneCoolCoilBranchList = new ArrayList<String>();
	for (int i = 0; i < building.getNumberOfZone(); i++) {
	    String zone = building.getZoneNamebyIndex(i);
	    boolean builtController = false;
	    for (EplusObject eo : supplySystem) {
		EplusObject cl = null;
		if (eo.getObjectName().equals("AirLoopHVAC:ControllerList") && !builtController) {
		    cl = Manufacturer
			    .generateObject("AirLoopHVACControllerList", zone);
		    cl.addField(new KeyValuePair("Controller 1 Object Type",
			    "Controller:WaterCoil"));
		    cl.addField(new KeyValuePair("Controller 1 Name",
			    zone + " Cooling Coil Controller"));
		    cl.addField(new KeyValuePair("Controller 2 Object Type",
			    "Controller:WaterCoil"));
		    cl.addField(new KeyValuePair("Controller 2 Name",
			    zone + " Heating Coil Controller"));
		    builtController = true;
		} else if(eo.getObjectName().equals("AirLoopHVAC:ControllerList") && builtController){
		    continue;
		}else{
		    cl = eo.clone();

		    // System.out.println(cl.getObjectName());
		    if (cl.hasSpecialCharacters()) {
			cl.replaceSpecialCharacters(zone);
		    }
		    if (cl.getObjectName().equals("Branch")) {
			// need to distinguish heating coil with cooling coil
			// branch
			String name = cl.getKeyValuePair(0).getValue();
			if (name.contains("Heating")) {
			    zoneHeatCoilBranchList.add(name);
			} else {
			    zoneCoolCoilBranchList.add(name);
			}
		    }
		}
		supplyTemp.add(cl);
	    }
	}

	plantObjects.put("Supply Side System", supplyTemp);
	// deal with plant connections
	double floorArea = building.getConditionedFloorArea(); // G3.1.3.5,
	double coolLoad = building.getTotalCoolingLoad();

	ArrayList<EplusObject> plantSideTemp = plantObjects.get("Plant");
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
		    if (temp.getObjectName()
			    .equalsIgnoreCase("HeaderedPumps:VariableSpeed")) {
			eoIterator.remove();
			continue;
		    }
		} else {
		    if (temp.getObjectName()
			    .equalsIgnoreCase("HeaderedPumps:ConstantSpeed")) {
			eoIterator.remove();
			heatingPump = "HeaderedPumps:VariableSpeed";
			continue;
		    }
		}
	    } else if (temp.getKeyValuePair(0).getValue()
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
		    .equals("Hot Water Loop HW Supply Inlet Branch")) {
		plantTemp.get(j).getKeyValuePair(3).setValue(heatingPump);
	    } else if (plantTemp.get(j).getKeyValuePair(0).getValue()
		    .equals("Chilled Water Loop CHW Supply Inlet Branch")) {
		plantTemp.get(j).getKeyValuePair(3).setValue(coolingPump);
	    }
	}

	HVACSystemImplUtil.plantConnectionForDistrictHeating(plantTemp,
		zoneHeatCoilBranchList);
	HVACSystemImplUtil.plantConnectionForDistrictCooling(plantTemp,
		zoneCoolCoilBranchList);
	plantObjects.put("Plant", plantTemp);
    }

    private void removeOldSystem() {
	ArrayList<EplusObject> supplySystem = objectLists
		.get("Supply Side System");
	Iterator<EplusObject> supplySystemIterator = supplySystem.iterator();
	while (supplySystemIterator.hasNext()) {
	    EplusObject eo = supplySystemIterator.next();
	    if (eo.getObjectName().equals("Coil:Heating:Gas")
		    || eo.getObjectName().equals("Coil:Cooling:DX:SingleSpeed")
		    || eo.getObjectName().equals("CoilSystem:Cooling:DX")) {
		supplySystemIterator.remove();
	    } else if (eo.getObjectName().equals("Branch")
		    && eo.getSize() >= objectSizeLimit) {
		for (int i = 0; i < eo.getSize(); i++) {
		    if (eo.getKeyValuePair(i).getValue()
			    .equals("Coil:Heating:Gas")) {
			eo.getKeyValuePair(i).setValue("Coil:Heating:Water");
		    } else if (eo.getKeyValuePair(i).getValue()
			    .equals("CoilSystem:Cooling:DX")) {
			eo.getKeyValuePair(i).setValue("Coil:Cooling:Water");
		    }
		}
	    } else if (eo.getObjectName().equals("AirLoopHVAC")) {
		String zonename = eo.getKeyValuePair(0).getValue()
			.split(" ")[0];
		for (int i = 0; i < eo.getSize(); i++) {
		    if (eo.getKeyValuePair(i).getKey()
			    .equals("Controller List Name")) {
			eo.getKeyValuePair(i)
				.setValue(zonename + " Controllers");
		    }
		}
	    }
	}
    }

    @Override
    public HashMap<String, ArrayList<EplusObject>> getSystemData() {
	objectLists.get("Supply Side System")
		.addAll(plantObjects.get("Supply Side System"));
	if (objectLists.get("Plant") == null) {
	    objectLists.put("Plant", plantObjects.get("Plant"));
	} else {
	    // if correct, this line will never be executed because standard
	    // system 3 has no plant
	    objectLists.get("Plant").addAll(plantObjects.get("Plant"));
	}
	return objectLists;
    }

    /**
     * Separate the three systems into three data lists.
     */
    private void processTemplate(ArrayList<EplusObject> template) {
	for (EplusObject eo : template) {
	    // System.out.println(eo.getObjectName());
	    if (eo.getReference().equals("Supply Side System")) {
		if (!plantObjects.containsKey("Supply Side System")) {
		    plantObjects.put("Supply Side System",
			    new ArrayList<EplusObject>());
		}
		plantObjects.get("Supply Side System").add(eo);
	    } else if (eo.getReference().equals("Demand Side System")) {
		if (!plantObjects.containsKey("Demand Side System")) {
		    plantObjects.put("Demand Side System",
			    new ArrayList<EplusObject>());
		}
		plantObjects.get("Demand Side System").add(eo);
	    } else if (eo.getReference().equals("Plant")) {
		if (!plantObjects.containsKey("Plant")) {
		    plantObjects.put("Plant", new ArrayList<EplusObject>());
		}
		plantObjects.get("Plant").add(eo);
	    } else if (eo.getReference().equals("Schedule")) {
		if (!plantObjects.containsKey("Schedule")) {
		    plantObjects.put("Schedule", new ArrayList<EplusObject>());
		}
		plantObjects.get("Schedule").add(eo);
	    } else if (eo.getReference().equals("Global")) {
		if (!plantObjects.containsKey("Global")) {
		    plantObjects.put("Global", new ArrayList<EplusObject>());
		}
		plantObjects.get("Global").add(eo);
	    }
	}
    }

}
