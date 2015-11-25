package baseline.htmlparser;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class EquipmentSummary {
    private int waterflowIndex = 4;
    
    private final Document doc;
    private final Elements pumpsSummary;
    
    private static final String PUMP_SUMMARY = "Equipment Summary:Pumps";
    private static final String TAG = "tableID";
    
    public EquipmentSummary(Document d){
	doc = d;
	pumpsSummary = doc.getElementsByAttributeValue(TAG, PUMP_SUMMARY);
    }
    
    public String getPumpWaterFlow(String type){
	Elements pumpList = pumpsSummary.get(0).getElementsByTag("td");
	for(int i=0; i<pumpList.size(); i++){
	    if(pumpList.get(i).text().contains(type)){
		return pumpList.get(i+waterflowIndex).text();
	    }
	}
	return "0";
    }

}
