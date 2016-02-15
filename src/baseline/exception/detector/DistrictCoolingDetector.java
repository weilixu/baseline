package baseline.exception.detector;

import baseline.idfdata.IdfReader;
import baseline.idfdata.building.EnergyPlusBuilding;

public class DistrictCoolingDetector implements Detector{
    
    private final String name = "DistrictCool";

    @Override
    public String getDetectorName() {
	return name;
    }

    @Override
    public void foundException(EnergyPlusBuilding building) {
	IdfReader baselineModel = building.getBaselineModel();
	if(baselineModel.getObjectList("DistrictCooling")!=null){
	    building.setDistrictCool(true);
	}else{
	    building.setDistrictCool(false);
	}
    }

}
