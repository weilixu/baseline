package baseline.hvac.manufacturer;

import java.util.ArrayList;

import baseline.hvac.SystemParser;
import baseline.idfdata.EplusObject;

public class FourPipeFanCoilUnit {
    
    private static final String FILE_NAME = "hvaccomp.xml";
    
    private ArrayList<EplusObject> objects;
    
    public FourPipeFanCoilUnit(){
	SystemParser parser = new SystemParser("FanCoilUnit", FILE_NAME);
	objects = parser.getSystem();
	

    }
    
    public ArrayList<EplusObject> getObject(){
	return objects;
    }

}
