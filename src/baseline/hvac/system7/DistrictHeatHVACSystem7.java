package baseline.hvac.system7;

import java.util.ArrayList;
import java.util.HashMap;

import baseline.generator.EplusObject;

/**
 * decorator class, decorates the system with purchased heat system
 * @author Weili
 *
 */
public class DistrictHeatHVACSystem7 implements SystemType7{
    private SystemType7 system;
    
    public DistrictHeatHVACSystem7(SystemType7 system){
	this.system = system;
    }
    
    
    
    @Override
    public HashMap<String, ArrayList<EplusObject>> getSystemData() {
	
	return null;
    }
}
