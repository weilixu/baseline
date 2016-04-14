package baseline.hvac.system1;

import java.util.ArrayList;
import java.util.HashMap;

import baseline.hvac.HVACSystem;
import baseline.hvac.SystemParser;
import baseline.idfdata.EplusObject;
import baseline.idfdata.building.EnergyPlusBuilding;

public class HVACSystem1Factory {
    
    //extract the template system
    private final SystemParser system = new SystemParser("System Type 1");
    
    private HashMap<String, ArrayList<EplusObject>> systemObjects;
    private SystemType1 systemType1;
    
    private EnergyPlusBuilding building;
    
    public HVACSystem1Factory(EnergyPlusBuilding building){
	systemObjects = new HashMap<String, ArrayList<EplusObject>>();
	this.building = building;
	processTemplate();
	systemType1 = new HVACSystem1(systemObjects, building);
    }
    
    public HVACSystem getSystem(){
	processSystem();
	return systemType1;
    }
    
    private void processSystem(){
	if(building.isDistrictHeat()&&!building.isDistrictCool()){
	    systemType1 = new DistrictHeatHVACSystem1(systemType1, building);
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
