package baseline.hvac.manufacturer;

import baseline.idfdata.EplusObject;
import baseline.idfdata.KeyValuePair;

public class AirLoopHVACControllerList {
    private EplusObject object;
    private String name;
    
    public AirLoopHVACControllerList(String name, String obName, String obRef){
	object = new EplusObject(obName, obRef);
	this.name = name;
	assembleAirLoopHVACControllerList();

    }
    
    private void assembleAirLoopHVACControllerList(){
	object.addField(new KeyValuePair("Name",name+ " Controllers"));
    }
    
    public EplusObject getObject(){
	return object;
    }

}
