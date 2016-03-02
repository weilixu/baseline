package baseline.hvac.system6;

import java.util.ArrayList;
import java.util.HashMap;

import baseline.hvac.HVACSystem;
import baseline.hvac.system8.HVACSystem8Factory;
import baseline.idfdata.EplusObject;
import baseline.idfdata.building.EnergyPlusBuilding;

/**
 * If building qualifies hvac system type 6 and this building has district cooling system
 * the system will be changed to system type 8 according to ASHRAE 90.1 2010 G3.1.1.3.2
 * @author Weili
 *
 */
public class DistrictCoolHVACSystem6 implements SystemType6{
    
    private HVACSystem system;
    
    public DistrictCoolHVACSystem6(EnergyPlusBuilding bldg){
	HVACSystem8Factory factory = new HVACSystem8Factory(bldg);
	
	system = factory.getSystem();
    }

    @Override
    public HashMap<String, ArrayList<EplusObject>> getSystemData() {
	return system.getSystemData();
    }
}
