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

	processOpaqueEnvelope();
    }
    
    private void processOpaqueEnvelope(){
	
    }
    
    
    
}
