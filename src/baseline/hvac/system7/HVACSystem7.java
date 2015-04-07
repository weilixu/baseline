package baseline.hvac.system7;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import baseline.generator.EplusObject;
import baseline.generator.KeyValuePair;
import baseline.idfdata.EnergyPlusBuilding;
import baseline.idfdata.ThermalZone;

/**
 * The class creates the HVAC System 7. It provides the basic linkages among
 * three systems namely: Supply Side System, Demand Side System and Plant
 * 
 * @author Weili
 *
 */
public class HVACSystem7 implements SystemType7 {
    private HashMap<String, ArrayList<EplusObject>> objectLists;
    private EnergyPlusBuilding building;
    
    private ArrayList<String> zoneSplitterList;
    private ArrayList<String> zoneMixerList;

    private ArrayList<String> systemCoolingCoilList;
    private ArrayList<String> systemHeatingCoilList;
    
    private ArrayList<String> zoneHeatingCoilList;

    public HVACSystem7(HashMap<String, ArrayList<EplusObject>> objects,
	    EnergyPlusBuilding bldg) {
	objectLists = objects;
	building = bldg;
	
	zoneSplitterList = new ArrayList<String>();
	zoneMixerList = new ArrayList<String>();
	systemCoolingCoilList = new ArrayList<String>();
	systemHeatingCoilList = new ArrayList<String>();
	zoneHeatingCoilList = new ArrayList<String>();
	
	processSystems();
    }

    private void processSystems() {
	ArrayList<EplusObject> supplySideSystem = new ArrayList<EplusObject>();
	ArrayList<EplusObject> demandSideSystem = new ArrayList<EplusObject>();
	ArrayList<EplusObject> plantSystem = new ArrayList<EplusObject>();

	ArrayList<EplusObject> supplySideSystemTemplate = objectLists
		.get("Supply Side System");
	ArrayList<EplusObject> demandSideSystemTemplate = objectLists
		.get("Demand Side System");
	ArrayList<EplusObject> plantSystemTemplate = objectLists.get("Plant");
	HashMap<String, ArrayList<ThermalZone>> floorMap = building
		.getFloorMap();

	Set<String> floorMapSet = floorMap.keySet();
	Iterator<String> floorMapIterator = floorMapSet.iterator();

	while (floorMapIterator.hasNext()) {
	    String floor = floorMapIterator.next();
	    // first process the demand side system and their connection to
	    // plant and supply side system
	    ArrayList<ThermalZone> zones = floorMap.get(floor);
	    for(ThermalZone zone:zones){
		demandSideSystem.addAll(processDemandTemp(zone.getFullName(),demandSideSystemTemplate));
	    }
	    //then process the supply side system and their connections to plant
	    supplySideSystem.addAll(processSupplyTemp(floor,
		    supplySideSystemTemplate));
	}
	
	plantSystem.addAll(processPlantTemp(plantSystemTemplate));
	
	objectLists.put("Supply Side System", supplySideSystem);
	objectLists.put("Demand Side System", demandSideSystem);
	objectLists.put("Plant", plantSystem);
	
    }
    
    /**
     * process the HVAC plant side system
     * @param plantSideTemp
     * @return
     */
    private ArrayList<EplusObject> processPlantTemp(ArrayList<EplusObject> plantSideTemp){
	ArrayList<EplusObject> plantTemp = new ArrayList<EplusObject>();
	for(EplusObject eo: plantSideTemp){
	    EplusObject temp = eo.clone();
	    if(temp.getObjectName().equalsIgnoreCase("Connector:Splitter")||temp.getObjectName().equalsIgnoreCase("Connector:Mixer")){
		String name = temp.getKeyValuePair(0).getValue();
		if(name.equals("Hot Water Loop HW Demand Splitter")||name.equals("Hot Water Loop HW Demand Mixer")){
		    for(String s: systemHeatingCoilList){
			KeyValuePair newPair = new KeyValuePair("Outlet Branch",s);
			temp.addField(newPair);
		    }
		    for(String s: zoneHeatingCoilList){
			KeyValuePair newPair = new KeyValuePair("Outlet Branch",s);
			temp.addField(newPair);
		    }
		}else if(name.equals("Chilled Water Loop ChW Demand Splitter")||name.equals("Chilled Water Loop ChW Demand Mixer")){
		    for(String s: systemCoolingCoilList){
			KeyValuePair newPair = new KeyValuePair("Outlet Branch",s);
			temp.addField(newPair);
		    }
		}
	    }else if(temp.getObjectName().equalsIgnoreCase("BranchList")){
		String branchName = temp.getKeyValuePair(0).getValue();
		if(branchName.equals("Hot Water Loop HW Demand Side Branches")){
		    for(String s: zoneHeatingCoilList){
			KeyValuePair newPair = new KeyValuePair("Branch Name",s);
			temp.insertFiled(2, newPair);
		    } 
		    for(String s: systemHeatingCoilList){
			KeyValuePair newPair = new KeyValuePair("Branch Name",s);
			temp.insertFiled(2, newPair);
		    }
		}else if(branchName.equals("Chilled Water Loop ChW Demand Side Branches")){
		    for(String s: systemCoolingCoilList){
			KeyValuePair newPair = new KeyValuePair("Branch Name",s);
			temp.insertFiled(2, newPair);
		    }
		}
	    }
	    plantTemp.add(temp);
	}
	return plantTemp;
    }
    
    /**
     * Process the zones in the building
     * @param zone
     * @param zoneTemp
     * @return
     */
    private ArrayList<EplusObject> processDemandTemp(String zone, ArrayList<EplusObject> zoneTemp){
	ArrayList<EplusObject> demandTemp = new ArrayList<EplusObject>();
	for(EplusObject eo: zoneTemp){
	    EplusObject temp = eo.clone();
	    int size = temp.getSize();
	    for(int i=0; i<size; i++){
		String value = temp.getKeyValuePair(i).getValue();
		if(value.contains("Zone_")){
		    value.replace("Zone_", zone);
		}
		temp.getKeyValuePair(i).setValue(value);
		
		String zoneSplitter = zone+" Zone Equip Inlet";
		String zoneMixer = zone+" Return Outlet";
		String reheatCoil = zone+" Reheat Coil HW Branch"; //this is only for system type 7
		zoneSplitterList.add(zoneSplitter);
		zoneMixerList.add(zoneMixer);
		zoneHeatingCoilList.add(reheatCoil);
	    }
	}
	return demandTemp;
    }
    
    /**
     * process the HVAC supply air side system
     * @param floor
     * @param supplySideSystemTemplate
     * @return
     */
    private ArrayList<EplusObject> processSupplyTemp(String floor,
	    ArrayList<EplusObject> supplySideSystemTemplate) {
	ArrayList<EplusObject> supplyTemp = new ArrayList<EplusObject>();
	for (EplusObject eo : supplySideSystemTemplate) {
	    EplusObject temp = eo.clone();
	    int size = temp.getSize();
	    for (int i = 0; i < size; i++) {
		String value = temp.getKeyValuePair(i).getValue();
		if (value.contains("Floor_")) {
		    value.replace("Floor_", floor);
		}
		temp.getKeyValuePair(i).setValue(value);

		if (temp.getObjectName().equals("Branch")) {
		    if (value.contains("Cooling Coil")) {
			systemCoolingCoilList.add(value);
		    } else if (value.contains("Heating Coil")) {
			systemHeatingCoilList.add(value);
		    }
		}
	    }
	    
	    //check if this is the connection between supply side and demand side systems
	    if(temp.getObjectName().equalsIgnoreCase("AirLoopHVAC:ZoneSplitter")){
		for(String s: zoneSplitterList){
		    KeyValuePair splitterPair = new KeyValuePair("Outlet Node Name",s);
		    temp.addField(splitterPair);
		}
	    }
	    
	    //check if this is the connection between supply side and demand side systems
	    if(temp.getObjectName().equalsIgnoreCase("AirLoopHVAC:ZoneMixer")){
		for(String s: zoneMixerList){
		    KeyValuePair mixerPair = new KeyValuePair("Intlet Node Name",s);
		    temp.addField(mixerPair);
		}
	    }
	}
	return supplyTemp;
    }
}
