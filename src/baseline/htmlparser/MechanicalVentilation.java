package baseline.htmlparser;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class MechanicalVentilation {
    
    private final int mechanicalVentilationIndex = 4;
    private final int zoneVolume = 3;
    
    private final Document doc;
    private final Elements ventilationSummary;
    
    private static final String ZONE_VENT = "Outdoor Air Summary:Average Outdoor Air During Occupied Hours";
    private static final String TAG = "tableID";
    
    public MechanicalVentilation(Document d){
	doc = d;
	ventilationSummary = doc.getElementsByAttributeValue(TAG, ZONE_VENT);
    }
    
    public Double getMinimumVentilationRate(String zone){
	Elements zoneList = ventilationSummary.get(0).getElementsByTag("td");
	for(int i=0;i<zoneList.size();i++){
	    if(zoneList.get(i).text().equalsIgnoreCase(zone)){
		double ach = Double.parseDouble(zoneList.get(i+mechanicalVentilationIndex).text());
		double volume = Double.parseDouble(zoneList.get(i+zoneVolume).text());
		return ach/3600 * volume;
	    }
	}
	return null;
    }
    
//    public String getUserDefinedHeatingAirFlow(String zone){
//	Elements zoneList = ventilationSummary.get(0).getElementsByTag("td");
//	for(int i=0;i<zoneList.size();i++){
//	    if(zoneList.get(i).text().equalsIgnoreCase(zone)){
//		return zoneList.get(i+userDefinedAirFlowIndex).text();
//	    }
//	}
//	return null;
//    }
}
