package baseline.hvac.system1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import baseline.hvac.HVACSystem;
import baseline.hvac.HVACSystemImplUtil;
import baseline.hvac.manufacturer.Manufacturer;
import baseline.idfdata.EplusObject;
import baseline.idfdata.building.EnergyPlusBuilding;
/**
 * This class modifies the standard ASHRAE HVAC System Type 1's heating source
 * to district heating and cooling source to district cooling. In addition to this, 
 * the system will be changed to 4 pipe fan coil units. This involves two
 * step constructions: First: remove the original system and its related objects 
 * Second: insert a fan coil unit system.
 * Third: replace plant side system as heating source 
 * AND a plant side system as cooling source
 * 
 * @author Weili
 *
 */
public class DistrictHeatCoolSystem1 implements SystemType1{
    
    // recording all the required data for HVAC system type 1
    private HashMap<String, ArrayList<EplusObject>> objectLists;
    private HashMap<String, ArrayList<EplusObject>> plantObjects;
    
    private HVACSystem system;

    private final EnergyPlusBuilding building;
    
    private String heatingPump = "HeaderedPumps:ConstantSpeed";
    
    
    // threshold for determine the HVAC components.
    private static final double heatingFloorThreshold = 11150; // m2
    
    public DistrictHeatCoolSystem1(SystemType1 sys, EnergyPlusBuilding bldg){
	building = bldg;
	
	//first get the district cool hvac system
	DistrictCoolHVACSystem1 districtCool = new DistrictCoolHVACSystem1 (sys, bldg);
	system = districtCool;
	objectLists = system.getSystemData();
	
	//now remove the heating system on the district cool system
	removeHeatingSystem();
	
	// inquire district heat plant manufacturer
	plantObjects = new HashMap<String, ArrayList<EplusObject>>();
	processTemplate(Manufacturer.generateSystem("District Heat"));
	
	//replace the system with district heat system
	insertDistrictHeatingSystem();
    }
    
    private void insertDistrictHeatingSystem(){
	// now we should have plant template, include supply system and plant
	// system
	// The first thing is to add supply side cooling coils to the branch connections
	// data use for plant system connection
	ArrayList<String> heatCoilBranchList = new ArrayList<String>();
	
	for(int i=0; i<building.getNumberOfZone(); i++){
	    heatCoilBranchList.add(building.getZoneNamebyIndex(i) + " Heating Coil HW Branch");
	}
	
	ArrayList<EplusObject> plantTemp = new ArrayList<EplusObject>();
	ArrayList<EplusObject> plantSideTemp = plantObjects.get("Plant");
	
	// deal with plant connections
	double floorArea = building.getConditionedFloorArea(); // G3.1.3.5,
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
	HVACSystemImplUtil.plantConnectionForDistrictHeating(plantTemp, heatCoilBranchList);
	plantObjects.put("Plant", plantTemp);
    }
    
    private void removeHeatingSystem(){
	// extract the plant side system
	ArrayList<EplusObject> plantSystem = objectLists.get("Plant");
	Iterator<EplusObject> plantIterator = plantSystem.iterator();
	
	while(plantIterator.hasNext()){
	    EplusObject eo = plantIterator.next();
	  //remove plant sizing objects
	    if(eo.getObjectName().equals("Sizing:Plant")){
		if(eo.getKeyValuePair(0).getValue().startsWith("Hot Water")){
		    plantIterator.remove();
		}
	    }else if(eo.getObjectName().equals("Boiler:HotWater")||
		    eo.getObjectName().equals("PlantEquipmentOperation:HeatingLoad")){
		plantIterator.remove();
	    }else if(eo.getObjectName().equals("PlantLoop")||
		    eo.getObjectName().equals("PlantEquipmentList")||
		    eo.getObjectName().equals("PlantEquipmentOperationSchemes")||
		    eo.getObjectName().equals("SetpointManager:OutdoorAirReset")||
		    eo.getObjectName().equals("BranchList")||
		    eo.getObjectName().equals("Connector:Splitter")||
		    eo.getObjectName().equals("Connector:Mixer")||
		    eo.getObjectName().equals("ConnectorList")||
		    eo.getObjectName().equals("NodeList")||
		    eo.getObjectName().equals("Pipe:Adiabatic")||
		    eo.getObjectName().equals("HeaderedPumps:ConstantSpeed")||
		    eo.getObjectName().equals("HeaderedPumps:VariableSpeed")
		    ){
		if(eo.getKeyValuePair(0).getValue().startsWith("Hot Water")){
		    plantIterator.remove();
		}
	    }else if(eo.getObjectName().equals("Branch")){
		if(eo.getKeyValuePair(0).getValue().startsWith("Boiler")||
			eo.getKeyValuePair(0).getValue().startsWith("Hot Water")){
		    plantIterator.remove();
		}
	    }
	}
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
