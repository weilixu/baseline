package hvac.manufacturer;

import baseline.generator.EplusObject;
import baseline.generator.KeyValuePair;

public final class Manufacturer {
    private static int branchComponent1ObjectTypeIndex = 3;
    private static int branchComponent1NameIndex = 4;
    private static int branchComponent1InletNodeIndex = 5;
    private static int branchComponent1OutletNodeIndex = 6;
    private static int branchComponent1ControlTypeIndex = 7;
    private static int branchComponent2ObjectTypeIndex = 8;
    private static int branchComponent2NameIndex = 9;
    private static int branchComponent2InletNodeIndex = 10;
    private static int branchComponent2OutletNodeIndex = 11;
    private static int branchComponent2ControlTypeIndex = 12;
    
    
    public static EplusObject generateObject(String object, String hvacName){
	if(object.equals("Return Fan")){
	    VariableVolumeReturnFan fan = new VariableVolumeReturnFan(hvacName,"Fan:VariableVolume","Supply Side System");
	    return fan.getObject();
	}
	return null;
    }
    
    public static EplusObject insertReturnFanToBranch(EplusObject eo, String hvacName){
	eo.insertFiled(branchComponent1ObjectTypeIndex, new KeyValuePair("Component 1 Object Type","Fan:VariableVolume"));
	eo.insertFiled(branchComponent1NameIndex, new KeyValuePair("Component 1 Name",hvacName + " Return Fan"));
	eo.insertFiled(branchComponent1InletNodeIndex, new KeyValuePair("Component 1 Inlet Node Name",hvacName + " Air Loop Inlet"));
	eo.insertFiled(branchComponent1OutletNodeIndex, new KeyValuePair("Component 1 Outlet Node Name",hvacName + " Return Fan Outlet"));
	eo.insertFiled(branchComponent1ControlTypeIndex, new KeyValuePair("Component 1 Branch Control Type","Passive"));
	KeyValuePair kvp = eo.getKeyValuePair(branchComponent2InletNodeIndex);
	kvp.setValue(hvacName + " Return Fan Outlet");
	return eo;
    }

}
