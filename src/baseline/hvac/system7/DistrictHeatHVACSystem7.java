package baseline.hvac.system7;

import java.util.ArrayList;
import java.util.HashMap;

import baseline.generator.EplusObject;
import baseline.idfdata.EnergyPlusBuilding;

/**
 * decorator class, decorates the system with purchased heat system
 * G3.1.1.3.1: If the proposed building design uses purchased heat, but does not use purchased chilled water,
 * then Table 3.1.1A and Table G3.1.1B shall be used to select the baseline HVAC system Type and purchased heat shall be substituted
 * for the Heating Type in Table G3.1.1B. The same heating source shall be used in the proposed and baseline building design.
 * 
 * The pump power shall be 14W/gpm.
 * @author Weili
 *
 */
public class DistrictHeatHVACSystem7 implements SystemType7{
    private SystemType7 system;
    // recording all the required data for HVAC system type 7
    private HashMap<String, ArrayList<EplusObject>> objectLists;
    private final EnergyPlusBuilding building;

    
    public DistrictHeatHVACSystem7(SystemType7 sys, EnergyPlusBuilding bldg){
	this.system = sys;
	objectLists = system.getSystemData();
	building = bldg;
	
    }
    
    @Override
    public HashMap<String, ArrayList<EplusObject>> getSystemData() {
	return objectLists;
	
    }
    
    
    
}
