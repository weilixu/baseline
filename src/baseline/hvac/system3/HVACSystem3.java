package baseline.hvac.system3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import baseline.generator.EplusObject;
import baseline.hvac.HVACSystemImplUtil;
import baseline.idfdata.EnergyPlusBuilding;
import baseline.idfdata.ThermalZone;

public class HVACSystem3 implements SystemType3 {
    // recording all the required data for HVAC system type 3
    private HashMap<String, ArrayList<EplusObject>> objectLists;

    // building object contains building information and energyplus data
    private EnergyPlusBuilding building;

    public HVACSystem3(HashMap<String, ArrayList<EplusObject>> objects,
	    EnergyPlusBuilding bldg) {
	objectLists = objects;
	building = bldg;
	processSystems();
    }

    @Override
    public HashMap<String, ArrayList<EplusObject>> getSystemData() {
	return objectLists;
    }

    private void processSystems() {
	ArrayList<EplusObject> supplySideSystem = new ArrayList<EplusObject>();
	ArrayList<EplusObject> demandSideSystem = new ArrayList<EplusObject>();

	ArrayList<EplusObject> supplySideSystemTemplate = objectLists
		.get("Supply Side System");
	ArrayList<EplusObject> demandSideSystemTemplate = objectLists
		.get("Demand Side System");

	HashMap<String, ArrayList<ThermalZone>> floorMap = building
		.getFloorMap();
	Set<String> floorMapSet = floorMap.keySet();
	Iterator<String> floorMapIterator = floorMapSet.iterator();

	// every zone has one set of the system
	while (floorMapIterator.hasNext()) {
	    String floor = floorMapIterator.next();
	    ArrayList<ThermalZone> zones = floorMap.get(floor);
	    for (ThermalZone zone : zones) {
		demandSideSystem.addAll(processDemandTemp(zone.getFullName(),
			demandSideSystemTemplate));
		// add the outdoor air object for demand zone
		demandSideSystem.add(zone.getOutdoorAirObject());
		supplySideSystem.addAll(processSupplyTemp(zone.getFullName(),
			supplySideSystemTemplate));
	    }
	}
	
	objectLists.put("Supply Side System", supplySideSystem);
	objectLists.put("Demand Side System", demandSideSystem);
	System.out.println("Re-tunning the supply side system...");
	checkSupplySideSystem();
    }
    
    /**
     * updates the supply side system parameters. Check economizer, fan power
     */
    private void checkSupplySideSystem() {
	ArrayList<EplusObject> supplySystem = objectLists
		.get("Supply Side System");
	// determine the economizers.
	double economizer = building.getClimateZone()
		.getEconomizerShutoffLimit();
	for (EplusObject eo : supplySystem) {
	    if (eo.getObjectName().equalsIgnoreCase("Controller:OutdoorAir")) {
		if (economizer > -1) {
		    HVACSystemImplUtil.economizer(eo, economizer);
		}
	    } else if (eo.getObjectName()
		    .equalsIgnoreCase("Fan:OnOff")) {
		//get the floor name
		String zone = eo.getKeyValuePair(0).getValue().split(" ")[0];
		HVACSystemImplUtil.updateFanPowerforSystem3To4(eo,
			building.getZoneMaximumFlowRate(zone));
	    }
	}
    }
    
    /**
     * process the HVAC supply air side system
     * @param zone
     * @param supplySideSystemTemplate
     * @return
     */
    private ArrayList<EplusObject> processSupplyTemp(String zone,
	    ArrayList<EplusObject> supplySideSystemTemplate) {
	ArrayList<EplusObject> supplyTemp = new ArrayList<EplusObject>();
	for (EplusObject eo : supplySideSystemTemplate) {
	    EplusObject temp = eo.clone();

	    /*
	     * replace the special characters that contains floors
	     */
	    if (temp.hasSpecialCharacters()) {
		temp.replaceSpecialCharacters(zone);
	    }

	    supplyTemp.add(temp);
	}
	return supplyTemp;
    }

    /**
     * process the demand side system
     * 
     * @param zone
     * @param zoneTemp
     * @return
     */
    private ArrayList<EplusObject> processDemandTemp(String zone,
	    ArrayList<EplusObject> zoneTemp) {
	ArrayList<EplusObject> demandTemp = new ArrayList<EplusObject>();
	for (EplusObject eo : zoneTemp) {
	    EplusObject temp = eo.clone();
	    // check special characters to avoid useless loop inside the replace
	    // special characters
	    if (temp.hasSpecialCharacters()) {
		temp.replaceSpecialCharacters(zone);
	    }
	    demandTemp.add(temp);
	}
	return demandTemp;
    }
}
