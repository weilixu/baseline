package baseline.htmlparser;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class HeatingLoadParser {
    
    private final int userDefinedLoadIndex = 2;
    private final int userDefinedAirFlowIndex = 4;
    
    private final Document doc;
    private final Elements heatingLoadSummary;
    
    private static final String ZONE_HEAT_LOAD = "HVAC Sizing Summary:Zone Heating";
    private static final String TAG = "tableID";
    
    public HeatingLoadParser(Document d){
	doc = d;
	heatingLoadSummary = doc.getElementsByAttributeValue(TAG, ZONE_HEAT_LOAD);
    }
    
    public String getUserDefinedHeatingLoad(String zone){
	Elements zoneList = heatingLoadSummary.get(0).getElementsByTag("td");
	for(int i=0;i<zoneList.size();i++){
	    if(zoneList.get(i).text().equalsIgnoreCase(zone)){
		return zoneList.get(i+userDefinedLoadIndex).text();
	    }
	}
	return null;
    }
    
    public String getUserDefinedHeatingAirFlow(String zone){
	Elements zoneList = heatingLoadSummary.get(0).getElementsByTag("td");
	for(int i=0;i<zoneList.size();i++){
	    if(zoneList.get(i).text().equalsIgnoreCase(zone)){
		return zoneList.get(i+userDefinedAirFlowIndex).text();
	    }
	}
	return null;
    }
}
