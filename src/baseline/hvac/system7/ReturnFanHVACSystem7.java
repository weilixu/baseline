package baseline.hvac.system7;

import java.util.ArrayList;
import java.util.HashMap;

import baseline.generator.EplusObject;
import baseline.idfdata.EnergyPlusBuilding;

public class ReturnFanHVACSystem7 implements SystemType7{
    //recording all the required data for HVAC system type 7
    private HashMap<String, ArrayList<EplusObject>> objectLists;
    
    private SystemType7 system;
    
    private final EnergyPlusBuilding building;
    
    private HashMap<String, Double> returnFanFlowMap;
    
    public ReturnFanHVACSystem7(SystemType7 sys, EnergyPlusBuilding bldg){
	system = sys;
	objectLists = system.getSystemData();
	building = bldg;
	constructFlowMap();
	addReturnFanToSystem();
    }

    @Override
    public HashMap<String, ArrayList<EplusObject>> getSystemData() {
	return objectLists;
    }
    
    private void constructFlowMap(){
	
    }
    
    private void addReturnFanToSystem(){
	ArrayList<EplusObject> supplySystem = objectLists.get("Supply Side System");
	for(EplusObject eo: supplySystem){
	    if(eo.getObjectName().equalsIgnoreCase("CONTROLLER:OUTDOORAIR")){
		
	    }
	}
    }
    

}
