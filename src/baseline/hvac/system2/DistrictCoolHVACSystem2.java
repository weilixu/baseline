package baseline.hvac.system2;

import java.util.ArrayList;
import java.util.HashMap;

import baseline.hvac.HVACSystem;
import baseline.hvac.system1.HVACSystem1Factory;
import baseline.idfdata.EplusObject;
import baseline.idfdata.building.EnergyPlusBuilding;

/**
 * Under this condition, system type 2 will be same as system type 1
 * so this class refers to the code in the system type 1
 * which abandon the previous creation of the system type 2,
 * and recreate the system type 1 with district cooling
 * @author Weili
 *
 */
@SuppressWarnings("unused")
public class DistrictCoolHVACSystem2 implements SystemType2{
    
    private HVACSystem system;
    
    private final EnergyPlusBuilding building;

    // threshold for determine the HVAC components.
	private String coolingPump = "HeaderedPumps:ConstantSpeed";
    
    public DistrictCoolHVACSystem2(SystemType2 sys, EnergyPlusBuilding bldg){
	system = sys;
	building = bldg;
	HVACSystem1Factory factory = new HVACSystem1Factory(building);
	system = factory.getSystem();
    }

    @Override
    public HashMap<String, ArrayList<EplusObject>> getSystemData() {
	return system.getSystemData();
    }
    

}
