package baseline.hvac.system3;

import java.util.ArrayList;
import java.util.HashMap;

import baseline.generator.EplusObject;
import baseline.idfdata.EnergyPlusBuilding;

public class HVACSystem3 implements SystemType3{
    //recording all the required data for HVAC system type 3
    private HashMap<String, ArrayList<EplusObject>> objectLists;
    
    //building object contains building information and energyplus data
    private EnergyPlusBuilding building;
    
    public HVACSystem3(HashMap<String, ArrayList<EplusObject>> objects, EnergyPlusBuilding bldg){
	objectLists = objects;
	building = bldg;
    }

    @Override
    public HashMap<String, ArrayList<EplusObject>> getSystemData() {
	// TODO Auto-generated method stub
	return null;
    }

}
