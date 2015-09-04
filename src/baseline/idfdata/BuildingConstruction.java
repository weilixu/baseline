package baseline.idfdata;

import baseline.construction.opaque.OpaqueEnvelopeParser;
import baseline.util.ClimateZone;

public interface BuildingConstruction {
    
    public ClimateZone getClimateZone();
    
    public void replaceConstruction(OpaqueEnvelopeParser envelope);
}
