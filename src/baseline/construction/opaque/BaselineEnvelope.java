package baseline.construction.opaque;

import baseline.idfdata.BuildingConstruction;

public class BaselineEnvelope {
    private BuildingConstruction building;
    private OpaqueEnvelopeParser envelope;

    public BaselineEnvelope(BuildingConstruction bldg) {
	building = bldg;
	envelope = new OpaqueEnvelopeParser(building.getClimateZone());//this actually includes transparent surfaces
    }
    
    public void replaceConstruction(){
	building.replaceConstruction(envelope);
    }
}
