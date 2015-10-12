package baseline.generator;

import java.io.File;
import java.io.IOException;

import baseline.construction.opaque.BaselineEnvelope;
import baseline.htmlparser.SizingHTMLParser;
import baseline.hvac.BaselineHVAC;
import baseline.idfdata.BuildingLight;
import baseline.idfdata.EnergyPlusBuilding;
import baseline.lighting.LightingGenerator;
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
    private LightingGenerator lightGenerator;
    private BaselineHVAC baselineHVAC;
    
    private final File energyplusFile;
    private final File weatherFile;
    
    private File htmlOutput;
    
    private EnergyPlusBuilding building;
    private String bldgType;
    private String tool;

    public Generator(File idfFile, File wea, ClimateZone zone, String buildingType,
	    boolean existing, String tool) {
	// identify the climate zone
	cZone = zone;
	isExisting = existing;
	bldgType = buildingType;

	// establish the design model
	energyplusFile = idfFile;
	weatherFile = wea;
	this.tool = tool;
	
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
	
	//run first sizing simulations-
	//this simulation is mainly to create abstract building info
	//on top of the basic data structure
	try {
	    firstSizingRun();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	//creating the abstract building info for deeper level information process
	System.out.println("Finish first round sizing");
	//
	//
	//debug purpose
	//
	//
	//htmlOutput = new File("E:\\02_Weili\\01_Projects\\12_ILEED\\Standard_Model\\Automate\\BaselineTable.html");
	building = new EnergyPlusBuilding(bldgType,cZone, baselineModel);
	//for test only
	//htmlOutput = new File("C:\\Users\\Weili\\Desktop\\AssetScoreTool\\1MPTest\\BaselineTable.html");
	SizingHTMLParser.setTool(this.tool);
	SizingHTMLParser.processOutputs(htmlOutput);
	SizingHTMLParser.extractBldgBasicInfo(building);
	SizingHTMLParser.extractThermalZones(building);
	building.processModelInfo();
	
	envelopeProcessor = new BaselineEnvelope(building);
	lightGenerator = new LightingGenerator(building);
	//change the envelope materials and lighting power densities
	processOpaqueEnvelope();
	lightGenerator.processBuildingTypeLPD();
	//modify lighting and WWR Skylights
	processWindowToWallRatio();
	//second round of sizing simulation - to provide update thermal load
	try {
	    sizingRun();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	System.out.println("Finish second round sizing");
	//IdfReader updatedReader = building.getBaselineModel();
	building = new EnergyPlusBuilding(bldgType,cZone, baselineModel);
	//reprocess the building abstract information
	SizingHTMLParser.setTool(this.tool);
	SizingHTMLParser.processOutputs(htmlOutput);
	SizingHTMLParser.extractBldgBasicInfo(building);
	SizingHTMLParser.extractThermalZones(building);
	building.processModelInfo();

	//build HVAC system
	buildingHVAC();
    }

    
    private void modifyOutput(){
	//change output units
	baselineModel.removeEnergyPlusObject("OutputControl:Table:Style");
	baselineModel.removeEnergyPlusObject("OutputControl:ReportingTolerances");
	baselineModel.removeEnergyPlusObject("Output:Meter");
	baselineModel.removeEnergyPlusObject("Output:Variable");
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
	    envelopeProcessor.replaceConstruction();;
	}
    }
    
    private void processWindowToWallRatio(){
	
    }
    
    /**
     * Process all the lighting power densities according to table 9.5.1 or table 9.6.1
     * This will implement later...
     */
//    private void processLighting(BuildingLight bldg){
//	LightingGenerator lgtGen = new LightingGenerator(bldg);
//	lgtGen.processBuildingTypeLPD();
//    }
    
    private void buildingHVAC(){
	BuildingType bldgType = BuildingType.NONRESIDENTIAL;
	baselineHVAC = new BaselineHVAC(bldgType,building);
	baselineHVAC.selectSystem();
	try{
		baselineHVAC.replaceHVACObjects();
		building.generateEnergyPlusModel(energyplusFile.getParentFile().getAbsolutePath(), "Baseline");
		eplusSizing.setEplusFile(new File(energyplusFile.getParentFile().getAbsolutePath()+"\\Baseline.idf"));
		htmlOutput = eplusSizing.runEnergyPlus();
	}catch(IOException e){
	    e.printStackTrace();
	}
    }
    
    private void sizingRun() throws IOException{
	building.generateEnergyPlusModel(energyplusFile.getParentFile().getAbsolutePath(), "Baseline");
	eplusSizing.setEplusFile(new File(energyplusFile.getParentFile().getAbsolutePath()+"\\Baseline.idf"));;
	htmlOutput = eplusSizing.runEnergyPlus();
	System.out.println(htmlOutput.getAbsolutePath());
    }
    
    //simple write out method, needs to be update later
    private void firstSizingRun() throws IOException{
	baselineModel.WriteIdf(energyplusFile.getParentFile().getAbsolutePath(), "Baseline");
	eplusSizing.setEplusFile(new File(energyplusFile.getParentFile().getAbsolutePath()+"\\Baseline.idf"));
	htmlOutput = eplusSizing.runEnergyPlus();
	System.out.println(htmlOutput.getAbsolutePath());
    }
}
