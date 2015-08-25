package baseline.hvac;

import java.util.ArrayList;

import baseline.generator.EplusObject;
import baseline.generator.KeyValuePair;
import baseline.htmlparser.SizingHTMLParser;

/**
 * This class helps to establish, modify, or implement control logic which are
 * specified in Standard 2007 and 2010
 * 
 * 
 * @author Weili
 *
 */
public final class HVACSystemImplUtil {
    // threshold for determine the HVAC components.
    private static final double heatingBoilerThreshold = 1393.55;// m2
    private static final double coolingChillerSmallThreshold = 10550558;// watt
    private static final double coolingChillerLargeThreshold = 21101115;// watt
    private static final double chillerCapacityThreshold = 28134820;

    /**
     * control the economizer, if economizer is needed, then modify the
     * energyplus object
     * 
     * @param eo
     * @param temperature
     */
    public static void economizer(EplusObject eo, double temperature) {
	int numOfFields = eo.getSize();
	for (int i = 0; i < numOfFields; i++) {
	    if (eo.getKeyValuePair(i).getKey()
		    .equals("Economizer Control Type")) {
		eo.getKeyValuePair(i).setValue("FixedDryBulb");
	    } else if (eo.getKeyValuePair(i).getKey()
		    .equals("Economizer Maximum Limit Dry-Bulb Temperature")) {
		eo.getKeyValuePair(i).setValue("" + temperature);
	    }
	}
    }
    
    /**
     * calculates the fan power the calculation method is established base don
     * G3.1.2.10 This is specifically calculation for system type 3 to 4 fan
     * power
     * 
     * @param eo
     * @param airflowRate
     */
    public static void updateFanPowerforSystem3To4(EplusObject eo, double airflowRate){
	Double fanPower = FanPowerCalculation.getFanPowerForSystem3To4(airflowRate);
	//0.5 is the assumed fan total efficiency
	Double pressureDrop = fanPower/airflowRate*0.6;
	Double motorEff = FanPowerCalculation.getFanMotorEfficiencyForSystem3To4(airflowRate);
	for (int i = 0; i < eo.getSize(); i++) {
	    if (eo.getKeyValuePair(i).getKey()
		    .equalsIgnoreCase("Pressure Rise")) {
		eo.getKeyValuePair(i).setValue("" + pressureDrop);
	    } else if (eo.getKeyValuePair(i).getKey()
		    .equalsIgnoreCase("Motor Efficiency")) {
		eo.getKeyValuePair(i).setValue("" + motorEff);
	    }
	}
    }

    /**
     * calculates the fan power the calculation method is established base don
     * G3.1.2.10 This is specifically calculation for system type 5 to 8 fan
     * power
     * 
     * @param eo
     * @param airflowRate
     */
    public static void updateFanPowerforSystem5To8(EplusObject eo,
	    double airflowRate) {
	Double fanPower = FanPowerCalculation
		.getFanPowerForSystem5To8(airflowRate);
	Double pressureDrop = fanPower / airflowRate * 0.6; // 0.6 is assumed
							    // fan total
							    // efficiency
	Double motorEff = FanPowerCalculation
		.getFanMotorEffciencyForSystem5To8(airflowRate);
	for (int i = 0; i < eo.getSize(); i++) {
	    if (eo.getKeyValuePair(i).getKey()
		    .equalsIgnoreCase("Pressure Rise")) {
		eo.getKeyValuePair(i).setValue("" + pressureDrop);
	    } else if (eo.getKeyValuePair(i).getKey()
		    .equalsIgnoreCase("Motor Efficiency")) {
		eo.getKeyValuePair(i).setValue("" + motorEff);
	    }
	}
    }
    
    /**
     * This method is specifically used when there are two fans (supply + return) / (supply + exhaust)
     * coexist in one HVAC air loop.
     * Calculates the fan power the calculation method is established base don
     * G3.1.2.10 This is specifically calculation for system type 5 to 8 fan
     * power.
     * 
     * @param supply: supply fan object
     * @param another: the other fan object
     * @param supplyAir: supply air flow rate
     * @param anotherAir: the another fan air flow rate
     */
    public static void updatedFanPowerforSystem5To8TwoFans(EplusObject supply, EplusObject another, double supplyAir, double anotherAir){
	Double totalFanPower = FanPowerCalculation.getFanPowerForSystem5To8(supplyAir);
	String supplyFanname = supply.getKeyValuePair(0).getValue();
	String anotherFanname = another.getKeyValuePair(0).getValue();
	double ratio = SizingHTMLParser.getSupplyFanPowerRatio(supplyFanname, anotherFanname);
	
	double supplyFanPower = totalFanPower * ratio;
	double anotherFanPower = totalFanPower - supplyFanPower;
	
	double supplyPressureDrop = supplyFanPower / supplyAir * 0.6;
	double anotherPressureDrop = anotherFanPower / anotherAir * 0.6;
	
	double supplymotorEff = FanPowerCalculation.getFanMotorEffciencyForSystem5To8(supplyAir);
	double anothermotorEff = FanPowerCalculation.getFanMotorEffciencyForSystem5To8(anotherAir);
	
	for(int i=0; i<supply.getSize(); i++){
	    if(supply.getKeyValuePair(i).getKey().equalsIgnoreCase("Pressure Rise")){
		supply.getKeyValuePair(i).setValue(""+supplyPressureDrop);
		another.getKeyValuePair(i).setValue("" + anotherPressureDrop);
	    }else if(supply.getKeyValuePair(i).getKey().equalsIgnoreCase("Motor Efficiency")){
		supply.getKeyValuePair(i).setValue(""+supplymotorEff);
		another.getKeyValuePair(i).setValue(""+anothermotorEff);
	    }
	}
    }

    /**
     * number of boiler calculation, based on G3.1.3.2
     * 
     * @param floorArea
     * @return
     */
    public static int boilerNumberCalculation(double floorArea) {
	int numberOfBoiler = 1;
	if (floorArea > heatingBoilerThreshold) {
	    numberOfBoiler = 2;
	}
	return numberOfBoiler;
    }

    /**
     * calculates chiller number based on table G3.1.3.7
     * 
     * @param coolingLoad
     * @return
     */
    public static int chillerNumberCalculation(double coolingLoad) {
	int numberOfChiller = 1;
	if (coolingLoad > coolingChillerSmallThreshold
		&& coolingLoad < coolingChillerLargeThreshold) {
	    numberOfChiller = 2;
	} else if (coolingLoad >= coolingChillerLargeThreshold) {
	    numberOfChiller = 2; // minimum is two chillers;
	    boolean converged = false;
	    while (!converged) {
		double singleChillerCapacity = coolingLoad / numberOfChiller;
		if (singleChillerCapacity < chillerCapacityThreshold) {
		    converged = true;
		} else {
		    numberOfChiller++;
		}
	    }
	}
	return numberOfChiller;
    }

    /**
     * For system type 7-8 where plant is available on-site and require to
     * connect to air distribution system
     * 
     * This method connects all the chillers, towers, boilers, heating and cooling
     * coils in the plant side system
     * 
     * @param plantSystem
     * @param chillerList
     * @param towerList
     * @param boilerList
     * @param sysCooilngCoilList
     * @param sysHeatingCoilList
     * @param zoneHeatingCoilList
     */
    public static void plantConnection(ArrayList<EplusObject> plantSystem,
	    ArrayList<String> chillerList, ArrayList<String> towerList,
	    ArrayList<String> boilerList, ArrayList<String> sysCooilngCoilList,
	    ArrayList<String> sysHeatingCoilList,
	    ArrayList<String> zoneHeatingCoilList) {

	// use for additional eplus objects
	for (EplusObject eo : plantSystem) {
	    String name = eo.getKeyValuePair(0).getValue();
	    if (name.equals("Hot Water Loop HW Demand Side Branches")) {
		insertHeatingCoils(2, eo, sysHeatingCoilList,
			zoneHeatingCoilList);
	    } else if (name.equals("Hot Water Loop HW Demand Splitter")
		    || name.equals("Hot Water Loop HW Demand Mixer")) {
		insertHeatingCoils(eo.getSize(), eo, sysHeatingCoilList,
			zoneHeatingCoilList);// insert to the last index
	    } else if (name
		    .equals("Chilled Water Loop ChW Demand Side Branches")) {
		insertCoolingCoils(2, eo, sysCooilngCoilList);
	    } else if (name.equals("Chilled Water Loop ChW Demand Splitter")
		    || name.equals("Chilled Water Loop ChW Demand Mixer")) {
		insertCoolingCoils(eo.getSize(), eo, sysCooilngCoilList);
	    } else if (name.equals("Hot Water Loop HW Supply Side Branches")
		    || name.equals("Hot Water Loop HW Supply Splitter")
		    || name.equals("Hot Water Loop HW Supply Mixer")) {
		insertBoilerRelatedInputs(2, eo, " HW Branch", boilerList);
	    } else if (name
		    .equals("Chilled Water Loop ChW Supply Side Branches")) {
		insertChillerRelatedInputs(2, eo, " ChW Branch", chillerList);
	    } else if (name.equals("Chilled Water Loop ChW Supply Splitter")
		    || name.equals("Chilled Water Loop ChW Supply Mixer")) {
		insertChillerRelatedInputs(3, eo, " ChW Branch", chillerList);
	    } else if (name
		    .equals("Chilled Water Loop CndW Demand Side Branches")) {
		insertChillerRelatedInputs(2, eo, " CndW Branch", chillerList);
	    } else if (name.equals("Chilled Water Loop CndW Demand Splitter")
		    || name.equals("Chilled Water Loop CndW Demand Mixer")) {
		insertChillerRelatedInputs(3, eo, " CndW Branch", chillerList);
	    } else if (name
		    .equals("Chilled Water Loop CndW Supply Side Branches")) {
		insertTowerRelatedInputs(2, eo, " CndW Branch", towerList);
	    } else if (name.equals("Chilled Water Loop CndW Supply Splitter")
		    || name.equals("Chilled Water Loop CndW Supply Mixer")) {
		insertTowerRelatedInputs(3, eo, " CndW Branch", towerList);
	    } else if (name.equals("Hot Water Loop HW Supply Setpoint Nodes")) {
		insertBoilerRelatedInputs(1, eo, " HW Outlet", boilerList);
	    } else if (name
		    .equals("Chilled Water Loop ChW Supply Setpoint Nodes")) {
		insertChillerRelatedInputs(1, eo, " ChW Outlet", chillerList);
	    } else if (name
		    .equals("Chilled Water Loop CndW Supply Setpoint Nodes")) {
		insertTowerRelatedInputs(1, eo, " CndW Outlet", towerList);
	    } else if (name.equals("Hot Water Loop All Equipment")) {
		insertBoilerEquipmentList(eo, boilerList);
	    } else if (name.equals("Chilled Water Loop All Chillers")) {
		insertChillerEquipmentList(eo, chillerList);
	    } else if (name.equals("Chilled Water Loop All Condensers")) {
		insertTowerEquipmentList(eo, towerList);
	    }
	}
    }

    /*
     * Group of helper functions which inserts the systems connections to the
     * specified fields
     */
    private static void insertHeatingCoils(int index, EplusObject eo,
	    ArrayList<String> systemHeatingCoilList,
	    ArrayList<String> zoneHeatingCoilList) {
	for (String s : zoneHeatingCoilList) {
	    KeyValuePair newPair = new KeyValuePair("Branch Name", s);
	    eo.insertFiled(index, newPair);
	}

	for (String s : systemHeatingCoilList) {
	    KeyValuePair newPair = new KeyValuePair("Branch Name", s);
	    eo.insertFiled(index, newPair);
	}
    }

    private static void insertCoolingCoils(int index, EplusObject eo,
	    ArrayList<String> sysCooilngCoilList) {
	for (String s : sysCooilngCoilList) {
	    KeyValuePair newPair = new KeyValuePair("Branch Name", s);
	    eo.insertFiled(index, newPair);
	}
    }

    private static void insertBoilerRelatedInputs(int index, EplusObject eo,
	    String postfix, ArrayList<String> boilerList) {
	if (boilerList.isEmpty()) {
	    KeyValuePair newPair = new KeyValuePair("Branch Name", "Boiler%"
		    + postfix);
	    eo.insertFiled(index, newPair);
	} else {
	    for (String s : boilerList) {
		KeyValuePair newPair = new KeyValuePair("Branch Name", s
			+ postfix);
		eo.insertFiled(index, newPair);
	    }
	}
    }

    private static void insertChillerRelatedInputs(int index, EplusObject eo,
	    String postfix, ArrayList<String> chillerList) {
	if (chillerList.isEmpty()) {
	    KeyValuePair newPair = new KeyValuePair("Branch Name", "Chiller%"
		    + postfix);
	    eo.insertFiled(index, newPair);
	} else {
	    for (String s : chillerList) {
		KeyValuePair newPair = new KeyValuePair("Branch Name", s
			+ postfix);
		eo.insertFiled(index, newPair);
	    }
	}
    }

    private static void insertTowerRelatedInputs(int index, EplusObject eo,
	    String postfix, ArrayList<String> towerList) {
	if (towerList.isEmpty()) {
	    KeyValuePair newPair = new KeyValuePair("Branch Name", "TOWER%"
		    + postfix);
	    eo.insertFiled(index, newPair);
	} else {
	    for (String s : towerList) {
		KeyValuePair newPair = new KeyValuePair("Branch Name", s
			+ postfix);
		eo.insertFiled(index, newPair);
	    }
	}
    }

    private static void insertBoilerEquipmentList(EplusObject eo,
	    ArrayList<String> boilerList) {
	if (boilerList.isEmpty()) {
	    KeyValuePair objectType = new KeyValuePair("Equipment Object Type",
		    "Boiler:HotWater");
	    KeyValuePair name = new KeyValuePair("Equipment Name", "Boiler%");
	    eo.addField(objectType);
	    eo.addField(name);
	} else {
	    for (String s : boilerList) {
		KeyValuePair objectType = new KeyValuePair(
			"Equipment Object Type", "Boiler:HotWater");
		KeyValuePair name = new KeyValuePair("Equipment Name", s);
		eo.addField(objectType);
		eo.addField(name);
	    }
	}
    }

    private static void insertChillerEquipmentList(EplusObject eo,
	    ArrayList<String> chillerList) {
	if (chillerList.isEmpty()) {
	    KeyValuePair objectType = new KeyValuePair("Equipment Object Type",
		    "Chiller:Electric:EIR");
	    KeyValuePair name = new KeyValuePair("Equipment Name", "Chiller%");
	    eo.addField(objectType);
	    eo.addField(name);
	} else {
	    for (String s : chillerList) {
		KeyValuePair objectType = new KeyValuePair(
			"Equipment Object Type", "Chiller:Electric:EIR");
		KeyValuePair name = new KeyValuePair("Equipment Name", s);
		eo.addField(objectType);
		eo.addField(name);
	    }
	}
    }

    private static void insertTowerEquipmentList(EplusObject eo,
	    ArrayList<String> towerList) {
	if (towerList.isEmpty()) {
	    KeyValuePair objectType = new KeyValuePair("Equipment Object Type",
		    "CoolingTower:TwoSpeed");
	    KeyValuePair name = new KeyValuePair("Equipment Name", "Tower%");
	    eo.addField(objectType);
	    eo.addField(name);
	} else {
	    for (String s : towerList) {
		KeyValuePair objectType = new KeyValuePair(
			"Equipment Object Type", "CoolingTower:TwoSpeed");
		KeyValuePair name = new KeyValuePair("Equipment Name", s);
		eo.addField(objectType);
		eo.addField(name);
	    }
	}
    }

}
