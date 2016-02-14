package hvac.manufacturer;

import java.util.ArrayList;

import baseline.hvac.SystemParser;
import baseline.idfdata.EplusObject;

public class DistrictCoolingPlant {
    private static final String FILE_NAME = "hvaccomp.xml";
    
    private ArrayList<EplusObject> objects;
    
    public DistrictCoolingPlant(){
	SystemParser parser = new SystemParser("DistrictCool", FILE_NAME);
	objects = parser.getSystem();
    }
    
    public ArrayList<EplusObject> getObject(){
	return objects;
    }
}
