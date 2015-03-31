package test.thermalzone;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import baseline.idfdata.AssetScoreThermalZone;
import baseline.idfdata.ThermalZone;

public class AssetScoreThermalZoneTest {
    private ArrayList<ThermalZone> thermalZones;
    
    /**Called before each test case method */
    @Before
    public void setUp() throws Exception{
	String[] zones = {"BLOCKS-3619_OFFICE_1_CORE","BLOCKS-3619_OFFICE_2_CORE"};
	thermalZones = new ArrayList<ThermalZone>();
	
	for(String z: zones){
	    thermalZones.add(new AssetScoreThermalZone(z));
	}
    }
    
    /** Called after each test case method. */
    @After
    public void tearDown() throws Exception {
	// Don't need to do anything here.
    }
    
    @Test
    public void nameTest(){
	assertEquals(thermalZones.get(0).getBlock(),thermalZones.get(1).getBlock());
	assertEquals(thermalZones.get(0).getZoneType(),thermalZones.get(1).getZoneType());
    }
}
