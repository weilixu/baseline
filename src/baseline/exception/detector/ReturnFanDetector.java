package baseline.exception.detector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import baseline.htmlparser.SizingHTMLParser;
import baseline.idfdata.IdfReader;
import baseline.idfdata.IdfReader.ValueNode;
import baseline.idfdata.building.EnergyPlusBuilding;

public class ReturnFanDetector implements Detector{
    
    private double numberOfSystem;
    private double supplyReturnRatio;
    
    private final String name = "ReturnFan";
    
    private HashMap<String, Boolean> returnFanMap;
    
    @Override
    public String getDetectorName(){
	return name;
    }
    
    @Override
    public void foundException(EnergyPlusBuilding building) {
	//set-up values
	IdfReader baselineModel = building.getBaselineModel();
	returnFanMap = new HashMap<String, Boolean>();
	numberOfSystem = 0.0;
	supplyReturnRatio = 0.0;
	
	//check systems
	checkForReturnFans(baselineModel);
	
	//check if any return fan exist
	Set<String> returnFan = returnFanMap.keySet();
	Iterator<String> returnFanIterator = returnFan.iterator();
	while (returnFanIterator.hasNext()) {
	    String fan = returnFanIterator.next();
	    // System.out.println(fan+" " + returnFanMap.get(fan));
	    if (returnFanMap.get(fan)) {
		//if there is, set the supply return fan power ratio
		building.setSupplyReturnRatio(supplyReturnRatio);
		building.setNumberOfSystem(numberOfSystem);
		building.setReturnFanIndicator(true);
	    }
	}
    }
    

    private void checkForReturnFans(IdfReader baselineModel) {
	HashMap<String, ArrayList<ValueNode>> airLoops;
	try {
	    airLoops = baselineModel.getObjectList("AirLoopHVAC").get(
		    "AirLoopHVAC");
	} catch (NullPointerException e) {
	    airLoops = null;
	}
	if (airLoops != null) {
	    Set<String> airloopList = airLoops.keySet();
	    Iterator<String> airLoopIterator = airloopList.iterator();
	    while (airLoopIterator.hasNext()) {
		String airloop = airLoopIterator.next();
		String branchListName = "";
		String demandSideOutletName = "";
		for (int i = 0; i < airLoops.get(airloop).size(); i++) {
		    if (airLoops.get(airloop).get(i).getDescription()
			    .equals("Branch List Name")) {
			branchListName = airLoops.get(airloop).get(i)
				.getAttribute();
		    } else if (airLoops.get(airloop).get(i).getDescription()
			    .equals("Demand Side Outlet Node Name")) {
			demandSideOutletName = airLoops.get(airloop).get(i)
				.getAttribute();
		    }
		}
		// branch list to check system return fan
		String returnFan = hasReturnFan(baselineModel, branchListName);
		returnFanMap.put("Building", returnFan != null);
		// demand side check thermal zones
		// processFloorReturnFanMap(demandSideOutletName, returnFan);
		String supplyFan = getSupplyFanName(baselineModel, branchListName, returnFan);
		System.out.println(supplyFan + " " + returnFan);
		numberOfSystem = numberOfSystem + 1;
		supplyReturnRatio = supplyReturnRatio
			+ SizingHTMLParser.getSupplyFanPowerRatio(supplyFan,
				returnFan);
		//System.out.println(numberOfSystem + " " + supplyReturnRatio);
	    }
	}
    }
    

    private String hasReturnFan(IdfReader baselineModel, String BranchList) {
	String returnFan = null;
	// 1. get the air loop branch name from branchlist
	String branchName = baselineModel.getValue("BranchList", BranchList,
		"Branch 1 Name");
	// 2. check fan object at first component listed on branch
	// System.out.println(branchName);
	String componentName = baselineModel.getValue("Branch", branchName,
		"Component 1 Object Type");
	// System.out.println(componentName);
	if (componentName.contains("Fan")) {
	    returnFan = baselineModel.getValue("Branch", branchName,
		    "Component 1 Name");
	}
	// System.out.println("******************************************************"+returnFan);
	return returnFan;
    }

    private String getSupplyFanName(IdfReader baselineModel, String BranchList, String returnFan) {
	// 1. get the air loop branch name from branchlist
	String branchName = baselineModel.getValue("BranchList", BranchList,
		"Branch 1 Name");
	// baselineModel.get
	ArrayList<ValueNode> nodeList = baselineModel.getObject("Branch",
		branchName);
	for (int i = 0; i < nodeList.size(); i++) {
	    ValueNode vn = nodeList.get(i);
	    if (vn.getDescription().contains("Object Type")
		    && vn.getAttribute().contains("Fan")) {
		// if this is not return fan, then we assume it is the supply
		// fan
		if (!nodeList.get(i + 1).getAttribute()
			.equalsIgnoreCase(returnFan)) {
		    return nodeList.get(i + 1).getAttribute();
		}
	    }
	}
	return null;
    }
}
