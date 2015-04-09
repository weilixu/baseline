package baseline.util;

public enum BuildingType {
    RESIDENTIAL("Residential"),
    NONRESIDENTIAL("Nonresidential"),
    HEATEDONLYSTORAGE("Heated Only Storage");
    
    private String bldgType;
    
    private BuildingType(String type){
	this.bldgType = type;
    }
    
    @Override
    public String toString(){
	return bldgType;
    }

}
