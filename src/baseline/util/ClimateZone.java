package baseline.util;
/**
 * This represents the ASHRAE Climate Zones
 * 
 * @author Weili
 *
 */
public enum ClimateZone {
    CLIMATEZONE1A("Climate Zone 1 A"),
    CLIMATEZONE1B("Climate Zone 1 B"),
    CLIMATEZONE1C("Climate Zone 1 C"),
    CLIMATEZONE2A("Climate Zone 2 A"),
    CLIMATEZONE2B("Climate Zone 2 B"),
    CLIMATEZONE2C("Climate Zone 2 C"),
    CLIMATEZONE3A("Climate Zone 3 A"),
    CLIMATEZONE3B("Climate Zone 3 B"),
    CLIMATEZONE3C("Climate Zone 3 C"),
    CLIMATEZONE4A("Climate Zone 4 A"),
    CLIMATEZONE4B("Climate Zone 4 B"),
    CLIMATEZONE4C("Climate Zone 4 C"),
    CLIMATEZONE5A("Climate Zone 5 A"),
    CLIMATEZONE5B("Climate Zone 5 B"),
    CLIMATEZONE5C("Climate Zone 5 C"),
    CLIMATEZONE6A("Climate Zone 6 A"),
    CLIMATEZONE6B("Climate Zone 6 B"),
    CLIMATEZONE6C("Climate Zone 6 C"),
    CLIMATEZONE7A("Climate Zone 7 A"),
    CLIMATEZONE7B("Climate Zone 7 B"),
    CLIMATEZONE7C("Climate Zone 7 C"),
    CLIMATEZONE8A("Climate Zone 8 A"),
    CLIMATEZONE8B("Climate Zone 8 B"),
    CLIMATEZONE8C("Climate Zone 8 C");
    
    private String cZone;
    
    private ClimateZone(String cZone){
	this.cZone = cZone;
    }
    
    @Override
    public String toString(){
	return cZone;
    }
    
}
