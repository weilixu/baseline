package baseline.construction.opaque;

import java.util.ArrayList;

import baseline.generator.EplusObject;
import baseline.generator.IdfReader;
import baseline.util.ClimateZone;

public class BaselineEnvelope {
    private final IdfReader baselineModel;
    private final OpaqueEnvelopeParser envelope;
    
    private final ClimateZone zone;
    
    //EnergyPlus Objects that require to be removed
    private static final String MATERIAL="Material";
    private static final String MATERIALMASS="Material:NoMass";
    private static final String CONSTRUCTION="Construction";
    private static final String SIMPLE_WINDOW = "WindowMaterial:SimpleGlazingSystem";
    
    //Baseline Construction Name (to replace the original constructions)
    private static final String EXTERNAL_WALL = "Project Wall";
    private static final String ROOF = "Project Roof";
    private static final String PARTITION = "Project Partition";
    private static final String INTERNAL_FLOOR = "Project Internal Floor";
    private static final String BG_WALL = "Project Below Grade Wall";
    private static final String EXTERNAL_FLOOR = "Project External Floor";
    private static final String SOG_FLOOR = "Project Slab On Grade Floor";
    private static final String WINDOW="Project Window";
    private static final String CURTAIN_WALL="Project Curtain Wall";
    private static final String SKYLIGHT="Project Skylight";
    
    //EnergyPlus objects that relates to the construction changes
    private static final String BLDG_SURFACE = "BuildingSurface:Detailed";
    private static final String BLDG_FENE = "FenestrationSurface:Detailed";

    public BaselineEnvelope(IdfReader m, ClimateZone c){
	baselineModel = m;
	zone=c;
	envelope = new OpaqueEnvelopeParser(zone);
    }
    
    public void execute(){
	//remove all the building envelope related objects
	baselineModel.removeEnergyPlusObject(MATERIAL);
	baselineModel.removeEnergyPlusObject(MATERIALMASS);
	baselineModel.removeEnergyPlusObject(CONSTRUCTION);
	baselineModel.removeEnergyPlusObject(SIMPLE_WINDOW);
	
	//add new building envelope related objects
	ArrayList<EplusObject> objects = envelope.getObjects(); //retrieve the data from database
	for(EplusObject o: objects){
	    String[] objectValues = new String[o.getSize()];
	    String[] objectDes = new String[o.getSize()];
	    //loop over the key-value pairs
	    for(int i=0; i<objectValues.length; i++){
		objectValues[i]=o.getKeyValuePair(i).getValue();
		objectDes[i] = o.getKeyValuePair(i).getKey();
	    }
	    //add the object to the baseline model
	    baselineModel.addNewEnergyPlusObject(o.getObjectName(), objectValues, objectDes);
	}
	
	//change the EnergyPlus object that relates to the constructions
	

    }
    
    

}
