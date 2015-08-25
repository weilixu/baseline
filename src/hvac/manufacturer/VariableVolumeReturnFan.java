package hvac.manufacturer;


import baseline.generator.EplusObject;
import baseline.generator.KeyValuePair;

public class VariableVolumeReturnFan {
    private EplusObject object;
    
    private final String returnNode = " Return Fan Outlet";
    private final String supplyNode = " Air Loop Inlet";
    private String hvacName;
    
    public VariableVolumeReturnFan(String name, String obName, String obRef){
	object = new EplusObject(obName, obRef);
	hvacName = name;
	assembleReturnFan();
    }
    
    private void assembleReturnFan(){
	object.addField(new KeyValuePair("Name",hvacName+ " Return Fan"));
	object.addField(new KeyValuePair("Availability Schedule Name","On"));
	object.addField(new KeyValuePair("Fan Total Efficiency","0.7"));
	object.addField(new KeyValuePair("Pressure Rise","2000"));
	object.addField(new KeyValuePair("Maximum Flow Rate","autosize"));
	object.addField(new KeyValuePair("Fan Power Minimum Flow Rate Input Method","Fraction"));
	object.addField(new KeyValuePair("Fan Power Minimum Flow Fraction","0"));
	object.addField(new KeyValuePair("Fan Power Minimum Air Flow Rate",""));
	object.addField(new KeyValuePair("Motor Efficiency","0.9"));
	object.addField(new KeyValuePair("Motor In Airstream Fraction","1"));
	object.addField(new KeyValuePair("Fan Power Coefficient 1","0.0013"));
	object.addField(new KeyValuePair("Fan Power Coefficient 2","0.147"));
	object.addField(new KeyValuePair("Fan Power Coefficient 3","0.9506"));
	object.addField(new KeyValuePair("Fan Power Coefficient 4","-0.0998"));
	object.addField(new KeyValuePair("Fan Power Coefficient 5","0"));
	object.addField(new KeyValuePair("Air Inlet Node Name",hvacName + supplyNode));
	object.addField(new KeyValuePair("Air Outlet Node Name",hvacName + returnNode));
    }
    
    public EplusObject getObject(){
	return object;
    }

}
