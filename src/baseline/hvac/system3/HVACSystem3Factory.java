package baseline.hvac.system3;

import java.util.ArrayList;
import java.util.HashMap;

import baseline.hvac.HVACSystem;
import baseline.hvac.SystemParser;
import baseline.idfdata.EplusObject;
import baseline.idfdata.building.EnergyPlusBuilding;
/**
 * This class represent HVAC system type 3 manufacturer
 * The class behaviors includes
 * 1. Establish the template System Type 3
 * 2. Check Clauses for components modifications:
 * 	G3.1.2.2; G3.1.2.4 (Not implemented); G3.1.2.5; G3.1.2.7;G3.1.2.8;G3.1.2.9(Not completed);
 * 	G3.1.2.11(Not implemented yet)
 * 3. Check exceptions includes
 * 	G3.1.1; G3.1.1.1 (Not implemented); G3.1.1.2 (Not implemented)
 * 	G3.1.1.3 (nOT implemented)
 * 4. Manufacture correct system type 3 based on design case and merge it back to the whole building
 *    energy model
 * @author Weili
 *
 */

public class HVACSystem3Factory {
    //extract the template system
    private final SystemParser system = new SystemParser("System Type 3");
    
    private HashMap<String,ArrayList<EplusObject>> systemObjects;
    private SystemType3 systemType3;
    
    private EnergyPlusBuilding building;
    
    public HVACSystem3Factory(EnergyPlusBuilding building){
	systemObjects = new HashMap<String,ArrayList<EplusObject>>();
	this.building = building;
	processTemplate();
	systemType3 = new HVACSystem3(systemObjects, building);
	
    }
    
    public HVACSystem getSystem(){
	processSystem();
	return systemType3;
    }
    
    private void processSystem(){
	if(building.isDistrictHeat() && !building.isDistrictCool()){
	    systemType3 = new DistrictHeatHVACSystem3(systemType3, building);
	}else if(building.isDistrictCool() && !building.isDistrictHeat()){
	    systemType3 = new DistrictCoolHVACSystem3(systemType3, building);
	}else if(building.isDistrictCool() && building.isDistrictHeat()){
	    systemType3 = new DistrictHeatCoolSystem3(systemType3, building);
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
