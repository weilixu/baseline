package baseline.hvac;

import java.util.ArrayList;
import java.util.HashMap;

import baseline.generator.EplusObject;

/**
 * This interface represents an HVAC System in the EnergyPlus
 * @author Weili
 *
 */
public interface HVACSystem {
    public HashMap<String, ArrayList<EplusObject>> getSystemData();

}
