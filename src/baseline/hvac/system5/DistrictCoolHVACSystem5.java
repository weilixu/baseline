package baseline.hvac.system5;

import java.util.ArrayList;
import java.util.HashMap;

import baseline.hvac.HVACSystem;
import baseline.hvac.system7.HVACSystem7Factory;
import baseline.idfdata.EplusObject;
import baseline.idfdata.building.EnergyPlusBuilding;

/**
 * If building qualifies hvac system type 5 and this building has district cooling system
 * the system will be changed to system type 7 according to ASHRAE 90.1 2010 G3.1.1.3.2
 * @author Weili
 *
 */
public class DistrictCoolHVACSystem5 implements SystemType5{
    
    private HVACSystem system;
    
    public DistrictCoolHVACSystem5(EnergyPlusBuilding bldg){
	HVACSystem7Factory factory = new HVACSystem7Factory(bldg);
	
	system = factory.getSystem();
    }
    
    @Override
    public HashMap<String, ArrayList<EplusObject>> getSystemData() {
	return system.getSystemData();
    }

}
