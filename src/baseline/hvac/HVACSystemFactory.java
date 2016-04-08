package baseline.hvac;

import baseline.hvac.system1.HVACSystem1Factory;
import baseline.hvac.system3.HVACSystem3Factory;
import baseline.hvac.system4.HVACSystem4Factory;
import baseline.hvac.system5.HVACSystem5Factory;
import baseline.hvac.system6.HVACSystem6Factory;
import baseline.hvac.system7.HVACSystem7Factory;
import baseline.hvac.system8.HVACSystem8Factory;
import baseline.idfdata.building.EnergyPlusBuilding;

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
	}else if(systemType.equals("System Type 4")){
	    HVACSystem4Factory factory = new HVACSystem4Factory(building);
	    system = factory.getSystem();
	}else if(systemType.equals("System Type 6")){
	    HVACSystem6Factory factory = new HVACSystem6Factory(building);
	    system = factory.getSystem();
	}else if(systemType.equals("System Type 1")){
	    HVACSystem1Factory factory = new HVACSystem1Factory(building);
	    system = factory.getSystem();
	}
	    
	return system;
    }
}
