package baseline.htmlparser;

import java.io.File;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class SizingHTMLParser {
    private final File sizing_html;
    private Document doc;
    
    public SizingHTMLParser(File html){
	sizing_html = html;
	try{
	    doc=Jsoup.parse(sizing_html,"UTF-8");
	    preprocessTable();
	}catch(IOException e){
	    //do nothing
	}
    }
    
    private void preprocessTable() {
	String report = null;
	Elements htmlNodes = doc.getAllElements();
	for (int i = 0; i < htmlNodes.size(); i++) {
	    if (htmlNodes.get(i).text().contains("Report:")) {
		report = htmlNodes.get(i + 1).text();
	    }
	    if (htmlNodes.get(i).hasAttr("cellpadding")) {
		String tableName = htmlNodes.get(i - 3).text();
		htmlNodes.get(i).attr("tableID", report + ":" + tableName);
	    }
	}
    }   
}
