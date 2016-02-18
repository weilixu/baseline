package baseline.hvac.system5;

import java.util.ArrayList;
import java.util.HashMap;

import baseline.hvac.HVACSystem;
import baseline.idfdata.EplusObject;
import baseline.idfdata.building.EnergyPlusBuilding;

/**
 * In ASHRAE 90.1 2007 and 2010 standard, district heating source won't
 * change the setting for system type 5, therefore, this class will not
 * modify the original system struture. 
 * @author Weili
 *
 */
public class DistrictHeatHVACSystem5 implements SystemType5{
    
    private HashMap<String, ArrayList<EplusObject>> objectLists;
    
    private HVACSystem system;
    
    private final EnergyPlusBuilding building;
    
    public DistrictHeatHVACSystem5(SystemType5 sys, EnergyPlusBuilding bldg){
	system = sys;
	objectLists = system.getSystemData();
	building = bldg;
    }

    @Override
    public HashMap<String, ArrayList<EplusObject>> getSystemData() {
	return objectLists;
    }

}
