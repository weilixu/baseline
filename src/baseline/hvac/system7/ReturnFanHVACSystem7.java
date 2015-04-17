package baseline.hvac.system7;

import java.util.ArrayList;
import java.util.HashMap;

import baseline.generator.EplusObject;

public class ReturnFanHVACSystem7 implements SystemType7{
    //recording all the required data for HVAC system type 7
    private HashMap<String, ArrayList<EplusObject>> objectLists;
    
    private SystemType7 system;
    
    public ReturnFanHVACSystem7(SystemType7 sys){
	system = sys;
	objectLists = system.getSystemData();
	addReturnFanToSystem();
    }

    @Override
    public HashMap<String, ArrayList<EplusObject>> getSystemData() {
	return objectLists;
    }
    
    private void addReturnFanToSystem(){
	ArrayList<EplusObject> supplySystem = objectLists.get("Supply Side System");
	for(EplusObject eo: supplySystem){
	    if(eo.getObjectName().equalsIgnoreCase("CONTROLLER:OUTDOORAIR")){
		
	    }
	}
    }
    

}
