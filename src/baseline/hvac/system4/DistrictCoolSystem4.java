package baseline.hvac.system4;

import java.util.ArrayList;
import java.util.HashMap;

import baseline.hvac.HVACSystem;
import baseline.hvac.system3.HVACSystem3Factory;
import baseline.idfdata.EplusObject;
import baseline.idfdata.building.EnergyPlusBuilding;

/**
 * This class modifies the standard ASHRAE HVAC System Type 4's cooling source
 * to district cooling. Because the current logic for changing Type 4's cooling source
 * to district cooling requires to modify the entire system to system type 3 with district cooling
 * system only, therefore, this class uses the hvac system 3 factory to manufacture system type 3 with
 * district cooling system only system.
 * 
 * @author Weili
 *
 */
public class DistrictCoolSystem4 implements SystemType4{
    
    private HVACSystem system;
    
    public DistrictCoolSystem4(EnergyPlusBuilding bldg){
	HVACSystem3Factory factory = new HVACSystem3Factory(bldg);
	system = factory.getSystem();
    }

    @Override
    public HashMap<String, ArrayList<EplusObject>> getSystemData() {
	return system.getSystemData();
    }
    

}
