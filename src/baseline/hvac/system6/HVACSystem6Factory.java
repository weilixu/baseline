package baseline.hvac.system6;

import java.util.ArrayList;
import java.util.HashMap;

import baseline.hvac.HVACSystem;
import baseline.hvac.SystemParser;
import baseline.idfdata.EplusObject;
import baseline.idfdata.building.EnergyPlusBuilding;

/**
 * This class represents HVAC System type 5 manufacturer
 * The class behaviors includes:
 * 1. Establish the template System Type 5
 * 	Template implemented clauses: G3.1.2.1 (Not fully implemented), G3.1.2.2,G3.1.2.5, G3.1.3.12, G3.1.3.13, G3.1.3.15
 * 2. Check Clauses for components modifications:
 * 	G3.1.2.4 (Not implemented); G3.1.2.5; G3.1.2.7;G3.1.2.8;G3.1.2.9 (Not completed);
 * 	G3.1.2.11 (Not implemented yet)
 * 	G3.1.3.2; G3.1.3.3; G3.1.3.4; G3.1.3.5;
 * 3. Check exceptions includes:
 * 	G3.1.1 (not implemented); G3.1.1.1 (Not implemented); G3.1.1.2 (Not implemented)
 * 	G3.1.1.3 (nOT implemented)
 * 4. Manufactured correct system type 5 based on design case and merge it back to the whole building
 *    energy model
 * @author Weili
 *
 */
public class HVACSystem6Factory {
    //extract the template system
    private final SystemParser system = new SystemParser("System Type 5");
    
    private HashMap<String, ArrayList<EplusObject>> systemObjects;
    
    private SystemType6 systemType6;
    
    private EnergyPlusBuilding building;
    
    public HVACSystem6Factory(EnergyPlusBuilding building){
	systemObjects = new HashMap<String, ArrayList<EplusObject>>();
	this.building = building;
	processTemplate();
	systemType6 = new HVACSystem6(systemObjects, building);
    }
    
    public HVACSystem getSystem(){
	processSystem();
	return systemType6;
    }
    
    private void processSystem(){
	if(building.isDistrictHeat()){
	    
	}else if(building.hasReturnFan()){
	    
	}
    }
    
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
