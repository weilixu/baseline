package baseline.construction.opaque;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import baseline.generator.EplusObject;
import baseline.generator.KeyValuePair;
import baseline.util.ClimateZone;

/**
 * Parse the opaque envelope XML into database
 * 
 * 
 * @author Weili
 *
 */
public class OpaqueEnvelopeParser {

    private final SAXBuilder builder;
    private final File envelope;
    private Document document;

    private final ClimateZone cZone;
    private ArrayList<EplusObject> objects;

    private static final String FILE_NAME = "envelope.xml";

    public OpaqueEnvelopeParser(ClimateZone zone) {
	cZone = zone;

	builder = new SAXBuilder();
	envelope = new File(FILE_NAME);
	// read the file
	try {
	    document = (Document) builder.build(envelope);
	} catch (Exception e) {
	    e.printStackTrace();
	}

	objects = new ArrayList<EplusObject>();
	envelopBuilder(); // build the model
    }

    /**
     * get the selected objects
     * 
     * @return
     */
    public ArrayList<EplusObject> getObjects() {
	return objects;
    }

    private void envelopBuilder() {
	Element root = document.getRootElement();
	builderHelper(root);
    }

    /**
     * this method focus on finding the correct climate zone dataset
     * 
     * @param current
     */
    private void builderHelper(Element current) {
	List<Element> children = current.getChildren();
	Iterator<Element> iterator = children.iterator();
	while (iterator.hasNext()) {
	    Element child = iterator.next();
	    // if there is an object, find the correct climate dataset
	    System.out.println(child.getName());
	    if (child.getName().equals("dataset")
		    && cZone.toString().contains(child.getAttributeValue("category"))) {
		buildObject(child);
	    }
	}
    }

    /**
     * Build the arraylist of energyplus objects under one specific dataset
     * 
     * @param current
     */
    private void buildObject(Element current) {
	List<Element> children = current.getChildren();
	Iterator<Element> iterator = children.iterator();
	while (iterator.hasNext()) {
	    Element child = iterator.next();
	    String category = child.getAttributeValue("description");
	    String reference = child.getAttributeValue("reference");

	    EplusObject ob = new EplusObject(category,reference);
	    processFields(child, ob);
	    objects.add(ob);
	}
    }

    /**
     * process the fields under one specific object
     * 
     * @param node
     * @param object
     */
    private void processFields(Element node, EplusObject object) {
	List<Element> children = node.getChildren();
	Iterator<Element> iterator = children.iterator();
	while (iterator.hasNext()) {
	    Element child = iterator.next();
	    KeyValuePair pair = new KeyValuePair(
		    child.getAttributeValue("description"), child.getText());
	    object.addField(pair);
	}
    }
}
