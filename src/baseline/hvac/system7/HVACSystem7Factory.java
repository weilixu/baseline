package baseline.hvac.system7;

import java.util.ArrayList;
import java.util.HashMap;

import baseline.generator.EplusObject;
import baseline.hvac.SystemParser;
import baseline.idfdata.EnergyPlusBuilding;

/**
 * This class represents HVAC System type 7 manufacturer
 * The class behaviors includes:
 * 1. Establish the template System Type 7
 * 2. Check Clauses for components modifications:
 * 	G3.1.2.2; G3.1.2.4 (Not implemented); G3.1.2.5; G3.1.2.7;G3.1.2.8;G3.1.2.9 (Not completed);
 * 	G3.1.2.11 (Not implemented yet)
 * 	G3.1.3.2; G3.1.3.3; G3.1.3.4; G3.1.3.5;G3.1.3.7;G3.1.3.8;G3.1.3.9;
 * 	G3.1.3.10;G3.1.3.11;G3.1.3.12;G3.1.3.13;G3.1.3.15
 * 3. Check exceptions includes:
 * 	G3.1.1 (not implemented); G3.1.1.1 (Not implemented); G3.1.1.2 (Not implemented)
 * 	G3.1.1.3 (nOT implemented)
 * 4. Manufactured correct system type 7 based on design case and merge it back to the whole building
 *    energy model
 * @author Weili
 *
 */
public class HVACSystem7Factory {
    //extract the template system
    private final SystemParser system = new SystemParser("System Type 7");
    
    private HashMap<String,ArrayList<EplusObject>> systemObjects;
    
    private SystemType7 systemType7;
    
    private EnergyPlusBuilding building;
    
    public HVACSystem7Factory(EnergyPlusBuilding building){
	systemObjects = new HashMap<String,ArrayList<EplusObject>>();
	this.building = building;
	processTemplate();
	systemType7 = new HVACSystem7(systemObjects,building);
    }
    
    /**
     * Control the creation of the system type 7
     * @return
     */
    public SystemType7 getSystem(){
	processSystem();
	return systemType7;
    }
    
    private void processSystem(){
	//judge if it is purchased heat or not
	if(building.getBaselineModel().getObjectList("DistrictHeating")!=null){
	    
	}else if(building.hasReturnFan()){
	    systemType7 = new ReturnFanHVACSystem7(systemType7,building);
	}
    }
    
    /**
     * Separate the three systems into three data lists.
     */
    private void processTemplate(){
	ArrayList<EplusObject> template = system.getSystem();
	for(EplusObject eo: template){	    
	    if(eo.getReference().equals("Supply Side System")){
		if(!systemObjects.containsKey("Supply Side System")){
		    systemObjects.put("Supply Side System", new ArrayList<EplusObject>());
		}
		systemObjects.get("Supply Side System").add(eo);
	    }else if(eo.getReference().equals("Demand Side System")){
		if(!systemObjects.containsKey("Demand Side System")){
		    systemObjects.put("Demand Side System", new ArrayList<EplusObject>());
		}
		systemObjects.get("Demand Side System").add(eo);
	    }else if(eo.getReference().equals("Plant")){
		if(!systemObjects.containsKey("Plant")){
		    systemObjects.put("Plant", new ArrayList<EplusObject>());
		}
		systemObjects.get("Plant").add(eo);
	    }else if(eo.getReference().equals("Schedule")){
		if(!systemObjects.containsKey("Schedule")){
		    systemObjects.put("Schedule", new ArrayList<EplusObject>());
		}
		systemObjects.get("Schedule").add(eo);
	    }else if(eo.getReference().equals("Global")){
		if(!systemObjects.containsKey("Global")){
		    systemObjects.put("Global", new ArrayList<EplusObject>());
		}
		systemObjects.get("Global").add(eo);
	    }
	}
    }
}
