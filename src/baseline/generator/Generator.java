package baseline.generator;

import java.io.File;
import java.io.IOException;

import baseline.util.ClimateZone;

public class Generator {
    
    private final IdfReader designModel;
    private final IdfReader baselineModel;
    private final ClimateZone cZone;
    
    public Generator(File idfFile, File weatherFile,ClimateZone zone){
	//identify the climate zone
	cZone = zone;
	
	//establish the design model
	designModel = new IdfReader();
	designModel.setFilePath(idfFile.getAbsolutePath());
	try{
	    designModel.readEplusFile();
	}catch(IOException e){
	    e.printStackTrace();
	    //cannot read the design file, check directory
	}
	baselineModel = designModel.cloneIdf();
	
	processThermalZones();
	processOpaqueEnvelope();
    }
    
    private void processThermalZones(){
	
    }
    
    /**
     * process all the envelopes. This process includes:
     * 1. Delete the original constructions
     * 2. Replace with a standard set of baseline constructions with respect to the thermal zones
     * 3. Replace the construction name in the buildingsurface:detailed and FenestrationSurface:Detailed objects
     */
    private void processOpaqueEnvelope(){
	
    }
    
    
    
}
