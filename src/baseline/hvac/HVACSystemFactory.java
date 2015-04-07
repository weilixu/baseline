package baseline.hvac;

import baseline.hvac.system7.HVACSystem7Factory;
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
	}
	return system;
    }
}
