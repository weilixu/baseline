package baseline.exception.detector;

import baseline.idfdata.IdfReader;
import baseline.idfdata.building.EnergyPlusBuilding;

public class DistrictHeatingDetector implements Detector{
    
    private final String name = "DistrictHeat";

    @Override
    public String getDetectorName() {
	return name;
    }

    @Override
    public void foundException(EnergyPlusBuilding building) {
	IdfReader baselineModel = building.getBaselineModel();
	if(baselineModel.getObjectList("DistrictHeating")!=null){
	    building.setDistrictHeat(true);
	}else{
	    building.setDistrictHeat(false);
	}
    }
}
