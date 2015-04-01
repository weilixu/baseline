package test.thermalzone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import baseline.htmlparser.SizingHTMLParser;
import baseline.idfdata.EnergyPlusBuilding;
import baseline.idfdata.ThermalZone;

public class EplusSizingHTMLParserTest {
    private EnergyPlusBuilding building;
    
    /**Called before each test case method */
    @Before
    public void setUp() throws Exception{
	String path = "C:\\Users\\Weili\\Desktop\\AssetScoreTool\\TESTFile\\BaselineTable.html";
	File html = new File(path);
	SizingHTMLParser.processOutputs(html);
	building = new EnergyPlusBuilding();
	SizingHTMLParser.extractBldgBasicInfo(building);
	SizingHTMLParser.extractThermalZones(building);
    }
    
    /** Called after each test case method. */
    @After
    public void tearDown() throws Exception {
	// Don't need to do anything here.
    }
    
    @Test
    public void testBuildingBasicInfo(){
	Double totalFloorArea = 2112.62;
	Double conditionedFloorArea = 2112.62;
	assertEquals(building.getTotalFloorArea(),totalFloorArea);
	assertEquals(building.getConditionedFloorArea(),conditionedFloorArea);
	
	Double heatHr = 192.67;
	Double coolHr = 4.67;
	assertEquals(building.getHeatingSetPointNotMet(),heatHr);
	assertEquals(building.getCoolingSetPointNotMet(),coolHr);
    }
    
    @Test
    public void testThermalZones(){
	building.getThermalZoneInfo();
	
	Double totalCoolingLoad = 68819.88;
	assertEquals(building.getTotalCoolingLoad(),totalCoolingLoad);
	Double totalHeatingLoad = 131588.67;
	assertEquals(building.getTotalHeatingLoad(),totalHeatingLoad);
	
	HashMap<String, ArrayList<ThermalZone>> floorMap = building.getFloorMap();
	assertTrue(floorMap.containsKey("1"));
	assertTrue(floorMap.containsKey("2"));
	assertTrue(floorMap.get("1").size()==1);
	assertTrue(floorMap.get("2").size()==1);
	
	assertTrue(floorMap.get("1").get(0).getFullName().equals("BLOCKS-3619_OFFICE_1_CORE"));
	assertTrue(floorMap.get("2").get(0).getFullName().equals("BLOCKS-3619_OFFICE_2_CORE"));

    }

}
