package baseline.hvac.system6;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import baseline.hvac.HVACSystemImplUtil;
import baseline.idfdata.EplusObject;
import baseline.idfdata.KeyValuePair;
import baseline.idfdata.building.EnergyPlusBuilding;
import baseline.idfdata.thermalzone.ThermalZone;

public class HVACSystem6 implements SystemType6{
    //recording all the required data for HVAC system type 6
    private HashMap<String, ArrayList<EplusObject>> objectLists;
    // building object contains building information and energyplus data
    private EnergyPlusBuilding building;
    
    // supply air connection list
    private ArrayList<String> zoneSplitterList;
    private ArrayList<String> zoneMixerList;
    
    public HVACSystem6(HashMap<String, ArrayList<EplusObject>> objects, EnergyPlusBuilding bldg){
	objectLists = objects;
	building = bldg;
	
	//Set-up all the data strcuture
	zoneSplitterList = new ArrayList<String>();
	zoneMixerList = new ArrayList<String>();
	
	processSystems();
    }
    
    private void processSystems(){
	ArrayList<EplusObject> supplySideSystem = new ArrayList<EplusObject>();
	ArrayList<EplusObject> demandSideSystem = new ArrayList<EplusObject>();
	
	ArrayList<EplusObject> supplySideSystemTemplate = objectLists.get("Supply Side System");
	ArrayList<EplusObject> demandSideSystemTemplate = objectLists.get("Demand Side System");
	HashMap<String, ArrayList<ThermalZone>> floorMap = building.getFloorMap();
	
	Set<String> floorMapSet = floorMap.keySet();
	Iterator<String> floorMapIterator = floorMapSet.iterator();
	
	int roomCounter = 0;
	int floorCounter = 0;
	while(floorMapIterator.hasNext()){
	    zoneSplitterList.clear();
	    zoneMixerList.clear();
	    String floor = floorMapIterator.next();
	    // first process the demand side system and their connection to
	    // supply side system
	    ArrayList<ThermalZone> zones = floorMap.get(floor);
	    for(ThermalZone zone: zones){
		demandSideSystem.addAll(processDemandTemp(zone.getFullName(), demandSideSystemTemplate));
		// add the outdoor air object for demand zone
		demandSideSystem.add(zone.getOutdoorAirObject());
		roomCounter++;
	    }
	    floorCounter++;
	    //then process the supply side system and their connections to plant
	    supplySideSystem.addAll(processSupplyTemp(floor, supplySideSystemTemplate));
	}
	// number of similar systems
	if (building.getInfoObject() != null) {
	    building.getInfoObject().setNumOfSystem(floorCounter);
	}

	System.out.println("Counting the rooms: " + roomCounter);
	objectLists.put("Supply Side System", supplySideSystem);
	objectLists.put("Demand Side System", demandSideSystem);
	System.out.println("Re-tunning the supply side system...");	
	checkSupplySideSystem();
    }

    @Override
    public HashMap<String, ArrayList<EplusObject>> getSystemData() {
	return objectLists;
    }
    
    private ArrayList<EplusObject> processDemandTemp(String zone, ArrayList<EplusObject> zoneTemp){
	ArrayList<EplusObject> demandTemp = new ArrayList<EplusObject>();
	for(EplusObject eo : zoneTemp){
	    EplusObject temp = eo.clone();
	    // check special characters to avoid useless loop inside the replace
	    // special characters
	    if(temp.hasSpecialCharacters()){
		temp.replaceSpecialCharacters(zone);
	    }
	    demandTemp.add(temp);
	}
	//record the connection links in the HVAC system
	String zoneSplitter = zone + " Zone Equip Inlet";
	String zoneMixer = zone + " Return Outlet";
	
	// add the connection links to another data lists for later
	// processing
	zoneSplitterList.add(zoneSplitter);
	zoneMixerList.add(zoneMixer);
	return demandTemp;
    }
    
    private ArrayList<EplusObject> processSupplyTemp(String floor, ArrayList<EplusObject> supplySideSystemTemplate){
	ArrayList<EplusObject> supplyTemp = new ArrayList<EplusObject>();
	
	for(EplusObject eo : supplySideSystemTemplate){
	    EplusObject temp = eo.clone();
	    
	    /*
	     * replace the special characters that contains floor
	     */
	    if(temp.hasSpecialCharacters()){
		temp.replaceSpecialCharacters(floor);
	    }
	    
	    /*
	     * check if this is the connection between supply side and demand side systems
	     */
	    if(temp.getObjectName().equalsIgnoreCase("AirLoopHVAC:ZoneSplitter")){
		for(String s : zoneSplitterList){
		    KeyValuePair splitterPair = new KeyValuePair("Outlet Node Name", s);
		    temp.addField(splitterPair);
		}
	    }
	    
	    // check if this is the connection between supply side and demand side systems
	    if(temp.getObjectName().equalsIgnoreCase("AirLoopHVAC:ZoneMixer")){
		for(String s : zoneMixerList){
		    KeyValuePair mixerPair = new KeyValuePair("Inlet Node Name",s);
		    temp.addField(mixerPair);
		}
	    }
	    supplyTemp.add(temp);
	}
	return supplyTemp;
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
	
	//Log Changes, economizer
	if(building.getInfoObject()!=null){
	    building.getInfoObject().setHasEconomizer(economizer);
	}
	
	double totalFanPower = 0;
	for (EplusObject eo : supplySystem) {
	    if (eo.getObjectName().equalsIgnoreCase("Controller:OutdoorAir")) {
		if (economizer > -1) {
		    HVACSystemImplUtil.economizer(eo, economizer);
		}
	    } else if (eo.getObjectName()
		    .equalsIgnoreCase("Fan:VariableVolume")) {
		// get the floor name
		String floor = eo.getKeyValuePair(0).getValue().split(" ")[0];
		HVACSystemImplUtil.updateFanPowerforSystem5To8(eo,
			building.getFloorMaximumFlowRate(floor));
		for (int i = 0; i < eo.getSize(); i++) {
		    if (eo.getKeyValuePair(i).getKey().equals("Pressure Rise")) {
			double pressureRise = Double.parseDouble(eo
				.getKeyValuePair(i).getValue());
			totalFanPower += (pressureRise / 0.6 * building
				.getFloorMaximumFlowRate(floor));
		    }
		}
	    }
	}
	if(building.getInfoObject()!=null){
		building.getInfoObject().setFanPower(totalFanPower);	    
	}
    }
}
