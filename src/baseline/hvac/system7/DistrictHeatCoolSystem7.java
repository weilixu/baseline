package baseline.hvac.system7;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import baseline.hvac.HVACSystem;
import baseline.hvac.HVACSystemImplUtil;
import baseline.idfdata.EplusObject;
import baseline.idfdata.building.EnergyPlusBuilding;
import hvac.manufacturer.Manufacturer;

public class DistrictHeatCoolSystem7 implements SystemType7{
    // recording all the required data for HVAC system type 7
    private HashMap<String, ArrayList<EplusObject>> objectLists;
    private HashMap<String, ArrayList<EplusObject>> plantObjects;
    
    private HVACSystem system;

    private final EnergyPlusBuilding building;
    private static final double coolingLoadThreshold = 10550558;// watt
    //private final static int objectSizeLimit = 13; // differentiate the air loop
						   // branch with other branches
    // threshold for determine the HVAC components.
    private static final double heatingFloorThreshold = 11150; // m2
    private String heatingPump = "HeaderedPumps:ConstantSpeed";
    // threshold for determine the HVAC components.
    private String coolingPump = "HeaderedPumps:ConstantSpeed";
    
    
    public DistrictHeatCoolSystem7(SystemType7 sys, EnergyPlusBuilding bldg){
	system = sys;
	objectLists = system.getSystemData();
	building = bldg;
	
	// remove the old systems
	removeOldSystem();
	
	//process templates
	plantObjects = new HashMap<String, ArrayList<EplusObject>>();
	ArrayList<EplusObject> template = new ArrayList<EplusObject>();
	template.addAll(Manufacturer.generateSystem("District Heat"));
	template.addAll(Manufacturer.generateSystem("District Cool"));
	processTemplate(template);
	
	//insert district systems
	insertDistrictSystems();
	
    }

    @Override
    public HashMap<String, ArrayList<EplusObject>> getSystemData() {
	if (objectLists.get("Plant") == null) {
	    // if correct, this line will never be executed because standard
	    // system 7 has hot water plant
	    objectLists.put("Plant", plantObjects.get("Plant"));
	}else {
	    objectLists.get("Plant").addAll(plantObjects.get("Plant"));
	}
	return objectLists;
    }
    
    private void insertDistrictSystems(){
	// now we should have plant template, include supply system and plant
	// system,
	// lets deal with supply system first, also record the necessary parts
	// for future connection
	// with plant system
	// data use for plant system connection
	ArrayList<String> heatCoilBranchList = new ArrayList<String>();
	ArrayList<String> coolCoilBranchList = new ArrayList<String>();
	Set<String> floorName = building.getFloorMap().keySet();
	Iterator<String> floorIterator = floorName.iterator();
	while(floorIterator.hasNext()){
	    String floor = floorIterator.next();
	    coolCoilBranchList.add(floor + " Cooling Coil ChW Branch");
	    heatCoilBranchList.add(floor + " Heating Coil HW Branch");
	}
	
	for(int i=0; i<building.getNumberOfZone(); i++){
	    heatCoilBranchList.add(building.getZoneNamebyIndex(i) + " Reheat Coil HW Branch");
	}
	
	double coolLoad = building.getTotalCoolingLoad();
	double floorArea = building.getConditionedFloorArea(); // G3.1.3.5,
	
	ArrayList<EplusObject> plantTemp = new ArrayList<EplusObject>();
	ArrayList<EplusObject> plantSideTemp = plantObjects.get("Plant");
	
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
		heatCoilBranchList);
	HVACSystemImplUtil.plantConnectionForDistrictCooling(plantTemp,
		coolCoilBranchList);
	plantObjects.put("Plant", plantTemp);
    }
    
    private void removeOldSystem(){
	//remove all the plant elements
	objectLists.remove("Plant");
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
