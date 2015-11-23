package baseline.generator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import baseline.construction.opaque.BaselineEnvelope;
import baseline.generator.IdfReader.ValueNode;
import baseline.htmlparser.SizingHTMLParser;
import baseline.htmlparser.WindowWallRatioParser;
import baseline.hvac.BaselineHVAC;
import baseline.idfdata.BaselineInfo;
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

    /*
     * generation engines
     */
    private BaselineEnvelope envelopeProcessor;
    private LightingGenerator lightGenerator;
    private BaselineHVAC baselineHVAC;
    private WindowWallRatioParser wwrParser;

    private final File energyplusFile;
    private final File weatherFile;

    private File htmlOutput;

    private EnergyPlusBuilding building;
    private String bldgType;
    private String tool;
    private BaselineInfo info;

    public Generator(File idfFile, File wea, ClimateZone zone,
	    String buildingType, boolean existing, String tool) {
	// identify the climate zone
	cZone = zone;
	isExisting = existing;
	bldgType = buildingType;
	info = new BaselineInfo();

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
	// set-up sizing simulator

	baselineModel = designModel.cloneIdf();

	eplusSizing = new SizingRun(weatherFile);
	modifyOutput();

	// run first sizing simulations-
	// this simulation is mainly to create abstract building info
	// on top of the basic data structure
	try {
	    firstSizingRun();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	// creating the abstract building info for deeper level information
	// process
	System.out.println("Finish first round sizing");
	//
	//
	// debug purpose
	//
	IdfReader sizeModel = baselineModel.cloneIdf();
	// htmlOutput = new
	// File("E:\\02_Weili\\01_Projects\\12_ILEED\\Standard_Model\\Automate\\BaselineTable.html");
	building = new EnergyPlusBuilding(bldgType, cZone, sizeModel, null);
	// for test only
	// htmlOutput = new
	// File("C:\\Users\\Weili\\Desktop\\AssetScoreTool\\1MPTest\\BaselineTable.html");
	SizingHTMLParser.setTool(this.tool);
	SizingHTMLParser.processOutputs(htmlOutput);
	SizingHTMLParser.extractBldgBasicInfo(building);
	SizingHTMLParser.extractThermalZones(building);
	building.processModelInfo();

	envelopeProcessor = new BaselineEnvelope(building);
	lightGenerator = new LightingGenerator(building);
	// change the envelope materials and lighting power densities
	processOpaqueEnvelope();
	lightGenerator.processBuildingTypeLPD();
	// modify lighting and WWR Skylights
	processWindowToWallRatio();
	// second round of sizing simulation - to provide update thermal load
	// build HVAC system
	buildingHVAC();

	try {
	    sizingRun();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	System.out.println("Finish third round sizing");
	// IdfReader updatedReader = building.getBaselineModel();
	building = new EnergyPlusBuilding(bldgType, cZone, baselineModel, info);
	// reprocess the building abstract information
	SizingHTMLParser.setTool(this.tool);
	SizingHTMLParser.processOutputs(htmlOutput);
	SizingHTMLParser.extractBldgBasicInfo(building);
	SizingHTMLParser.extractThermalZones(building);
	building.processModelInfo();
	envelopeProcessor = new BaselineEnvelope(building);
	lightGenerator = new LightingGenerator(building);
	// change the envelope materials and lighting power densities
	processOpaqueEnvelope();
	lightGenerator.processBuildingTypeLPD();
	// modify lighting and WWR Skylights
	processWindowToWallRatio();
	// second round of sizing simulation - to provide update thermal load
	// build HVAC system
	buildingHVAC();
	try {
	    baselineSimulation();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	postprocessInfo();
    }
    
    public BaselineInfo getBaselineInfo(){
	return building.getInfoObject();
    }

    private void modifyOutput() {
	// change output units
	baselineModel.removeEnergyPlusObject("OutputControl:Table:Style");
	baselineModel
		.removeEnergyPlusObject("OutputControl:ReportingTolerances");
	baselineModel.removeEnergyPlusObject("Output:Meter");
	baselineModel.removeEnergyPlusObject("Output:Variable");
	String[] objectValue = { "HTML", "JtoKWH" };
	String[] objectDes = { "Column Separator", "Unit Conversion" };
	baselineModel.addNewEnergyPlusObject("OutputControl:Table:Style",
		objectValue, objectDes);

	// change the reporting tolerances
	String[] toleranceValue = { "0.556", "0.556" };
	String[] toleranceDes = {
		"Tolerance for Time Heating Setpoint Not Met",
		"Tolerance for Time Cooling Setpoint Not Met" };
	baselineModel.addNewEnergyPlusObject(
		"OutputControl:ReportingTolerances", toleranceValue,
		toleranceDes);
    }

    /**
     * process all the envelopes. This process includes: 1. Delete the original
     * constructions 2. Replace with a standard set of baseline constructions
     * with respect to the thermal zones 3. Replace the construction name in the
     * buildingsurface:detailed and FenestrationSurface:Detailed objects
     */
    private void processOpaqueEnvelope() {
	if (!isExisting) {
	    envelopeProcessor.replaceConstruction();
	    ;
	}
    }

    /**
     * adjust window to wall ratio
     */
    private void processWindowToWallRatio() {
	wwrParser = new WindowWallRatioParser(building.getBaselineModel());
	wwrParser.adjustToThreshold();
    }

    /**
     * Process all the lighting power densities according to table 9.5.1 or
     * table 9.6.1 This will implement later...
     */
    // private void processLighting(BuildingLight bldg){
    // LightingGenerator lgtGen = new LightingGenerator(bldg);
    // lgtGen.processBuildingTypeLPD();
    // }

    private void buildingHVAC() {
	BuildingType bldgType = BuildingType.NONRESIDENTIAL;
	baselineHVAC = new BaselineHVAC(bldgType, building);
	baselineHVAC.selectSystem();
	try {
	    baselineHVAC.replaceHVACObjects();
	} catch (IOException e) {
	    e.printStackTrace();
	}

    }

    private void sizingRun() throws IOException {
	building.generateEnergyPlusModel(energyplusFile.getParentFile()
		.getAbsolutePath(), "Baseline_0","0");
	eplusSizing.setEplusFile(new File(energyplusFile.getParentFile()
		.getAbsolutePath() + "\\Baseline_0.idf"));
	eplusSizing.setBaselineSizing();
	htmlOutput = eplusSizing.runEnergyPlus();
	System.out.println(htmlOutput.getAbsolutePath());
    }

    // simple write out method, needs to be update later
    private void firstSizingRun() throws IOException {
	baselineModel.WriteIdf(
		energyplusFile.getParentFile().getAbsolutePath(), "Baseline_0");
	eplusSizing.setEplusFile(new File(energyplusFile.getParentFile()
		.getAbsolutePath() + "\\Baseline_0.idf"));
	htmlOutput = eplusSizing.runEnergyPlus();
	System.out.println(htmlOutput.getAbsolutePath());
    }

    private void baselineSimulation() throws IOException {
	String[] baselineList = { "0", "90", "180", "270" };
	for (String degree : baselineList) {
	    String filename = "Baseline" + "_" + degree;
		building.generateEnergyPlusModel(energyplusFile.getParentFile()
			.getAbsolutePath(), filename,degree);
	    eplusSizing.setEplusFile(new File(energyplusFile.getParentFile()
		    .getAbsolutePath() + "\\" + filename + ".idf"));
	    if(degree == "0"){
		htmlOutput = eplusSizing.runEnergyPlus();
	    }else{
		eplusSizing.runEnergyPlus();
	    }
	}
	building.getInfoObject().setBaselineDirectory(energyplusFile.getParentFile().getAbsolutePath());
    }
    
    private void postprocessInfo(){
	SizingHTMLParser.processOutputs(htmlOutput);

	if(building.getInfoObject().getSystemType().equals("System Type 7")){
	    building.getInfoObject().setChwPumpFlow(SizingHTMLParser.getPumpWaterFlowRate("CHW"));
	    building.getInfoObject().setCwPumpFlow(SizingHTMLParser.getPumpWaterFlowRate("CNDW"));
	    building.getInfoObject().setHwPumpFlow(SizingHTMLParser.getPumpWaterFlowRate("HW"));
	}else if(building.getInfoObject().getSystemType().equals("System Type 8")){
	    building.getInfoObject().setChwPumpFlow(SizingHTMLParser.getPumpWaterFlowRate("CHW"));
	    building.getInfoObject().setCwPumpFlow(SizingHTMLParser.getPumpWaterFlowRate("CNDW"));	    
	}
    }
}
