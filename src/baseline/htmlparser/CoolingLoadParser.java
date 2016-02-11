package baseline.htmlparser;

import java.util.Iterator;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CoolingLoadParser {
    private final int userDefinedLoadIndex = 2;
    private final int userDefinedAirFlowIndex = 5;
    
    private final Document doc;
    private final Elements coolingLoadSummary;
    
    private static final String report = "Zone Component Load Summary";
    private static final String table_name = "Estimated Cooling Peak Load Components";
    private static final String ZONE_COOL_LOAD = "HVAC Sizing Summary:Zone Cooling";
    private static final String TAG = "tableID";
    
    public CoolingLoadParser(Document d){
	doc = d;
	coolingLoadSummary = doc.getElementsByAttributeValue(TAG, ZONE_COOL_LOAD);
    }
    
    public String getCoolingLoad(String zone){
	String tableId = report + ":" + zone.toUpperCase() + ":" + table_name;
	Element coolingLoadTable = doc.getElementsByAttributeValue(TAG, tableId).get(0);
	
	Elements coolingLoadTr = coolingLoadTable.getElementsByTag("tr");
	
	Elements title = coolingLoadTr.get(0).getElementsByTag("td");
	int index = 0;
	Iterator<Element> titleIt = title.iterator();
	while(titleIt.hasNext()){
	    Element td = titleIt.next();
	    if(td.text().startsWith("Total")){
		break;
	    }
	    index++;
	}
	Element data = coolingLoadTr.last();
	return data.getElementsByTag("td").get(index).text().trim();
    }
    
    public String getUserDefinedCoolingLoad(String zone){
	Elements zoneList = coolingLoadSummary.get(0).getElementsByTag("td");
	for(int i=0;i<zoneList.size();i++){
	    if(zoneList.get(i).text().equalsIgnoreCase(zone)){
		return zoneList.get(i+userDefinedLoadIndex).text();
	    }
	}
	return "";
    }
    
    public String getUserDefinedCoolingAirFlow(String zone){
	Elements zoneList = coolingLoadSummary.get(0).getElementsByTag("td");
	for(int i=0;i<zoneList.size();i++){
	    if(zoneList.get(i).text().equalsIgnoreCase(zone)){
		return zoneList.get(i+userDefinedAirFlowIndex).text();
	    }
	}
	return "";
    }
}
