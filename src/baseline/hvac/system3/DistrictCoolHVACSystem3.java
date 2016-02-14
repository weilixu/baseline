package baseline.hvac.system3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import baseline.idfdata.EplusObject;
import baseline.idfdata.building.EnergyPlusBuilding;

/**
 * This class should not be implemented since the coil:cooling:water is not supported
 * in AirLoopHVAC:Unitary:Furnace:HeatCool object
 * This class modifies the standard ASHRAE HVAC System Type 3's cooling source
 * to district cooling. This involves two step contruction: First: remove the
 * original coil:cooling:DX:SingleSpeed object and its related objects Second: insert a
 * plant side system as cooling source
 * 
 * @author Weili
 *
 */
public class DistrictCoolHVACSystem3 implements SystemType3{
    // recording all the required data for HVAC system type 7
    private HashMap<String, ArrayList<EplusObject>> objectLists;

    private HashMap<String, ArrayList<EplusObject>> plantObjects;

    private SystemType3 system;

    private final EnergyPlusBuilding building;
    
    //threshold for determine the HVAC components.
    private String heatingPump = "HeaderedPumps:ConstantSpeed";
    
    public DistrictCoolHVACSystem3(SystemType3 sys, EnergyPlusBuilding bldg){
	system = sys;
	objectLists = system.getSystemData();
	building = bldg;
	
	//remove original cooling system
	removeCoolingSystem();
    }
    
    private void removeCoolingSystem(){
	ArrayList<EplusObject> supplySystem = objectLists
		.get("Supply Side System");
	Iterator<EplusObject> supplySystemIterator = supplySystem.iterator();
	while (supplySystemIterator.hasNext()){
	    EplusObject eo = supplySystemIterator.next();
	    //delete the original cooling coil
	    if(eo.getObjectName().equals("Coil:Cooling:DX:SingleSpeed")){
		supplySystemIterator.remove();
	    }
	}
    }
    
    
    @Override
    public HashMap<String, ArrayList<EplusObject>> getSystemData() {
	// TODO Auto-generated method stub
	return null;
    }

}
