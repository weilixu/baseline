package baseline.htmlparser;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import baseline.idfdata.building.EnergyPlusBuilding;
import baseline.idfdata.thermalzone.AssetScoreThermalZone;
import baseline.idfdata.thermalzone.DesignBuilderThermalZone;
import baseline.idfdata.thermalzone.ThermalZone;

/**
 * This class extracts the sizing results. This includes: Window-wall ratio,
 * Skylight-Roof Ratio, Conditioned Zone List, Floor Area, Building Peak Cooling
 * and heating loads.
 * 
 * @author Weili
 *
 */
public final class SizingHTMLParser {
    private static Document doc;
    
    //All Summary tables
    private static ZoneSummaryParser zoneSummary;
    private static HeatingLoadParser heatingLoad;
    private static CoolingLoadParser coolingLoad;
    private static MechanicalVentilation miniVent;
    private static EndUseParser enduse;
    private static EquipmentSummary equipment;
    
    private static String tool;
    
    public static void setTool(String t){
	tool = t;
    }
    
    /**
     * process the sizing results
     * @param html
     */
    public static void processOutputs(File html){
	try {
	    doc = Jsoup.parse(html, "UTF-8");
	    preprocessTable();
	    zoneSummary = new ZoneSummaryParser(doc);
	    heatingLoad = new HeatingLoadParser(doc);
	    coolingLoad = new CoolingLoadParser(doc);
	    enduse = new EndUseParser(doc);
	    miniVent = new MechanicalVentilation(doc);
	    equipment = new EquipmentSummary(doc);
	} catch (IOException e) {
	    // do nothing
	}
    }
    
    /**
     * Extract the building basic information. This includes the building total area,
     * building conditioned area and time set point not met
     * @param building
     */
    public static void extractBldgBasicInfo(EnergyPlusBuilding building){
	//extract the building floor area
	extractBuildingFloorArea(building);
	//extract the time setpoint not met
	extractTimeSetPointNotMet(building);
	//extract the heating method
	extractHeatingMethod(building);
    }

    /**
     * add the thermal zones to the building object
     * Can be expanded to add floor area etc.
     */
    public static void extractThermalZones(EnergyPlusBuilding building){
	building.initializeBuildingData();
	Elements thermalZoneSummary = doc.getElementsByAttributeValue("tableID", "Input Verification and Results Summary:Zone Summary");
	Elements zoneList = thermalZoneSummary.get(0).getElementsByTag("tr");
	//jump the first <tr> which contains the meta data
	int conditionIndex = 2;
	for(int i=1; i<zoneList.size();i++){
	    Elements info = zoneList.get(i).getElementsByTag("td");
	    if(info.get(conditionIndex).text().equalsIgnoreCase("YES")){
		String zoneName = info.get(0).text();
		ThermalZone temp = null;
		if(tool.equalsIgnoreCase("Asset Score Tool")){
		    temp = new AssetScoreThermalZone(zoneName);
		}else if(tool.equalsIgnoreCase("DesignBuilder")){
		    temp = new DesignBuilderThermalZone(zoneName);
		}
		double coolLoad = getZoneCoolingLoad(zoneName);
		double heatLoad = getZoneHeatingLoad(zoneName);
		double coolAirFlow = getZoneCoolingAirFlow(zoneName);
		double heatAirFlow = getZoneHeatingAirFlow(zoneName);
		double minimumVent = getZoneMinimumVentilation(zoneName);
		temp.setCoolingLoad(coolLoad);
		temp.setHeaingLoad(heatLoad);
		temp.setCoolingAirFlow(coolAirFlow);
		temp.setHeatingAirFlow(heatAirFlow);
		temp.setMechanicalVentilation(minimumVent);
		temp.setZoneArea(getZoneArea(zoneName));
		temp.setZoneGrossWallArea(getZoneGrossWallArea(zoneName));
		temp.setZoneOccupants(getZoneOccupants(zoneName));
		temp.setZoneLPD(getZoneLPD(zoneName));
		temp.setZoneEPD(getZoneEPD(zoneName));
		building.addThermalZone(temp);
	    }
	}
    }
    
    /**
     * Get the ratio of supply fan power in the total fan power. This method is only useful when
     * the system has supply fan and return fan / supply fan and exhaust fan only.
     * @param supplyFan
     * @param anotherFan
     * @return
     */
    public static double getSupplyFanPowerRatio(String supplyFan, String anotherFan){
	Elements fanSummary = doc.getElementsByAttributeValue("tableID", "Equipment Summary:Fans");
	Elements fanList = fanSummary.get(0).getElementsByTag("tr");
	double supplyFanPower = 0.0;
	double anotherFanPower = 0.0;
	int powerIndex = 5;
	for(int i=1; i<fanList.size(); i++){
	    Elements info = fanList.get(i).getElementsByTag("td");
	    if(info.get(0).text().equalsIgnoreCase(supplyFan)){
		supplyFanPower = Double.parseDouble(info.get(0+powerIndex).text());
	    }
	    if(info.get(0).text().equalsIgnoreCase(anotherFan)){
		anotherFanPower = Double.parseDouble(info.get(0+powerIndex).text());
	    }
	}

	return supplyFanPower / (supplyFanPower + anotherFanPower);
    }
    
    public static double getPumpWaterFlowRate(String type){
	return Double.parseDouble(equipment.getPumpWaterFlow(type));
    }
    
    private static void extractBuildingFloorArea(EnergyPlusBuilding building){
	int areaIndex = 1; //floor area is at index 1 of each column
	Elements buildingAreaSummary = doc.getElementsByAttributeValue("tableID","Annual Building Utility Performance Summary:Building Area");
	Elements areaTableList = buildingAreaSummary.get(0).getElementsByTag("td");
	for(int i=0; i<areaTableList.size();i++){
	    if(areaTableList.get(i).text().equalsIgnoreCase("TOTAL BUILDING AREA")){
		Double totalArea = Double.parseDouble(areaTableList.get(i+areaIndex).text());
		building.setTotalFloorArea(totalArea);
	    }else if(areaTableList.get(i).text().equalsIgnoreCase("NET CONDITIONED BUILDING AREA")){
		Double conditionedArea = Double.parseDouble(areaTableList.get(i+areaIndex).text());
		building.setConditionedFloorArea(conditionedArea);;
	    }
	}
    }
    
    private static void extractTimeSetPointNotMet(EnergyPlusBuilding building){
	int notMetHour = 1; //not met hour is at index 1 of each column
	Elements notmetSummary = doc.getElementsByAttributeValue("tableID","Annual Building Utility Performance Summary:Comfort and Setpoint Not Met Summary");
	Elements hourList = notmetSummary.get(0).getElementsByTag("td");
	for(int i=0; i<hourList.size();i++){
	    if(hourList.get(i).text().equalsIgnoreCase("Time Setpoint Not Met During Occupied Heating")){
		Double heatHr = Double.parseDouble(hourList.get(i+notMetHour).text());
		building.setHeatTimeSetPointNotMet(heatHr);;
	    }else if(hourList.get(i).text().equalsIgnoreCase("Time Setpoint Not Met During Occupied Cooling")){
		Double coolHr = Double.parseDouble(hourList.get(i+notMetHour).text());
		building.setCoolTimeSetPointNotMet(coolHr);
	    }
	}
    }
    
    private static void extractHeatingMethod(EnergyPlusBuilding building){
	HashMap<String, String> heatingEndUse = enduse.getHeatingEndUseMap();
	Double electricity = Double.parseDouble(heatingEndUse.get("Electricity"));
	Double naturalgas = Double.parseDouble(heatingEndUse.get("Natural Gas"));
	Double districtheat = Double.parseDouble(heatingEndUse.get("District Heating"));
	Double addFuel = Double.parseDouble(heatingEndUse.get("Additional Fuel"));
	if(electricity>naturalgas && electricity > districtheat && electricity > addFuel){
	    building.setElectricHeating();
	}
    }
    
    private static Double getZoneHeatingAirFlow(String zone){
	String airflow = heatingLoad.getUserDefinedHeatingAirFlow(zone);
	if(airflow.equals("")){
	    return 0.0;
	}else{
	    return Double.parseDouble(airflow);
	}
    }
    
    private static Double getZoneCoolingAirFlow(String zone){
	String airflow = coolingLoad.getUserDefinedCoolingAirFlow(zone);
	if(airflow.equals("")){
	    return 0.0;
	}else{
	    return Double.parseDouble(airflow);
	}
    }
    
    /**
     * The method must be called after the output was processed.
     */
    private static Double getZoneHeatingLoad(String zone){
	String load = heatingLoad.getHeatingLoad(zone);
	if(load.equals("")){
	    return 0.0;
	}else{
	    return -Double.parseDouble(load);
	}
    }
    
    /**
     * The method must be called after the output was processed.
     */
    private static Double getZoneCoolingLoad(String zone){
	String load = coolingLoad.getCoolingLoad(zone);
	if(load.equals("")){
	    return 0.0;
	}else{
	    return Double.parseDouble(load);
	}
    }
    
    private static Double getZoneMinimumVentilation(String zone){
	Double vent = miniVent.getMinimumVentilationRate(zone);
	return vent;
    }
    
    private static Double getZoneArea(String zone){
	Double area = zoneSummary.getZoneArea(zone);
	return area;
    }
    
    private static Double getZoneGrossWallArea(String zone){
	Double grossWallArea = zoneSummary.getZoneGrossWallArea(zone);
	return grossWallArea;
    }
    
    private static Double getZoneLPD(String zone){
	Double lpd = zoneSummary.getZoneLPD(zone);
	return lpd;
    }
    
    private static Double getZoneOccupants(String zone){
	Double occupants = zoneSummary.getZoneOccupants(zone);
	return occupants;
    }
    
    private static Double getZoneEPD(String zone){
	Double epd = zoneSummary.getZoneEPD(zone);
	return epd;
    }

    private static void preprocessTable() {
	String report = null;
	Elements htmlNodes = doc.getAllElements();
	for (int i = 0; i < htmlNodes.size(); i++) {
	    if (htmlNodes.get(i).text().contains("Report:")) {
		report = htmlNodes.get(i + 1).text().trim();
		if(report.equals("Zone Component Load Summary")){
		    report = report + ":" + htmlNodes.get(i+3).text();
		}
	    }
	    if (htmlNodes.get(i).hasAttr("cellpadding")) {
		String tableName = htmlNodes.get(i - 3).text();
		htmlNodes.get(i).attr("tableID", report + ":" + tableName);
	    }
	}
    }
}
