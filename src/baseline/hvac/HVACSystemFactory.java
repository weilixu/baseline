package baseline.hvac;

import baseline.hvac.system3.HVACSystem3Factory;
import baseline.hvac.system5.HVACSystem5Factory;
import baseline.hvac.system7.HVACSystem7Factory;
import baseline.hvac.system8.HVACSystem8Factory;
import baseline.idfdata.EnergyPlusBuilding;

public class HVACSystemFactory {
    private String systemType;
    
    private HVACSystem system;
    private EnergyPlusBuilding building;
    
    public HVACSystemFactory(String system, EnergyPlusBuilding bldg){
	systemType = system;
	building = bldg;
    }
    
    /**
     * create the HVAC system based on system types
     * @return
     */
    public HVACSystem createSystem(){
	if(systemType.equals("System Type 7")){
	    HVACSystem7Factory factory = new HVACSystem7Factory(building);
	    system = factory.getSystem();
	}else if(systemType.equals("System Type 3")){
	    HVACSystem3Factory factory = new HVACSystem3Factory(building);
	    system = factory.getSystem();
	}else if(systemType.equals("System Type 5")){
	    HVACSystem5Factory factory = new HVACSystem5Factory(building);
	    system = factory.getSystem();
	}else if(systemType.equals("System Type 8")){
	    HVACSystem8Factory factory = new HVACSystem8Factory(building);
	    system = factory.getSystem();
	}
	    
	return system;
    }
}
