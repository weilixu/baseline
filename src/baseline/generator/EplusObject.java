package baseline.generator;

import java.util.ArrayList;

public class EplusObject {
    private final String objectName;
    private final ArrayList<KeyValuePair> objectValues;
    private int size = 0;
    
    public EplusObject(String n){
	objectName = n;
	objectValues = new ArrayList<KeyValuePair>();
    }
    
    /**
     * add fields
     * @param field
     */
    public void addField(KeyValuePair field){
	objectValues.add(field);
	size++;
    }
    
    /**
     * get the size of the object fields.
     * @return
     */
    public int size(){
	return size;
    }
    
    /**
     * get the correspondent key value pair
     * @param index
     * @return
     */
    public KeyValuePair getKeyValuePair(int index){
	return objectValues.get(index);
    }

}
