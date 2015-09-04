package baseline.idfdata;

import baseline.construction.opaque.OpaqueEnvelopeParser;
import baseline.util.ClimateZone;

public interface BuildingConstruction {
    /**
     * Get the climate zone of the study building.
     * This returns the numerator Climate Zone
     * @return
     */
    public ClimateZone getClimateZone();
    
    /**
     * Replace the design case construction with the ASHRAE 90.1
     * qualified performance constructions
     * This has to be performed before climate zone is assigned to
     * the building
     * @param envelope
     */
    public void replaceConstruction(OpaqueEnvelopeParser envelope);
}
