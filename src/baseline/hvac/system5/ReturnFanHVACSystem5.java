package baseline.hvac.system5;

import hvac.manufacturer.Manufacturer;

import java.util.ArrayList;
import java.util.HashMap;

import baseline.hvac.HVACSystemImplUtil;
import baseline.idfdata.EplusObject;
import baseline.idfdata.building.EnergyPlusBuilding;


/**
 *Add return fan to the systems. Once one return fan is found in the design case
 *this class will be initialized and insert return fan
 *to the system at every floor;
 *The return fan power is adjusted based on the ratio from design case
 *return fan power in baseline = return fan power in design case / fan power in design case
 *Supply fan will be adjusted too, pressure drop and motor efficiencies will be recalculated.
 *The connections requires to be modified as well. This is also implemented
 * 
 * @author Weili
 *
 */
public class ReturnFanHVACSystem5 implements SystemType5{
    // recording all the required data for HVAC system type 5
    private HashMap<String, ArrayList<EplusObject>> objectLists;

    private SystemType5 system;

    private final EnergyPlusBuilding building;
    //private HashMap<String, Double> returnFanFlowMap;

    // modify indexes
    private final int outdoorAirControllerIndex = 2;
    private final int outdoorAirMixer = 4;
    
    public ReturnFanHVACSystem5(SystemType5 sys, EnergyPlusBuilding bldg){
	system = sys;
	objectLists = system.getSystemData();
	building = bldg;
	addReturnFanToSystem();
    }
    
    @Override
    public HashMap<String, ArrayList<EplusObject>> getSystemData() {
	return objectLists;
    }
    
    private void addReturnFanToSystem() {
	// leave a space to combine with system name
	String returnNode = " Return Fan Outlet";
	// String inputNode = " Air Loop Inlet";
	ArrayList<EplusObject> supplySystem = objectLists
		.get("Supply Side System");
	ArrayList<EplusObject> returnFanList = new ArrayList<EplusObject>();
	for (EplusObject eo : supplySystem) {
	    if (eo.getObjectName().equalsIgnoreCase("CONTROLLER:OUTDOORAIR")) {
		// Index 2 is the return air node name, we should change this
		String hvacName = eo.getKeyValuePair(0).getValue().split(" ")[0];
		eo.getKeyValuePair(outdoorAirControllerIndex).setValue(
			hvacName + returnNode);
	    } else if (eo.getObjectName().equalsIgnoreCase("OutdoorAir:Mixer")) {
		String hvacName = eo.getKeyValuePair(0).getValue().split(" ")[0];
		eo.getKeyValuePair(outdoorAirMixer).setValue(
			hvacName + returnNode);
	    } else if (eo.getObjectName().equalsIgnoreCase("AirLoopHVAC")) {
		// find an air loop, change node + add a new fan object to the
		// objectlists
		// air loop first field is hvac name, no need to split
		String hvacFloorName = eo.getKeyValuePair(0).getValue().split(" ")[0];
		//generate the object
		EplusObject returnFan = Manufacturer.generateObject(
			"Return Fan", hvacFloorName);
		//adjust the return fan and supply fan's power ratio
		adjustPower(returnFan, hvacFloorName);
		//add the return fan back to system
		//objectLists.get("Supply Side System").add(returnFan);//to avoid cocurrentmodification
		returnFanList.add(returnFan);
	    } else if (eo.getKeyValuePair(0).getValue().contains("Main Branch")) {
		// find main HVAC branch, start modify the branch
		String hvacName = eo.getKeyValuePair(0).getValue().split(" ")[0];
		eo = Manufacturer.insertReturnFanToBranch(eo, hvacName);
	    }
	}
	
	for(EplusObject e: returnFanList){
	    objectLists.get("Supply Side System").add(e);
	}
    }

    private void adjustPower(EplusObject returnFan, String floor) {
	double maxAirFlow = building.getFloorMaximumFlowRate(floor);
	double minAirFlow = building.getFloorMinimumVentilationRate(floor);
	// select whichever is greater G3.1.2.8
	double returnFanFlow = Math.max(maxAirFlow - minAirFlow,
		0.9 * maxAirFlow);

	EplusObject supplyFan = null;
	ArrayList<EplusObject> supplySystem = objectLists
		.get("Supply Side System");
	for (EplusObject eo : supplySystem) {
	    if (eo.getObjectName().contains("Fan")
		    && eo.getKeyValuePair(0).getValue().contains(floor)) {
		supplyFan = eo;
	    }
	}
	//System.out.println(supplyFan + " " + returnFan + " " + maxAirFlow + " " + returnFanFlow+" "+
	building.getSupplyReturnFanRatio();
	HVACSystemImplUtil.updatedFanPowerforSystem5To8TwoFans(supplyFan,
		returnFan, maxAirFlow, returnFanFlow, building.getSupplyReturnFanRatio());
	
	double supplyFanPower = 0.0;
	double returnFanPower = 0.0;
	
	for (int i = 0; i < supplyFan.getSize(); i++) {
	    if (supplyFan.getKeyValuePair(i).getKey().equals("Pressure Rise")) {
		double supplyPressureRise = Double.parseDouble(supplyFan
			.getKeyValuePair(i).getValue());
		double returnPressureRise = Double.parseDouble(returnFan
			.getKeyValuePair(i).getValue());		
		supplyFanPower += (supplyPressureRise / 0.6 * building
			.getFloorMaximumFlowRate(floor));
		returnFanPower += (returnPressureRise / 0.6 * building
			.getFloorMaximumFlowRate(floor));
	    }
	}
	
	if(building.getInfoObject()!=null){
		building.getInfoObject().setFanPower(supplyFanPower);	  
		building.getInfoObject().setReturnFanPower(returnFanPower);	    
	}
    }

}
