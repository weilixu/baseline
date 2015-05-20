package baseline.generator;

import java.io.File;
import java.io.IOException;

import baseline.construction.opaque.BaselineEnvelope;
import baseline.htmlparser.SizingHTMLParser;
import baseline.hvac.BaselineHVAC;
import baseline.idfdata.EnergyPlusBuilding;
import baseline.runeplus.SizingRun;
import baseline.util.BuildingType;
import baseline.util.ClimateZone;

public class Generator {

    private boolean isExisting = false;

    private final IdfReader designModel;
    private final IdfReader baselineModel;
    private final ClimateZone cZone;
    private final SizingRun eplusSizing;

    private BaselineEnvelope envelopeProcessor;
    private BaselineHVAC baselineHVAC;
    
    private final File energyplusFile;
    private final File weatherFile;
    
    private File htmlOutput;
    
    private EnergyPlusBuilding building;

    public Generator(File idfFile, File wea, ClimateZone zone,
	    boolean existing) {
	// identify the climate zone
	cZone = zone;
	isExisting = existing;

	// establish the design model
	energyplusFile = idfFile;
	weatherFile = wea;
	
	designModel = new IdfReader();
	designModel.setFilePath(energyplusFile.getAbsolutePath());
	try {
	    designModel.readEplusFile();
	} catch (IOException e) {
	    e.printStackTrace();
	    // cannot read the design file, check directory
	}
	//set-up sizing simulator
	
	baselineModel = designModel.cloneIdf();
	
	eplusSizing = new SizingRun(weatherFile);
	modifyOutput();

	envelopeProcessor = new BaselineEnvelope(baselineModel, cZone);
	//change the envelope materials and lighting power densities
	processOpaqueEnvelope();
	processLighting();
	
	//run sizing simulations
	try {
	    sizingRun();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	
	System.out.println("Finish sizing round");
	building = new EnergyPlusBuilding(cZone, baselineModel);
	
	//for test only
	//htmlOutput = new File("C:\\Users\\Weili\\Desktop\\AssetScoreTool\\1MPTest\\BaselineTable.html");
	
	SizingHTMLParser.processOutputs(htmlOutput);
	SizingHTMLParser.extractBldgBasicInfo(building);
	SizingHTMLParser.extractThermalZones(building);
	building.processModelInfo();
	
	buildingHVAC();
    }

    
    private void modifyOutput(){
	//change output units
	baselineModel.removeEnergyPlusObject("OutputControl:Table:Style");
	String[] objectValue = {"HTML","JtoKWH"};
	String[] objectDes = {"Column Separator","Unit Conversion"};
	baselineModel.addNewEnergyPlusObject("OutputControl:Table:Style",objectValue,objectDes);
	
	//change the reporting tolerances
	String[] toleranceValue = {"0.556","0.556"};
	String[] toleranceDes = {"Tolerance for Time Heating Setpoint Not Met", "Tolerance for Time Cooling Setpoint Not Met"};
	baselineModel.addNewEnergyPlusObject("OutputControl:ReportingTolerances",toleranceValue,toleranceDes);
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
    
    /**
     * Process all the lighting power densities according to table 9.5.1 or table 9.6.1
     * This will implement later...
     */
    private void processLighting(){
	
    }
    
    private void buildingHVAC(){
	BuildingType bldgType = BuildingType.NONRESIDENTIAL;
	baselineHVAC = new BaselineHVAC(bldgType,building);
	baselineHVAC.selectSystem();
	try{
		baselineHVAC.replaceHVACObjects();
		baselineHVAC.getBaseline().WriteIdf(energyplusFile.getParentFile().getAbsolutePath(), "Baseline");
		eplusSizing.setEplusFile(new File(energyplusFile.getParentFile().getAbsolutePath()+"\\Baseline.idf"));
		htmlOutput = eplusSizing.runEnergyPlus();
	}catch(IOException e){
	    e.printStackTrace();
	}
    }
    
    //simple write out method, needs to be update later
    private void sizingRun() throws IOException{
	baselineModel.WriteIdf(energyplusFile.getParentFile().getAbsolutePath(), "Baseline");
	eplusSizing.setEplusFile(new File(energyplusFile.getParentFile().getAbsolutePath()+"\\Baseline.idf"));
	htmlOutput = eplusSizing.runEnergyPlus();
    }
}
