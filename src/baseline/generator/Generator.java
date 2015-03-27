package baseline.generator;

import java.io.File;
import java.io.IOException;

import baseline.construction.opaque.BaselineEnvelope;
import baseline.util.ClimateZone;

public class Generator {

    private boolean isExisting = false;

    private final IdfReader designModel;
    private final IdfReader baselineModel;
    private final ClimateZone cZone;

    private BaselineEnvelope envelopeProcessor;
    
    private final File energyplusFile;

    public Generator(File idfFile, File weatherFile, ClimateZone zone,
	    boolean existing) {
	// identify the climate zone
	cZone = zone;
	isExisting = existing;

	// establish the design model
	energyplusFile = idfFile;
	designModel = new IdfReader();
	designModel.setFilePath(energyplusFile.getAbsolutePath());
	try {
	    designModel.readEplusFile();
	} catch (IOException e) {
	    e.printStackTrace();
	    // cannot read the design file, check directory
	}
	baselineModel = designModel.cloneIdf();
	processThermalZones();

	envelopeProcessor = new BaselineEnvelope(baselineModel, cZone);
	processOpaqueEnvelope();
    }

    private void processThermalZones() {

    }

    /**
     * process all the envelopes. This process includes: 1. Delete the original
     * constructions 2. Replace with a standard set of baseline constructions
     * with respect to the thermal zones 3. Replace the construction name in the
     * buildingsurface:detailed and FenestrationSurface:Detailed objects
     */
    private void processOpaqueEnvelope() {
	if (!isExisting) {
	    envelopeProcessor.execute();
	}
    }
    
    //simple write out method, needs to be update later
    public void writeBaselineIdf(){
	baselineModel.WriteIdf(energyplusFile.getParentFile().getAbsolutePath(), "Baseline");
    }
}
