package baseline.hvac.manufacturer;

import java.util.ArrayList;

import baseline.hvac.SystemParser;
import baseline.idfdata.EplusObject;

/**
 * This class build a completely new district heating plant in EnergyPlus format
 * @author Weili
 *
 */
public class DistrictHeatingPlant {
    
    private static final String FILE_NAME = "hvaccomp.xml";

    private ArrayList<EplusObject> objects;
    
    public DistrictHeatingPlant(){
	SystemParser parser = new SystemParser("DistrctHeat",FILE_NAME);
	objects = parser.getSystem(); //get the entire plant system with district heating
	//System.out.println("3##############$%$#$%#$%#^$@%@#$%%" + objects.size());
    }
    
    public ArrayList<EplusObject> getObject(){
	return objects;
    }
}
