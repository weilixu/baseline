package baseline.construction.opaque;

import baseline.generator.IdfReader;

public class BaselineEnvelope {
    private final IdfReader baselineModel;
    
    private static final String EXTERNAL_WALL = "Project Wall";
    private static final String ROOF = "Project Roof";
    private static final String PARTITION = "Project Partition";
    private static final String INTERNAL_FLOOR = "Project Internal Floor";
    private static final String BG_WALL = "Project Below Grade Wall";
    private static final String EXTERNAL_FLOOR = "Project External Floor";
    private static final String SOG_FLOOR = "Project Slab On Grade Floor";

    public BaselineEnvelope(IdfReader m){
	baselineModel = m;
	
    }

}
