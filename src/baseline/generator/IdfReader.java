package baseline.generator;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * This object represents an data structure of a typical sorted idf file. The
 * file has to read a valid and sorted idf file first. And it can remove an
 * object, add an object, and edit an object (editing should be specified by
 * client)
 * 
 * 
 * Reads IDF file and store it in a map HashMap<String,
 * HashMap<String,ArrayList<ValueNode>>> The data structure is specifically
 * designed for EnergyPlus key-value pair. In the first HashMap, the String
 * should represents the object name in the EnergyPlus idf file. e.g. Material
 * or BuildingSurface:Detailed etc.
 * 
 * The key maps to another map, this map's key is an element count. This is the
 * number of the same object. e.g. There will be more than 1
 * BuildingSurface:Detailed objects in one single idf file, each time, when the
 * searching algorithm finds the same object, it will increase the count and put
 * it as the second map's key
 * 
 * ValueNode recorde the key-value pair in the idf file. Each line of idf file
 * is one key-value pair. The key in a line should be the text after ! symbol,
 * which represents the information of the data. Value should be the data, which
 * shows at the begining of each line. e.g. 5.456, !- Thermal Resistance
 * {m2-K/W} "5.456" is the value and "Thermal Resistance" is the key
 * 
 * The data structure allows the delete an object, or duplicate an object or
 * edit an object.
 * 
 * @author Weili
 */
public class IdfReader {

    // reading specification
    private static final String endToken = ";";
    private static final String descriptionToken = "!";

    // data structures
    private HashMap<String, HashMap<String, ArrayList<ValueNode>>> eplusMap;

    private String path;

    // boolean indicates the flag
    private boolean output = false;
    private boolean dataFilled = false;

    /**
     * constructor
     */
    public IdfReader() {
	path = null;
	eplusMap = new HashMap<String, HashMap<String, ArrayList<ValueNode>>>();
    }

    /**
     * meanly used for clone this object
     * 
     * @param map
     */
    public IdfReader(String p,
	    HashMap<String, HashMap<String, ArrayList<ValueNode>>> map) {
	dataFilled = true;
	path = p;
	eplusMap = map;
    }


    public void setFilePath(String filePath) {
	path = filePath;
    }

    /**
     * read EplusFile and create the database. read the data from idf file and
     * fill the HashMap. This method will switch the flag <dataFilled> to true
     * so that it can offer remove, add or edit functions to client
     * 
     * This method will also notify GUI for the possible uncertain variables
     * found in the idf file
     * 
     * @throws IOException
     */
    // the file must be stored by overwrite the !-Option(2nd line in the idf
    // file) to sort the order

    public void readEplusFile() throws IOException {
	// reset the data structures
	eplusMap = new HashMap<String, HashMap<String, ArrayList<ValueNode>>>();
	dataFilled = false;
	// set the necessary elements
	FileInputStream inputStream = null;
	Scanner sc = null;
	String startToken = null;
	String elementCount = null;

	try {
	    inputStream = new FileInputStream(path);
	    sc = new Scanner(inputStream, "UTF-8");
	    while (sc.hasNextLine()) {
		String line = sc.nextLine();
		if (line.length() <= 2 || line.startsWith("!")) {
		    continue;
		}
		String firstChar = line.substring(0, 1);

		// look for the start token first when the flag is false
		if (!output && !firstChar.equals(" ")
			&& !firstChar.equals(descriptionToken)) {
		    // find the start token
		    output = true;
		    startToken = line.trim().replace(",", "");
		    // if the start token is not in the hash map, put it in
		    if (!eplusMap.containsKey(startToken)) {
			eplusMap.put(startToken,
				new HashMap<String, ArrayList<ValueNode>>());
		    }

		    // set the second map's key
		    Integer mapSize = eplusMap.get(startToken).size();
		    elementCount = mapSize.toString();
		    eplusMap.get(startToken).put(elementCount,
			    new ArrayList<ValueNode>());

		} else if (output) {
		    // if output is true, it means the searching is still valid.
		    // for the normal cases. put the string in the last nested
		    // map
		    String[] temp = line.split(descriptionToken);

		    // this temp has only one/two elements (for sure)
		    // description stands for the input information
		    // element stands for the data
		    String description = "";
		    String element = "";
		    if (temp.length > 1) {
			element = temp[0].trim();
			description = temp[1].trim().substring(2);
		    }

		    // add the special character to the variableList
//		    if (element.indexOf("$") > -1) {
//
//			String[] keyPair = { startToken, elementCount,
//				description };
//		    }

		    // put element into the map
		    eplusMap.get(startToken).get(elementCount)
			    .add(new ValueNode(element, description));

		    if (line.indexOf(endToken) > -1) {
			// find the end line of the statement, swithc the flag!
			// switch flag
			output = false;
		    }
		}
		if (sc.ioException() != null) {
		    throw sc.ioException();
		}
	    }
	} finally {
	    if (inputStream != null) {
		inputStream.close();
	    }
	    if (sc != null) {
		sc.close();
	    }
	}
	// indicates the data structure is created and filled with data
	dataFilled = true;
    }

    /**
     * @require objectName is exist in the data base remove the whole objects in
     *          the energyPlus. Careful on using this method, it might creates
     *          severe errors in the simulation
     * 
     * @param objectName
     */
    public void removeEnergyPlusObject(String objectName) {
	if (dataFilled) {
	    eplusMap.remove(objectName);
	}
    }
    
    /**
     * get the zone list name that contains the zone
     * @param zoneName
     * @return
     */
    public String getZoneListName(String zoneName){
	HashMap<String,ArrayList<ValueNode>>zoneList = eplusMap.get("ZoneList");
	String zoneListName = null;
	if(zoneList!=null){
	    Set<String> zoneListSet = zoneList.keySet();
	    Iterator<String> zoneListIterator = zoneListSet.iterator();
	    while(zoneListIterator.hasNext()){
		String zoneListCount = zoneListIterator.next();
		ArrayList<ValueNode> zoneListField = zoneList.get(zoneListCount);
		zoneListName = zoneListField.get(0).getAttribute();//first field is the zonelist name
		for(ValueNode vn: zoneListField){
		    if(vn.getAttribute().equalsIgnoreCase(zoneName)){
			return zoneListName;
		    }
		}
	    }
	}
	//if cannot found, return null
	return null;
    }

    /**
     * get value from the database. This method will extract the value according
     * to client inputs. Client needs to provide the name of the object and the
     * input description for a successful extraction. In EnergyPlus, most of
     * objects will have multiple items (e.g. BuildingSurface:Detailed),
     * therefore, this method can only extract one item's input value
     * 
     * If the database does not contain the input description, then this method
     * will return null.
     * 
     * @param objectName
     * @param description
     * @return
     */
    public String getValue(String objectName, String description) {
	HashMap<String, ArrayList<ValueNode>> temp = eplusMap.get(objectName);
	Set<String> elementSet = temp.keySet();
	Iterator<String> iterator = elementSet.iterator();
	String element = iterator.next();
	// search the first element's inputs
	for (ValueNode vn : temp.get(element)) {
	    if (vn.getDescription().equals(description)) {
		return vn.getAttribute();
	    }
	}
	// if we cannot find it.
	return null;
    }

    /**
     * search for same values under same type of objects. It returns a string of
     * grouped value
     * 
     * @param object
     * @param description
     * @return
     */
    public String[] getListValue(String object, String description) {
	String[] list;
	HashMap<String, ArrayList<ValueNode>> temp = eplusMap.get(object);
	Set<String> elementSet = temp.keySet();
	list = new String[elementSet.size()];
	Iterator<String> iterator = elementSet.iterator();
	int counter = 0;
	while (iterator.hasNext()) {
	    String s = iterator.next();
	    ArrayList<ValueNode> nodeList = eplusMap.get(object).get(s);
	    for (ValueNode node : nodeList) {
		if (node.getDescription().equals(description)) {
		    list[counter] = node.getAttribute();
		    counter++;
		    break;
		}
	    }
	}
	return list;
    }

    public HashMap<String, HashMap<String, ArrayList<ValueNode>>> getObjectList(
	    String object) {
	if (eplusMap.get(object) == null) {
	    return null;
	}
	return deepCopyPartialMap(object);
    }

    /**
     * get value from the database. This method will extract the value according
     * to client inputs. Client needs to provide the name of the object, object
     * identification ("Name" inputs in the idf File) and the input description
     * to generate a successful extraction. This method should be used for
     * multiple same objects with different inputs.
     * 
     * If the database doesn't contain the information. This method will yield
     * null
     * 
     * @param objectName
     * @param name
     * @param description
     * @return
     */
    public String getValue(String objectName, String name, String description) {
	HashMap<String, ArrayList<ValueNode>> temp = eplusMap.get(objectName);
	Set<String> elementSet = temp.keySet();
	Iterator<String> iterator = elementSet.iterator();
	ArrayList<ValueNode> targetList = null;
	// search for the object that has specific name
	while (iterator.hasNext() && targetList == null) {
	    String elementCount = iterator.next();
	    ArrayList<ValueNode> vnList = temp.get(elementCount);
	    for (ValueNode vn : vnList) {
		if (vn.getAttribute().equalsIgnoreCase(name)) {
		    targetList = vnList;
		    break;
		}
	    }
	}
	// if found the correct inputs group
	if (targetList != null) {
	    for (ValueNode target : targetList) {
		// search for the specific value
		if (target.getDescription().equals(description)) {
		    return target.getAttribute();
		}
	    }
	}
	// cannot find it
	return null;
    }

    /**
     * @require objectName and elementCount are exist in the data base Remove
     *          one object in the data structure. The objectName shows the name
     *          of the object, element count shows the number of the object in
     *          the database
     * 
     * @param objectName
     * @param elementCount
     */
    public void removeEnergyPlusObject(String objectName, String elementCount) {
	if (dataFilled) {
	    eplusMap.get(objectName).remove(elementCount);
	}
    }

    /**
     * insert an new object without specify the values and descriptions. This
     * algorithm will copy the existing same object data and create the new
     * element.
     * 
     * @param objectName
     */
    public void addNewEnergyPlusObject(String objectName) {
	if (eplusMap.containsKey(objectName)) {
	    // copy the first element
	    String elementCount = "0";
	    ArrayList<ValueNode> temp = eplusMap.get(objectName).get(
		    elementCount);

	    String[] objectValues = new String[temp.size()];
	    String[] objectDes = new String[temp.size()];
	    for (int i = 0; i < temp.size(); i++) {
		objectValues[i] = temp.get(i).getAttribute();
		objectDes[i] = temp.get(i).getDescription();
	    }
	    addNewEnergyPlusObject(objectName, objectValues, objectDes);
	} else {
	    // throw exception or warnings something, determine later
	}
    }

    /**
     * insert an new object into the database with known object name, object
     * values and object descriptions
     * 
     * @param objectName
     *            the name of the object
     * @param objectValues
     *            the object values
     * @param objectDes
     *            the object description (also means the key to the value node)
     */
    public void addNewEnergyPlusObject(String objectName,
	    String[] objectValues, String[] objectDes) {
	// create value node array
	ArrayList<ValueNode> newObject = new ArrayList<ValueNode>();
	for (int i = 0; i < objectValues.length; i++) {
	    newObject.add(new ValueNode(objectValues[i], objectDes[i]));
	}
	// create a new map
	String elementCount = null;
	if (eplusMap.containsKey(objectName)) {
	    Integer count = eplusMap.get(objectName).size();
	    elementCount = count.toString();
	} else {
	    eplusMap.put(objectName, new HashMap<String, ArrayList<ValueNode>>());
	    elementCount = "0";
	}
	eplusMap.get(objectName).put(elementCount, newObject);
    }
    
    /**
     * Remove the old objects in the EnergyPlus and replace them with a new set of objects.
     * Error handling is not complete for this method...cannot detect the object missing in this method
     * @param map
     */
    public void replaceEnergyPlusObjects(HashMap<String, HashMap<String, ArrayList<ValueNode>>> map){
	Set<String> objectList = map.keySet();
	Iterator<String> objectIterator = objectList.iterator();
	while(objectIterator.hasNext()){
	    String object = objectIterator.next();
	    eplusMap.remove(object);
	    eplusMap.put(object, map.get(object));
	}
    }

    public String copyExistingEplusObject(String objectName, String elementCount) {
	HashMap<String, ArrayList<ValueNode>> objectMap = eplusMap
		.get(objectName);
	Integer size = objectMap.size() + 1;
	ArrayList<ValueNode> nodeList = new ArrayList<ValueNode>();
	ArrayList<ValueNode> temp = eplusMap.get(objectName).get(elementCount);
	for (ValueNode vn : temp) {
	    ValueNode node = vn.clone();
	    nodeList.add(node);
	}

	eplusMap.get(objectName).put(size.toString(), nodeList);
	return size.toString();
    }

    /**
     * Replace one specific inputs through all the same object
     * 
     * @param objectName
     *            the object
     * @param onKey
     *            the input description
     * @param toValue
     *            the value
     */
    public void editExistObjectAtSameTime(String objectName, String onKey,
	    String toValue) {
	// retrieve the elements from the map and convert it to set
	Set<String> subSet = eplusMap.get(objectName).keySet();
	Iterator<String> iterator = subSet.iterator();
	// go through all the element in the set
	while (iterator.hasNext()) {
	    ArrayList<ValueNode> vnList = eplusMap.get(objectName).get(
		    iterator.next());
	    // go through all the ValueNode, find the key and change the value
	    for (ValueNode vn : vnList) {
		if (onKey.equals(vn.getDescription())) {
		    vn.setAttribute(toValue);
		}
	    }
	}
    }

    /**
     * change one element in the data base with a specific element Name, such as
     * "boiler1" If there are multiple objects whose name is "boiler1" only the
     * first will be changed.
     * 
     * @param objectName
     * @param elementName
     * @param onKey
     * @param toValue
     */
    public void editExistObjectsOnOneElement(String objectName,
	    String elementName, String onKey, String toValue) {
	// retrieve the elements from the map and convert it to set
	Set<String> subSet = eplusMap.get(objectName).keySet();
	Iterator<String> iterator = subSet.iterator();
	// go through all the element in the set to find the correct element
	ArrayList<ValueNode> vnList = new ArrayList<ValueNode>();
	// flag to indicate whether the element is found or not
	boolean found = false;
	while (iterator.hasNext() && !found) {
	    ArrayList<ValueNode> temp = eplusMap.get(objectName).get(
		    iterator.next());
	    // go through all the ValueNode, find the key and change the value
	    for (ValueNode vn : temp) {
		if (elementName.equals(vn.getAttribute())) {
		    vnList = temp;
		    found = true;
		    break;
		}
	    }
	}
	for (ValueNode node : vnList) {
	    if (onKey.equals(node.getDescription())) {
		node.setAttribute(toValue);
	    }
	}
    }

    public void editExistObjectsOnOneElementFromCount(String objectName,
	    String elementCount, String onKey, String toValue) {
	// retrieve the elements from the map and convert it to set
	ArrayList<ValueNode> vnList = eplusMap.get(objectName)
		.get(elementCount);
	// go through all the ValueNode, find the key and change the value
	for (ValueNode vn : vnList) {
	    if (onKey.equals(vn.getDescription())) {
		vn.setAttribute(toValue);
	    }
	}
    }

    /**
     * create a default mutable tree node for display purpose
     * 
     * @return
     */
    public DefaultMutableTreeNode createTree() {
	DefaultMutableTreeNode energyPlus = new DefaultMutableTreeNode(
		"EnergyPlus");

	Set<String> objectName = eplusMap.keySet();
	Iterator<String> oIterator = objectName.iterator();
	while (oIterator.hasNext()) {
	    String temp = oIterator.next();
	    // create object level node
	    DefaultMutableTreeNode objectNode = new DefaultMutableTreeNode(temp);
	    energyPlus.add(objectNode);

	    // getting the next level information
	    Set<String> elementCount = eplusMap.get(temp).keySet();
	    Iterator<String> eIterator = elementCount.iterator();
	    while (eIterator.hasNext()) {
		String name = eIterator.next();
		ElementList list = new ElementList(name, eplusMap.get(temp)
			.get(name));
		DefaultMutableTreeNode elementNode = new DefaultMutableTreeNode(
			list);
		objectNode.add(elementNode);
	    }
	}
	return energyPlus;
    }

    public void WriteIdf(String path, String fileID) {
	IdfWriter writer = new IdfWriter(eplusMap, path + "\\", fileID);
	try {
	    writer.writeIdf();
	} catch (IOException e) {
	    // do something!
	}
    }
    
    /**
     * clone the object for baseline generation
     * @return
     */
    public IdfReader cloneIdf(){
	return new IdfReader(path,deepCopyMap());
    }

    private HashMap<String, HashMap<String, ArrayList<ValueNode>>> deepCopyMap() {
	HashMap<String, HashMap<String, ArrayList<ValueNode>>> tempMap = new HashMap<String, HashMap<String, ArrayList<ValueNode>>>();
	Set<String> objectList = eplusMap.keySet();
	Iterator<String> iterator = objectList.iterator();
	while (iterator.hasNext()) {
	    String object = iterator.next();

	    HashMap<String, ArrayList<ValueNode>> objectMap = new HashMap<String, ArrayList<ValueNode>>();
	    Set<String> elementList = eplusMap.get(object).keySet();
	    Iterator<String> elementIterator = elementList.iterator();
	    while (elementIterator.hasNext()) {
		String element = elementIterator.next();
		ArrayList<ValueNode> temp = eplusMap.get(object).get(element);
		ArrayList<ValueNode> list = new ArrayList<ValueNode>();
		for (ValueNode vn : temp) {
		    ValueNode newNode = vn.clone();
		    list.add(newNode);
		}
		objectMap.put(element, list);
	    }
	    tempMap.put(object, objectMap);
	}
	return tempMap;
    }

    private HashMap<String, HashMap<String, ArrayList<ValueNode>>> deepCopyPartialMap(
	    String object) {
	HashMap<String, HashMap<String, ArrayList<ValueNode>>> tempMap = new HashMap<String, HashMap<String, ArrayList<ValueNode>>>();
	HashMap<String, ArrayList<ValueNode>> objectMap = new HashMap<String, ArrayList<ValueNode>>();

	Set<String> elementList = eplusMap.get(object).keySet();
	Iterator<String> elementIterator = elementList.iterator();
	while (elementIterator.hasNext()) {
	    String element = elementIterator.next();
	    ArrayList<ValueNode> temp = eplusMap.get(object).get(element);
	    ArrayList<ValueNode> list = new ArrayList<ValueNode>();
	    for (ValueNode vn : temp) {
		ValueNode newNode = vn.clone();
		list.add(newNode);
	    }
	    objectMap.put(element, list);
	}
	tempMap.put(object, objectMap);
	return tempMap;
    }

    /**
     * This is a wrapper class that wraps the element its correspondent data
     * 
     * @author Weili
     *
     */
    public class ElementList {
	private String name;
	private final ArrayList<ValueNode> infoList;

	public ElementList(String n, ArrayList<ValueNode> il) {
	    name = n;
	    infoList = il;
	}

	public ArrayList<ValueNode> getInfo() {
	    return infoList;
	}

	public void addValueNode(ValueNode node) {
	    infoList.add(node);
	}

	public String getName() {
	    return name;
	}

	public String toString() {
	    return infoList.get(0).getAttribute();
	}
    }

    /**
     * IDF Writer class. This class writes the IDF out based on the reader's
     * data
     * 
     * @author Weili
     *
     */
    private class IdfWriter {

	private HashMap<String, HashMap<String, ArrayList<ValueNode>>> eplusMap;
	private String path;
	private String fileID;

	/**
	 * 
	 * @param map
	 *            datastructure that contains energyplus idf file
	 * @param p
	 *            path that the newly generated file can be saved at
	 * @param ID
	 *            the identification for the file name
	 */
	private IdfWriter(
		HashMap<String, HashMap<String, ArrayList<ValueNode>>> map,
		String p, String ID) {
	    eplusMap = map;
	    path = p;
	    fileID = ID;
	}

	private void writeIdf() throws IOException {
	    Writer writer = null;
	    try {
		writer = new BufferedWriter(new OutputStreamWriter(
			new FileOutputStream(path + fileID + ".idf"), "UTF-8"));

		Set<String> eplusObjects = eplusMap.keySet();
		Iterator<String> objectIterator = eplusObjects.iterator();

		while (objectIterator.hasNext()) {
		    String objectKey = objectIterator.next();
		    Set<String> objectElements = eplusMap.get(objectKey)
			    .keySet();
		    Iterator<String> elementIterator = objectElements
			    .iterator();

		    while (elementIterator.hasNext()) {
			String elementKey = elementIterator.next();
			writer.write(objectKey + "," + "\n");
			ArrayList<ValueNode> nodeList = eplusMap.get(objectKey)
				.get(elementKey);
			// loop the nodes through the list
			for (int i = 0; i < nodeList.size(); i++) {
			    ValueNode n = nodeList.get(i);
			    // end statement needs to be end by ;
			    if (i == nodeList.size() - 1) {
				writer.write(n.getAttribute() + "; \n");
			    } else {
				writer.write(n.getAttribute() + ", \n");
			    }
			}
		    }
		}
	    } catch (IOException e) {
		// do nothing
	    } finally {
		try {
		    writer.close();
		} catch (Exception e) {
		    // we are about to close anyway
		}
	    }
	}
    }

    /**
     * This private class stores every line in IDF file as a node. Each node
     * contains two information, two indicators and one parametric signal.
     * 
     * Information: {@value attribute} shows the input for this node {@value
     * description} shows the explanation for the correspondent input
     * 
     * Indicators: {@value isEnd} indicates whether this line is the end of the
     * object inputs or not. {@value isCriticalLine} indicates whether this line
     * contains parametric value, which will later be replaced by parametric
     * studies
     * 
     * Parametric Signal: the {@value index} indicates the index of the
     * parametric value, -1 shows there is no parametric value in this line, if
     * the value is greater than 1, that means this node contains parametric
     * input.
     * 
     * @author Weili
     *
     */
    public class ValueNode {
	/**
	 * Invariance: Each node should contains information includes an
	 * original attribute and the input description
	 */

	// used for copy itself
	private String originalAttribute;
	// real value which will then pass to the energyplus engine
	private String attribute;
	// comments after the (!)
	private String description;
	private String unit;

	private boolean isEnd = false;
	private boolean isCriticalLine = false;

	public ValueNode(String att, String des) {
	    if(des!=null){
		    if (des.indexOf("{") > -1) {
			description = des.substring(0, des.indexOf(" {"));
			unit = des.substring(des.indexOf("{"));
		    } else {
			description = des;
		    }
	    }
	    originalAttribute = att;

	    // test whether this line contains parametric value
	    if (originalAttribute.indexOf("$") > -1) {
		isCriticalLine = true;
	    }

	    // test whether this line is the end input
	    if (originalAttribute.indexOf(";") > -1) {
		attribute = originalAttribute.substring(0,
			originalAttribute.indexOf(";"));
		isEnd = true;
		//this is accomdation to the open studio generated buildingsurface:detail object where
		//coordinates for one point are listed in a line. 35.6615996952, 10.6679966472, 32.9184,  !- X,Y,Z Vertex 1 {m}
	    } else if(originalAttribute.split(",").length>1){
		attribute = originalAttribute.substring(0,originalAttribute.length()-1);
	    }else if (originalAttribute.indexOf(",") > -1) {
		attribute = originalAttribute.substring(0,
			originalAttribute.indexOf(","));
	    } else {
		// hopefully we will never get to this point
		attribute = att;
	    }
	}

	// gets the attribute of the this line
	public String getAttribute() {
	    return attribute;
	}

	// reset the attribute for this object
	// can be used to replace the parametric values in the EnergyPlus
	public void setAttribute(String attr) {
	    attribute = attr;
	}

	// check whether this line is the end statment
	public boolean isEndStatement() {
	    return isEnd;
	}

	// get the description of this line
	public String getDescription() {
	    return description;
	}

	// get the unit of this node
	public String getUnit() {
	    return unit;
	}

	// check whether there is a parametric value defined in this line
	public boolean isCritical() {
	    return isCriticalLine;
	}

	// clone a new value node
	public ValueNode clone() {
	    return new ValueNode(originalAttribute, description);
	}
    }
}
