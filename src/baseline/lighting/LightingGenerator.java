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
	if(buildingType.equalsIgnoreCase("School") || buildingType.equalsIgnoreCase("University")){
	    lpd = 10.65628;
	}else if(buildingType.equalsIgnoreCase("Office")){
	    lpd = 9.687524;
	}else if(buildingType.equalsIgnoreCase("Convention center")){
	    lpd = 11.62503;
	}else if(buildingType.equalsIgnoreCase("Motion picture theater")){
	    lpd = 8.934049;
	}else if(buildingType.equalsIgnoreCase("Museum")){
	    lpd = 11.40975;
	}else if(buildingType.equalsIgnoreCase("Performing arts theater")){
	    lpd = 14.96184;
	}else if(buildingType.equalsIgnoreCase("Exercise center")){
	    lpd = 9.472245;
	}else if(buildingType.equalsIgnoreCase("Gymnasium")){
	    lpd = 10.76392;
	}else if(buildingType.equalsIgnoreCase("Town hall")){
	    lpd = 9.902802;
	}else if(buildingType.equalsIgnoreCase("Sports arena")){
	    lpd = 8.395854;
	}else if(buildingType.equalsIgnoreCase("Dining: bar lounge") || buildingType.equalsIgnoreCase("Dining: leisure")){
	    lpd = 10.65628;
	}else if(buildingType.equalsIgnoreCase("Retail")){
	    lpd = 15.06948;
	}else if(buildingType.equalsIgnoreCase("Dining: cafeterial") || buildingType.equalsIgnoreCase("Dining: fast food")){
	    lpd = 9.687524;
	}else if(buildingType.equalsIgnoreCase("Hospital")){
	    lpd = 13.02434;
	}else if(buildingType.equalsIgnoreCase("Health care clinit")){
	    lpd = 9.364606;
	}else if(buildingType.equalsIgnoreCase("Multifamily")){
	    lpd = 6.458349;
	}else if(buildingType.equalsIgnoreCase("Hotal")){
	    lpd = 10.76392;
	}else if(buildingType.equalsIgnoreCase("Penitentiary")){
	    lpd = 10.441;
	}else if(buildingType.equalsIgnoreCase("Dormitory")){
	    lpd = 6.565988;
	}else if(buildingType.equalsIgnoreCase("Manufacturing facility")){
	    lpd = 11.94795;
	}else if(buildingType.equalsIgnoreCase("Parking garage")){
	    lpd = 2.690979;
	}else if(buildingType.equalsIgnoreCase("Courthouse")){
	    lpd = 11.30211;
	}else if(buildingType.equalsIgnoreCase("Warehouse")){
	    lpd = 7.104184;
	}else if(buildingType.equalsIgnoreCase("Fire station")){
	    lpd = 7.64238;
	}else if(buildingType.equalsIgnoreCase("Library")){
	    lpd = 12.70142;
	}else if(buildingType.equalsIgnoreCase("Post office")){
	    lpd = 9.364606;
	}else if(buildingType.equalsIgnoreCase("Police station")){
	    lpd = 10.33336;
	}else if(buildingType.equalsIgnoreCase("Town hall")){
	    lpd = 9.902802;
	}else if(buildingType.equalsIgnoreCase("Transportation")){
	    lpd = 8.288215;
	}else if(buildingType.equalsIgnoreCase("Religious building")){
	    lpd = 11.30211;
	}else if(buildingType.equalsIgnoreCase("Automotive facility")){
	    lpd = 8.82641;
	}else if(buildingType.equalsIgnoreCase("Workshop")){
	    lpd = 12.9167;
	}else{
	    lpd = 9.687524;
	}

	building.setZoneLPDinBuildingTypeMethod(lpd);
	
//	for(int i=0; i<building.getNumberOfZone(); i++){
//	    String zone = building.getZoneNamebyIndex(i);
//	    building.setZoneLPD(zone, lpd);
//	}
    }
}
