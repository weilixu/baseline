package baseline.htmlparser;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class ZoneSummaryParser {
    private final int zoneAreaIndex = 1;
    private final int zoneGrossWallAreaIndex = 4;
    private final int zoneWindowGlassAreaIndex = 5;
    private final int zoneLPDIndex = 6;
    private final int zoneOccupantIndex = 7;
    private final int zoneEPDIndex = 8;
    
    private final Document doc;
    private final Elements zoneSummary;
    
    private static final String ZONE_SUMMARY = "Input Verification and Results Summary:Zone Summary";
    private static final String TAG = "tableID";
    
    public ZoneSummaryParser(Document d){
	doc = d;
	zoneSummary = doc.getElementsByAttributeValue(TAG, ZONE_SUMMARY);
    }
    
    public Double getZoneArea(String zone){
	Elements zoneList = zoneSummary.get(0).getElementsByTag("td");
	for(int i=0; i<zoneList.size(); i++){
	    if(zoneList.get(i).text().equalsIgnoreCase(zone)){
		return Double.parseDouble(zoneList.get(i+zoneAreaIndex).text());
	    }
	}
	return null;
    }
    
    public Double getZoneGrossWallArea(String zone){
	Elements zoneList = zoneSummary.get(0).getElementsByTag("td");
	for(int i=0; i<zoneList.size(); i++){
	    if(zoneList.get(i).text().equalsIgnoreCase(zone)){
		return Double.parseDouble(zoneList.get(i+zoneGrossWallAreaIndex).text());
	    }
	}
	return null;
    }
    
    public Double getZoneWindowGlassArea(String zone){
	Elements zoneList = zoneSummary.get(0).getElementsByTag("td");
	for(int i=0; i<zoneList.size(); i++){
	    if(zoneList.get(i).text().equalsIgnoreCase(zone)){
		return Double.parseDouble(zoneList.get(i+zoneWindowGlassAreaIndex).text());
	    }
	}
	return null;
    }
    
    public Double getZoneLPD(String zone){
	Elements zoneList = zoneSummary.get(0).getElementsByTag("td");
	for(int i=0; i<zoneList.size(); i++){
	    if(zoneList.get(i).text().equalsIgnoreCase(zone)){
		return Double.parseDouble(zoneList.get(i+zoneLPDIndex).text());
	    }
	}
	return null;
    }
    
    public Double getZoneOccupants(String zone){
	Elements zoneList = zoneSummary.get(0).getElementsByTag("td");
	for(int i=0; i<zoneList.size(); i++){
	    if(zoneList.get(i).text().equalsIgnoreCase(zone)){
		return Double.parseDouble(zoneList.get(i+zoneOccupantIndex).text());
	    }
	}
	return null;
    } 
    
    public Double getZoneEPD(String zone){
	Elements zoneList = zoneSummary.get(0).getElementsByTag("td");
	for(int i=0; i<zoneList.size(); i++){
	    if(zoneList.get(i).text().equalsIgnoreCase(zone)){
		return Double.parseDouble(zoneList.get(i+zoneEPDIndex).text());
	    }
	}
	return null;
    }
}
