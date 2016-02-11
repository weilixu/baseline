package baseline.exception.detector;

import baseline.idfdata.EnergyPlusBuilding;

public interface Detector {
    
    public String getDetectorName();
    
    public void foundException(EnergyPlusBuilding building);

}
