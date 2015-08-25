package baseline.hvac.system7;

import hvac.manufacturer.Manufacturer;

import java.util.ArrayList;
import java.util.HashMap;

import baseline.generator.EplusObject;
import baseline.hvac.HVACSystemImplUtil;
import baseline.idfdata.EnergyPlusBuilding;

/**
 * Add return fan to the systems.
 * Once one return fan is found in the design case,
 * this class will be initialized and insert return fan
 * to the system at every floor.
 * The return fan power is adjusted based on the ratio from design case
 * return fan power in baseline = return fan power in design case / fan power in design case
 * supply fan will be adjusted too, pressure drop and motor efficiencies will be recalculated.
 * The connections requires to be modified as well. This is also implemented.
 * 
 *
 * @author Weili Xu
 *
 */
public class ReturnFanHVACSystem7 implements SystemType7 {
    // recording all the required data for HVAC system type 7
    private HashMap<String, ArrayList<EplusObject>> objectLists;

    private SystemType7 system;

    private final EnergyPlusBuilding building;

    //private HashMap<String, Double> returnFanFlowMap;

    // modify indexes
    private final int outdoorAirControllerIndex = 2;
    private final int outdoorAirMixer = 4;

    public ReturnFanHVACSystem7(SystemType7 sys, EnergyPlusBuilding bldg) {
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
	for (EplusObject eo : supplySystem) {
	    if (eo.getObjectName().equalsIgnoreCase("CONTROLLER:OUTDOORAIR")) {
		// Index 2 is the return air node name, we should change this
		String hvacName = eo.getKeyValuePair(0).getValue().split(" ")[0];
		eo.getKeyValuePair(outdoorAirControllerIndex).setValue(
			hvacName + returnNode);
		;
	    } else if (eo.getObjectName().equalsIgnoreCase("OutdoorAir:Mixer")) {
		String hvacName = eo.getKeyValuePair(0).getValue().split(" ")[0];
		eo.getKeyValuePair(outdoorAirMixer).setValue(
			hvacName + returnNode);
	    } else if (eo.getObjectName().equalsIgnoreCase("AirLoopHVAC")) {
		// find an air loop, change node + add a new fan object to the
		// objectlists
		// air loop first field is hvac name, no need to split
		String hvacName = eo.getKeyValuePair(0).getValue();
		//generate the object
		EplusObject returnFan = Manufacturer.generateObject(
			"Return Fan", hvacName);
		//adjust the return fan and supply fan's power ratio
		adjustPower(returnFan, hvacName);
		//add the return fan back to system
		objectLists.get("Supply Side System").add(returnFan);

	    } else if (eo.getKeyValuePair(0).getValue().contains("Main Branch")) {
		// find main HVAC branch, start modify the branch
		String hvacName = eo.getKeyValuePair(0).getValue().split(" ")[0];
		eo = Manufacturer.insertReturnFanToBranch(eo, hvacName);
	    }
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
	HVACSystemImplUtil.updatedFanPowerforSystem5To8TwoFans(supplyFan,
		returnFan, maxAirFlow, returnFanFlow);
    }
}
