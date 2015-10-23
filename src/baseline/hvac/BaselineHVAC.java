package baseline.hvac;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import baseline.generator.EplusObject;
import baseline.generator.IdfReader;
import baseline.idfdata.EnergyPlusBuilding;
import baseline.util.BuildingType;

public class BaselineHVAC {

    // HVAC related objects
    private HVACSystemFactory factory;
    private HVACSystem system;

    // use for determine whether need a economizer or not
    private final BuildingType bldgType;
    private final EnergyPlusBuilding building;

    // EnergyPlus Objects that requrie to be removed
    private static final String FILE_NAME = "HVACObjects.txt";
    private String[] objectList;

    // thresholds for the system selection
    private static final double smallFloorArea = 2300.0;
    private static final double mediumFloorArea = 14000.0;
    private static final int smallFloorNumber = 3;
    private static final int mediumFloorNumber = 5;

    public BaselineHVAC(BuildingType type, EnergyPlusBuilding bldg) {
	building = bldg;
	bldgType = type;
    }

    public IdfReader getBaseline() {
	return building.getBaselineModel();
    }

    /**
     * Remove the whole HVAC system in the original file
     * 
     * @throws IOException
     */
    public void replaceHVACObjects() throws IOException {
	processObjectLists();
	for (String s : objectList) {
	    building.removeHVACObject(s);
	}
	mergeSystem();
    }

    /**
     * Merge the system with baseline model, this should be called after
     */
    private void mergeSystem() {
	HashMap<String, ArrayList<EplusObject>> hvac = system.getSystemData();
	Set<String> hvacSet = hvac.keySet();
	Iterator<String> hvacIterator = hvacSet.iterator();
	while (hvacIterator.hasNext()) {
	    ArrayList<EplusObject> objectList = hvac.get(hvacIterator.next());
	    for (EplusObject eo : objectList) {
		String[] objectValues = new String[eo.getSize()];
		String[] objectDes = new String[eo.getSize()];
		// loop over the key-value pairs
		for (int i = 0; i < objectValues.length; i++) {
		    objectValues[i] = eo.getKeyValuePair(i).getValue();
		    objectDes[i] = eo.getKeyValuePair(i).getKey();
		}
		// add the object to the baseline model
		
		building.insertEnergyPlusObject(eo.getObjectName(),
			objectValues, objectDes);
	    }
	}
    }

    /**
     * This should be called before removeHVACObjects. It is because we need to
     * check district systems in the model
     */
    public void selectSystem() {
	// get required parameter
	double floorSize = building.getTotalFloorArea();
	int floorNumber = building.getNumberOfFloor();
	// first, exam the building type
	if (bldgType.toString().equalsIgnoreCase("NONRESIDENTIAL")) {
	    // second exam the floor size and area
	    System.out.println(floorNumber + " " + floorSize);
	    // haven't implement the heating resource to distinguish the
	    // two different types of systems
	    //factory = new HVACSystemFactory("System Type 7", building);
	    //system = factory.createSystem();
	    if (floorNumber > mediumFloorNumber && floorSize > mediumFloorArea) {
		if (building.getHeatingMethod()) {
		    
		}else{
		    factory = new HVACSystemFactory("System Type 7", building); 
		}
		system = factory.createSystem();
	    } else if (floorNumber <= smallFloorNumber
		    && floorSize <= smallFloorArea) {
		factory = new HVACSystemFactory("System Type 3", building);
		system = factory.createSystem();
	    }else{
		factory = new HVACSystemFactory("System Type 5", building);
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
		sb.append("%");
		line = br.readLine();
	    }
	    objectList = sb.toString().split("%");
	} finally {
	    br.close();
	}
    }
}
