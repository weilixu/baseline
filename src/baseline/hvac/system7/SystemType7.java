package baseline.hvac.system7;

import java.util.ArrayList;
import java.util.HashMap;

import baseline.generator.EplusObject;
import baseline.hvac.HVACSystem;

/**
 * system type 7, all the changes and checks will happens here
 * @author Weili
 *
 */
public interface SystemType7 extends HVACSystem{
    
    public HashMap<String, ArrayList<EplusObject>> getSystemData();
    

}
