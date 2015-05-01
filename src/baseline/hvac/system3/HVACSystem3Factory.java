package baseline.hvac.system3;

import java.util.ArrayList;
import java.util.HashMap;

import baseline.generator.EplusObject;
import baseline.hvac.SystemParser;
import baseline.idfdata.EnergyPlusBuilding;

public class HVACSystem3Factory {
    //extract the template system
    private final SystemParser system = new SystemParser("System Type 3");
    
    private HashMap<String,ArrayList<EplusObject>> systemObjects;
    private SystemType3 systemType3;
    
    private EnergyPlusBuilding building;
    
    public HVACSystem3Factory(EnergyPlusBuilding building){
	systemObjects = new HashMap<String,ArrayList<EplusObject>>();
	this.building = building;
	systemType3 = new HVACSystem3(systemObjects, building);
    }
    

}
