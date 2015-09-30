package baseline.lighting;

import baseline.idfdata.BuildingLight;

/**
 * This class modifies the lighting power density
 * the current setting only allows building types include education and office
 * Space by space method will be implemented in the next stage.
 * @author Weili
 *
 */
public class LightingGenerator {
    private BuildingLight building;
    
    public LightingGenerator(BuildingLight bldg){
	building = bldg;
    }
    
    public void processBuildingTypeLPD(){
	String buildingType = building.getBuildingType();
	double lpd = 0.0; //in watt/m2 unit
	if(buildingType.equalsIgnoreCase("Education")){
	    lpd = 10.65628;
	}else if(buildingType.equalsIgnoreCase("Office")){
	    lpd = 9.687524;
	}
	
	building.setZoneLPDinBuildingTypeMethod(lpd);
	
//	for(int i=0; i<building.getNumberOfZone(); i++){
//	    String zone = building.getZoneNamebyIndex(i);
//	    building.setZoneLPD(zone, lpd);
//	}
    }
}
