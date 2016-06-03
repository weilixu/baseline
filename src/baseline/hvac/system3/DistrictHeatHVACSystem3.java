package baseline.hvac.system3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import baseline.hvac.HVACSystem;
import baseline.hvac.HVACSystemImplUtil;
import baseline.idfdata.EplusObject;
import baseline.idfdata.building.EnergyPlusBuilding;
import baseline.hvac.manufacturer.Manufacturer;

/**
 * This class modifies the standard ASHRAE HVAC System Type 3's heating source
 * to district heating. This involves two step contruction: First: remove the
 * original coil:heating:gas object and its related objects Second: insert a
 * plant side system as heating source
 * 
 * @author Weili
 *
 */
public class DistrictHeatHVACSystem3 implements SystemType3 {
    // recording all the required data for HVAC system type 7
    private HashMap<String, ArrayList<EplusObject>> objectLists;

    private HashMap<String, ArrayList<EplusObject>> plantObjects;

    private HVACSystem system;

    private final EnergyPlusBuilding building;

    private final static int objectSizeLimit = 13; // differentiate the air loop
						   // branch with other branches

    // threshold for determine the HVAC components.
    private static final double heatingFloorThreshold = 11150; // m2
    private String heatingPump = "HeaderedPumps:ConstantSpeed";

    public DistrictHeatHVACSystem3(SystemType3 sys, EnergyPlusBuilding bldg) {
	system = sys;
	objectLists = system.getSystemData();
	building = bldg;

	// remove original heating system
	removeHeatingSystem();

	// inquire district heat plant manufacturer
	plantObjects = new HashMap<String, ArrayList<EplusObject>>();
	processTemplate(Manufacturer.generateSystem("District Heat"));

	// insert systems
	insertDistrictHeatingSystem();
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

    private void insertDistrictHeatingSystem() {
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
	
	for(int i=0; i<building.getNumberOfZone(); i++){
	    String zone = building.getZoneNamebyIndex(i);
	    for (EplusObject eo : supplySystem) {
		EplusObject cl = eo.clone();

		if (cl.hasSpecialCharacters()) {
		    cl.replaceSpecialCharacters(zone);
		}
		if (cl.getObjectName().equals("Branch")) {
		    String name = cl.getKeyValuePair(0).getValue();
		    zoneHeatCoilBranchList.add(name);
		}
		supplyTemp.add(cl);
	    }
	}

	plantObjects.put("Supply Side System", supplyTemp);

	// deal with plant connections
	double floorArea = building.getConditionedFloorArea(); // G3.1.3.5,
	// G3.1.3.2
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
	    }
	    plantTemp.add(temp);
	}
	for (int j = 0; j < plantTemp.size(); j++) {
	    if (plantTemp.get(j).getKeyValuePair(0).getValue()
		    .equals("Hot Water Loop HW Supply Inlet Branch")) {
		plantTemp.get(j).getKeyValuePair(3).setValue(heatingPump);
	    }
	}
	HVACSystemImplUtil.plantConnectionForDistrictHeating(plantTemp,
		zoneHeatCoilBranchList);

	plantObjects.put("Plant", plantTemp);
    }

    private void removeHeatingSystem() {
	ArrayList<EplusObject> supplySystem = objectLists
		.get("Supply Side System");
	Iterator<EplusObject> supplySystemIterator = supplySystem.iterator();
	while (supplySystemIterator.hasNext()) {
	    EplusObject eo = supplySystemIterator.next();
	    // delete the original heating coil
	    if (eo.getObjectName().equals("Coil:Heating:Gas")) {
		supplySystemIterator.remove();
	    } else if (eo.getObjectName().equals("Branch")
		    && eo.getSize() >= objectSizeLimit) {
		for (int i = 0; i < eo.getSize(); i++) {
		    if (eo.getKeyValuePair(i).getValue()
			    .equals("Coil:Heating:Gas")) {
			eo.getKeyValuePair(i).setValue("Coil:Heating:Water");
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

    /**
     * Separate the three systems into three data lists.
     */
    private void processTemplate(ArrayList<EplusObject> template) {
	for (EplusObject eo : template) {
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
