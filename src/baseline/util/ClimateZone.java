package baseline.util;

public enum ClimateZone {
    CLIMATEZONE1("Climate Zone 1"),
    CLIMATEZONE2("Climate Zone 2"),
    CLIMATEZONE3("Climate Zone 3"),
    CLIMATEZONE4("Climate Zone 4"),
    CLIMATEZONE5("Climate Zone 5"),
    CLIMATEZONE6("Climate Zone 6"),
    CLIMATEZONE7("Climate Zone 7"),
    CLIMATEZONE8("Climate Zone 8");
    
    private String cZone;
    
    private ClimateZone(String cZone){
	this.cZone = cZone;
    }
    
    @Override
    public String toString(){
	return cZone;
    }
    
}
