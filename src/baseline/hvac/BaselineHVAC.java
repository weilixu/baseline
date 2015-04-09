package baseline.hvac;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import baseline.generator.IdfReader;
import baseline.idfdata.EnergyPlusBuilding;
import baseline.util.BuildingType;
import baseline.util.ClimateZone;

public class BaselineHVAC {
    private final IdfReader baselineModel;
    
    //HVAC related objects
    private HVACSystemFactory factory;
    private HVACSystem system;

    // use for determine whether need a economizer or not
    private final BuildingType bldgType;
    private final EnergyPlusBuilding building;

    // EnergyPlus Objects that requrie to be removed
    private static final String FILE_NAME = "HVACObjects.txt";
    private String[] objectList;
    
    //threasholds for the system selection
    private static final double smallFloorArea = 2300.0;
    private static final double mediumFloorArea = 14000.0;
    private static final int smallFloorNumber=3;
    private static final int mediumFloorNumber=5;

    public BaselineHVAC(IdfReader m, BuildingType type, EnergyPlusBuilding bldg) {
	building = bldg;
	baselineModel = m;
	bldgType = type;
    }

    /**
     * Remove the whole HVAC system in the original file
     * 
     * @throws IOException
     */
    public void removeHVACObjects() throws IOException {
	processObjectLists();
	for (String s : objectList) {
	    baselineModel.removeEnergyPlusObject(s);
	}
    }

    /**
     * This should be called before removeHVACObjects. It is because we need to
     * check district systems in the model
     */
    public void selectSystem() {
	//get required parameter
	double floorSize = building.getTotalFloorArea();
	int floorNumber = building.getNumberOfFloor();
	//first, exam the building type
	if(bldgType.toString().equalsIgnoreCase("NONRESIDENTIAL")){
	    //second exam the floor size and area
	    if(floorNumber>mediumFloorNumber && floorSize>=mediumFloorArea){
		factory = new HVACSystemFactory("System Type 7", building);
		system = factory.createSystem();
	    }
	}
    }
    
    // HVAC objects list is read from local list file
    private void processObjectLists() throws IOException {
	BufferedReader br = new BufferedReader(new FileReader(FILE_NAME));

	try {
	    StringBuilder sb = new StringBuilder();
	    String line = br.readLine();

	    while (line != null) {
		sb.append(line);
		sb.append(":");
		line = br.readLine();
	    }
	    objectList = sb.toString().split(":");
	} finally {
	    br.close();
	}
    }
}
