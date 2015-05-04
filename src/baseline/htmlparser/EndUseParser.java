package baseline.htmlparser;

import java.util.HashMap;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class EndUseParser {
    
    private final int electricityIndex = 1;
    private final int naturalgasIndex = 2;
    private final int additioalfuelIndex = 3;
    private final int districtCoolingIndex = 4;
    private final int distribtHeatingIndex = 5;
    private final int waterIndex = 6;
    
    private final Document doc;
    private final Elements enduseSummary;
    
    private static final String END_USE_TABLE = "Annual Building Utility Performance Summary:End Uses";
    private static final String TAG = "tableID";
    
    public EndUseParser(Document d){
	doc = d;
	enduseSummary = doc.getElementsByAttributeValue(TAG, END_USE_TABLE);
    }
    
    public HashMap<String, String> getHeatingEndUseMap(){
	HashMap<String, String> heatingEndUseMap = new HashMap<String, String>();
	Elements heatingEndUseList = enduseSummary.get(0).getElementsByTag("td");
	for(int i=0; i<heatingEndUseList.size(); i++){
	    if(heatingEndUseList.get(i).text().equalsIgnoreCase("Heating")){
		heatingEndUseMap.put("Electricity", heatingEndUseList.get(i+electricityIndex).text());
		heatingEndUseMap.put("Natural Gas", heatingEndUseList.get(i+naturalgasIndex).text());
		heatingEndUseMap.put("Additional Fuel", heatingEndUseList.get(i+additioalfuelIndex).text());
		heatingEndUseMap.put("Disctrict Cooling", heatingEndUseList.get(i+districtCoolingIndex).text());
		heatingEndUseMap.put("Disctrict Heating", heatingEndUseList.get(i+distribtHeatingIndex).text());
		heatingEndUseMap.put("Water", heatingEndUseList.get(i+waterIndex).text());
	    }
	}
	return heatingEndUseMap;
    }
    
    
}
