package baseline.hvac.system2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import baseline.hvac.HVACSystemImplUtil;
import baseline.idfdata.EplusObject;
import baseline.idfdata.building.EnergyPlusBuilding;
import baseline.idfdata.thermalzone.ThermalZone;

public class HVACSystem2 implements SystemType2{
    // recording all the required data for HVAC system type 2
    private HashMap<String, ArrayList<EplusObject>> objectLists;
    
    // building object contains building information and energyplus data
    private EnergyPlusBuilding building;
    
    public HVACSystem2 (HashMap<String, ArrayList<EplusObject>> objects, EnergyPlusBuilding bldg){
	objectLists = objects;
	building = bldg;
	
	processSystems();
    }
    

    @Override
    public HashMap<String, ArrayList<EplusObject>> getSystemData() {
	return objectLists;
    }
    
    private void processSystems(){
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
	
	int zoneCounter = 0;
	while(floorMapIterator.hasNext()){
	    String floor = floorMapIterator.next();
	    //first process the demand side system and their connection to
	    //supply side system
	    ArrayList<ThermalZone> zones = floorMap.get(floor);
	    for(ThermalZone zone: zones){
		zoneCounter ++;
		demandSideSystem.addAll(processDemandTemp(zone.getFullName(), demandSideSystemTemplate));
		// add the outdoor air object for demand zone
		demandSideSystem.add(zone.getOutdoorAirObject());
		supplySideSystem.addAll(processSupplyTemp(zone.getFullName(),supplySideSystemTemplate));		
	    }
	}
	if(building.getInfoObject()!=null){
	    building.getInfoObject().setNumOfSystem(zoneCounter);
	}
	System.out.println("Counting the rooms: " + zoneCounter);
	objectLists.put("Supply Side System", supplySideSystem);
	objectLists.put("Demand Side System", demandSideSystem);
	System.out.println("Re-tunning the supply side system...");
	checkSupplySideSystem();
	System.out.println("Connect plans");	
    }
    
    /**
     * process the HVAC supply air side system
     * 
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
    
    
    /**
     * updates the supply side system parameters. Check economizer, fan power
     */
    private void checkSupplySideSystem() {
	ArrayList<EplusObject> supplySystem = objectLists
		.get("Supply Side System");
	// determine the economizers.
	double economizer = building.getClimateZone()
		.getEconomizerShutoffLimit();
	building.getInfoObject().setHasEconomizer(economizer);
	double totalFanPower = 0;
	for (EplusObject eo : supplySystem) {
	    if (eo.getObjectName().equalsIgnoreCase("Controller:OutdoorAir")) {
		if (economizer > -1) {
		    HVACSystemImplUtil.economizer(eo, economizer);
		}
	    } else if (eo.getObjectName()
		    .equalsIgnoreCase("Fan:ConstantVolume")) {
		// get the floor name
		String zone = eo.getKeyValuePair(0).getValue().split(" ")[0];
		HVACSystemImplUtil.updateFanPowerforSystem3To4(eo,
			building.getZoneMaximumFlowRate(zone));
		for (int i = 0; i < eo.getSize(); i++) {
		    if (eo.getKeyValuePair(i).getKey()
			    .equals("Pressure Rise")) {
			double pressureRise = Double
				.parseDouble(eo.getKeyValuePair(i).getValue());
			totalFanPower += (pressureRise / 0.6
				* building.getZoneMaximumFlowRate(zone));
		    }
		}
	    }
	    building.getInfoObject().setFanPower(totalFanPower);
	}
    }
    
    
    

}
