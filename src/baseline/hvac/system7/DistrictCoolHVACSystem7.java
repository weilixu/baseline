package baseline.hvac.system7;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import baseline.hvac.HVACSystem;
import baseline.hvac.HVACSystemImplUtil;
import baseline.idfdata.EplusObject;
import baseline.idfdata.building.EnergyPlusBuilding;
import baseline.hvac.manufacturer.Manufacturer;

public class DistrictCoolHVACSystem7 implements SystemType7{
    // recording all the required data for HVAC system type 7
    private HashMap<String, ArrayList<EplusObject>> objectLists;

    private HashMap<String, ArrayList<EplusObject>> plantObjects;

    private HVACSystem system;

    private final EnergyPlusBuilding building;
    
    private static final double coolingLoadThreshold = 10550558;// watt
    //private final static int objectSizeLimit = 13; // differentiate the air loop
    // branch with other branches
    // threshold for determine the HVAC components.
    private String coolingPump = "HeaderedPumps:ConstantSpeed";
    
    public DistrictCoolHVACSystem7(SystemType7 sys, EnergyPlusBuilding bldg){
	system = sys;
	objectLists = system.getSystemData();
	building = bldg;
	
	// remove original cooling system
	removeCoolingSystem();
	
	// inquire district cooling district plant manufacturer
	plantObjects = new HashMap<String, ArrayList<EplusObject>>();
	processTemplate(Manufacturer.generateSystem("District Cool"));
	
	// insert systems
	insertDistrictCoolingSystem();
	
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
    
    private void insertDistrictCoolingSystem(){
	// now we should have plant template, include supply system and plant
	// system
	// The first thing is to add supply side cooling coils to the branch connections
	// data use for plant system connection
	ArrayList<String> floorCoolCoilBranchList = new ArrayList<String>();
	Set<String> floorName = building.getFloorMap().keySet();
	Iterator<String> floorIterator = floorName.iterator();
	while(floorIterator.hasNext()){
	    String floor = floorIterator.next();
	    floorCoolCoilBranchList.add(floor + " Cooling Coil ChW Branch");
	}
	
	ArrayList<EplusObject> plantTemp = new ArrayList<EplusObject>();
	ArrayList<EplusObject> plantSideTemp = plantObjects.get("Plant");
	// deal with plant connection
	double coolLoad = building.getTotalCoolingLoad();
	// we use iterator because we will delete some objects in this loop
	// (pumps)
	Iterator<EplusObject> eoIterator = plantSideTemp.iterator();
	while(eoIterator.hasNext()){
	    EplusObject temp = eoIterator.next().clone();
	    if(temp.getKeyValuePair(0).getValue().equals("Chilled Water Loop CHW Supply Pump")){
		if(coolLoad <= coolingLoadThreshold){
		    if(temp.getObjectName().equalsIgnoreCase("HeaderedPumps:VariableSpeed")){
			eoIterator.remove();
			continue;
		    }
		}else{
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
		floorCoolCoilBranchList);
	plantObjects.put("Plant", plantTemp);
    }
    
    private void removeCoolingSystem(){
	// extract the plant side system
	ArrayList<EplusObject> plantSystem = objectLists.get("Plant");
	Iterator<EplusObject> plantIterator = plantSystem.iterator();
	
	while(plantIterator.hasNext()){
	    EplusObject eo = plantIterator.next();
	    //remove plant sizing objects
	    if(eo.getObjectName().equals("Sizing:Plant")){
		if(eo.getKeyValuePair(0).getValue().startsWith("Chilled") ||
			eo.getKeyValuePair(0).getValue().startsWith("Condenser")){
		    plantIterator.remove();
		}
	    }else if(eo.getObjectName().equals("Chiller:Electric:EIR") ||
		    eo.getObjectName().equals("CoolingTower:TwoSpeed")||
		    eo.getObjectName().equals("CondenserLoop")||
		    eo.getObjectName().equals("CondenserEquipmentList")||
		    eo.getObjectName().equals("CondenserEquipmentOperationSchemes")||
		    eo.getObjectName().equals("SetpointManager:FollowOutdoorAirTemperature")||
		    eo.getObjectName().equals("PlantEquipmentOperation:CoolingLoad")||
		    eo.getObjectName().equals("OutdoorAir:Node")){
		plantIterator.remove();
	    }else if(eo.getObjectName().equals("PlantLoop") ||
		    eo.getObjectName().equals("PlantEquipmentOperationSchemes")||
		    eo.getObjectName().equals("SetpointManager:OutdoorAirReset")||
		    eo.getObjectName().equals("BranchList")||
		    eo.getObjectName().equals("Connector:Splitter")||
		    eo.getObjectName().equals("Connector:Mixer")||
		    eo.getObjectName().equals("ConnectorList")||
		    eo.getObjectName().equals("NodeList")||
		    eo.getObjectName().equals("Pipe:Adiabatic")||
		    eo.getObjectName().equals("HeaderedPumps:ConstantSpeed")||
		    eo.getObjectName().equals("HeaderedPumps:VariableSpeed")||
		    eo.getObjectName().equals("PlantEquipmentList")||
		    eo.getObjectName().equals("SetpointManager:Scheduled")){
		if(eo.getKeyValuePair(0).getValue().startsWith("Chilled")){
		    plantIterator.remove();
		}
	    }else if(eo.getObjectName().equals("Branch")){
		if(eo.getKeyValuePair(0).getValue().startsWith("Chiller")||
			eo.getKeyValuePair(0).getValue().startsWith("Tower")||
			eo.getKeyValuePair(0).getValue().startsWith("Chilled")){
		    plantIterator.remove();
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
