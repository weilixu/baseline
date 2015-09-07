package baseline.util;

public enum BuildingType {
    RESIDENTIAL("Residential"),
    NONRESIDENTIAL("Nonresidential"),
    HEATEDONLYSTORAGE("Heated Only Storage");
    
    private String bldgCate;
    
    private BuildingType(String type){
	this.bldgCate = type;
    }
    
    @Override
    public String toString(){
	return bldgCate;
    }
    
}
