package baseline.hvac;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import baseline.idfdata.EplusObject;
import baseline.idfdata.IdfReader;
import baseline.idfdata.building.EnergyPlusBuilding;
import baseline.util.BuildingType;
import lepost.config.FilesPath;

public class BaselineHVAC {
	private final Logger LOG = LoggerFactory.getLogger(this.getClass());

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
	String systemType = null;
	if (bldgType.toString().equalsIgnoreCase("NONRESIDENTIAL")) {
	    // second exam the floor size and area
	    LOG.info("We identified floor number: " + floorNumber + " and total floor size: " + floorSize);
	    // haven't implement the heating resource to distinguish the
	    // two different types of systems
	    // factory = new HVACSystemFactory("System Type 7", building);
	    // system = factory.createSystem();
	    if (floorNumber >= mediumFloorNumber && floorSize > mediumFloorArea) {
		if (building.getHeatingMethod()) {
		    systemType = "System Type 8";
		    factory = new HVACSystemFactory(systemType, building);
		    LOG.info("We select System Type 8");
		} else {
		    systemType = "System Type 7";
		    factory = new HVACSystemFactory("System Type 7", building);
		    LOG.info("We select System Type 7");
		}
		system = factory.createSystem();
	    } else if (floorNumber <= smallFloorNumber
		    && floorSize <= smallFloorArea) {
		if(building.getHeatingMethod()){
		    systemType = "System Type 4";
		    factory = new HVACSystemFactory("System Type 4", building);
		    LOG.info("We select System Type 4");
		}else{
		    systemType = "System Type 3";
		    factory = new HVACSystemFactory("System Type 3", building);
		    LOG.info("We select System Type 3");		    
		}
		system = factory.createSystem();
	    } else {
		if(building.getHeatingMethod()){
			systemType = "System Type 6";
			factory = new HVACSystemFactory("System Type 6", building);
			LOG.info("We select System Type 6");		
		}else{
			systemType = "System Type 5";
			factory = new HVACSystemFactory("System Type 5", building);
			LOG.info("We select System Type 5");		    
		}
		system = factory.createSystem();
	    }
	}else if (bldgType.toString().equalsIgnoreCase("RESIDENTIAL")){
	    // second exam the floor size and area
		LOG.info("We identified floor number: " + floorNumber + " and total floor size: " + floorSize);
		if (building.getHeatingMethod()) {
		    systemType = "System Type 2";
		    factory = new HVACSystemFactory(systemType, building);
		    LOG.info("We select System Type 2");
		} else {
		    systemType = "System Type 1";
		    factory = new HVACSystemFactory(systemType, building);
		    LOG.info("We select System Type 1");
		}
		system = factory.createSystem();
	}
	if (building.getInfoObject() != null) {
	    building.initialInfoForSystem(systemType);
	}
    }

    // HVAC objects list is read from local list file
    private void processObjectLists() throws IOException {
	BufferedReader br = new BufferedReader(new FileReader(FilesPath.readProperty("ResourcePath_baseline")+FILE_NAME));

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
