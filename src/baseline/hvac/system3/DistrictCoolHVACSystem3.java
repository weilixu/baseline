package baseline.hvac.system3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import baseline.hvac.HVACSystem;
import baseline.hvac.HVACSystemImplUtil;
import baseline.idfdata.EplusObject;
import baseline.idfdata.building.EnergyPlusBuilding;
import hvac.manufacturer.Manufacturer;

/**
 * This class should not be implemented since the coil:cooling:water is not
 * supported in AirLoopHVAC:Unitary:Furnace:HeatCool object This class modifies
 * the standard ASHRAE HVAC System Type 3's cooling source to district cooling.
 * This involves two step contruction: First: remove the original
 * coil:cooling:DX:SingleSpeed object and its related objects Second: insert a
 * plant side system as cooling source
 * 
 * @author Weili
 *
 */
public class DistrictCoolHVACSystem3 implements SystemType3 {
    // recording all the required data for HVAC system type 3
    private HashMap<String, ArrayList<EplusObject>> objectLists;

    private HashMap<String, ArrayList<EplusObject>> plantObjects;

    private HVACSystem system;

    private final EnergyPlusBuilding building;

    private static final double coolingLoadThreshold = 10550558;// watt
    private final static int objectSizeLimit = 13; // differentiate the air loop
						   // branch with other branches
    // threshold for determine the HVAC components.
    private String coolingPump = "HeaderedPumps:ConstantSpeed";

    public DistrictCoolHVACSystem3(SystemType3 sys, EnergyPlusBuilding bldg) {
	system = sys;
	objectLists = system.getSystemData();
	building = bldg;

	// remove original cooling system
	removeCoolingSystem();

	// inquire district cool plant manufacturer
	plantObjects = new HashMap<String, ArrayList<EplusObject>>();
	processTemplate(Manufacturer.generateSystem("District Cool"));

	// insert systems
	insertDistrictCoolingSystem();

    }

    private void insertDistrictCoolingSystem() {
	// now we should have plant template, include supply system and plant
	// system,
	// lets deal with supply system first, also record the necessary parts
	// for future connection
	// with plant system
	ArrayList<EplusObject> supplySystem = plantObjects
		.get("Supply Side System");
	ArrayList<EplusObject> supplyTemp = new ArrayList<EplusObject>();

	// data use for plant system connection
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
	}

	plantObjects.put("Supply Side System", supplyTemp);
	// deal with plant connection
	double coolLoad = building.getTotalCoolingLoad();
	// G3.1.3.2
	ArrayList<EplusObject> plantSideTemp = plantObjects.get("Plant");
	ArrayList<EplusObject> plantTemp = new ArrayList<EplusObject>();
	// we use iterator because we will delete some objects in this loop
	// (pumps)
	Iterator<EplusObject> eoIterator = plantSideTemp.iterator();
	while (eoIterator.hasNext()) {
	    EplusObject temp = eoIterator.next().clone();
	    // select pumps from Templates based on the inputs
	    // choose chilled water loop pumps
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

	plantObjects.put("Plant", plantTemp);
    }

    private void removeCoolingSystem() {
	ArrayList<EplusObject> supplySystem = objectLists
		.get("Supply Side System");
	Iterator<EplusObject> supplySystemIterator = supplySystem.iterator();
	while (supplySystemIterator.hasNext()) {
	    EplusObject eo = supplySystemIterator.next();
	    // delete the original cooling coil
	    if (eo.getObjectName().equals("Coil:Cooling:DX:SingleSpeed")) {
		supplySystemIterator.remove();
	    } else if (eo.getObjectName().equals("CoilSystem:Cooling:DX")) {
		supplySystemIterator.remove();
	    } else if (eo.getObjectName().equals("Branch")
		    && eo.getSize() >= objectSizeLimit) {
		for (int i = 0; i < eo.getSize(); i++) {
		    if (eo.getKeyValuePair(i).getValue()
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
