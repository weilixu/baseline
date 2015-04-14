package baseline.htmlparser;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class CoolingLoadParser {
    private final int userDefinedLoadIndex = 2;
    private final int userDefinedAirFlowIndex = 4;
    
    private final Document doc;
    private final Elements coolingLoadSummary;
    
    private static final String ZONE_COOL_LOAD = "HVAC Sizing Summary:Zone Cooling";
    private static final String TAG = "tableID";
    
    public CoolingLoadParser(Document d){
	doc = d;
	coolingLoadSummary = doc.getElementsByAttributeValue(TAG, ZONE_COOL_LOAD);
    }
    
    public String getUserDefinedCoolingLoad(String zone){
	Elements zoneList = coolingLoadSummary.get(0).getElementsByTag("td");
	for(int i=0;i<zoneList.size();i++){
	    if(zoneList.get(i).text().equalsIgnoreCase(zone)){
		return zoneList.get(i+userDefinedLoadIndex).text();
	    }
	}
	return null;
    }
    
    public String getUserDefinedCoolingAirFlow(String zone){
	Elements zoneList = coolingLoadSummary.get(0).getElementsByTag("td");
	for(int i=0;i<zoneList.size();i++){
	    if(zoneList.get(i).text().equalsIgnoreCase(zone)){
		return zoneList.get(i+userDefinedAirFlowIndex).text();
	    }
	}
	return null;
    }
}
