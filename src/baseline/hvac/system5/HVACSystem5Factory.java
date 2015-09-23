package baseline.hvac.system5;

import java.util.ArrayList;
import java.util.HashMap;

import baseline.generator.EplusObject;
import baseline.hvac.SystemParser;
import baseline.idfdata.EnergyPlusBuilding;

public class HVACSystem5Factory {
    //extract the template system
    private final SystemParser system = new SystemParser("System Type 5");
    
    private HashMap<String,ArrayList<EplusObject>> systemObjects;
    
    private SystemType5 systemType5;
    
    private EnergyPlusBuilding building;
    
    public HVACSystem5Factory(EnergyPlusBuilding building){
	systemObjects = new HashMap<String, ArrayList<EplusObject>>();
	this.building = building;
	processTemplate();
	systemType5 = new HVACSystem5(systemObjects, building);
    }
    
    /**
     * Control the creation of the system type 7
     * @return
     */
    public SystemType5 getSystem(){
	processSystem();
	return systemType5;
    }
    
    private void processSystem(){
	//judge if it is purchased heat or not
	if(building.getBaselineModel().getObjectList("DistrictHeating")!=null){
	    
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
