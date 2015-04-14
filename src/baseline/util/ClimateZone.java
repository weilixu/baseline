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
    CLIMATEZONE2A("Climate Zone 2 A"),
    CLIMATEZONE2B("Climate Zone 2 B"),
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
    CLIMATEZONE7A("Climate Zone 7 A"),
    CLIMATEZONE7B("Climate Zone 7 B"),
    CLIMATEZONE8A("Climate Zone 8 A"),
    CLIMATEZONE8B("Climate Zone 8 B");
    
    private String cZone;
    
    private ClimateZone(String cZone){
	this.cZone = cZone;	
    }
    
    /**
     * -1 means this climate zone does not require economizer
     * @return
     */
    public double getEconomizerShutoffLimit(){
	if(cZone.equals("Climate Zone 1 A")||cZone.equals("Climate Zone 1 B")||
		cZone.equals("Climate Zone 2 A")||cZone.equals("Climate Zone 3 A")||
		cZone.equals("Climate Zone 4 A")){
	    return -1.0;
	}else{
	    if(cZone.equals("Climate Zone 2 B")||
		    cZone.equals("Climate Zone 3 B")||cZone.equals("Climate Zone 3 C")||
		    cZone.equals("Climate Zone 4 B")||cZone.equals("Climate Zone 4 C")||
		    cZone.equals("Climate Zone 5 B")||cZone.equals("Climate Zone 5 C")||
		    cZone.equals("Climate Zone 6 B")||cZone.equals("Climate Zone 7 B")||
		    cZone.equals("Climate Zone 8 A")||cZone.equals("Climate Zone 8 B")){
		return 23.89;
	    }else if(cZone.equals("Climate Zone 5 A")||cZone.equals("Climate Zone 6 A")||
		    cZone.equals("Climate Zone 7 A")){
		return 21.11;
	    }else{
		return 18.33;
	    }
	}
    }
    
    @Override
    public String toString(){
	return cZone;
    }
    
}
