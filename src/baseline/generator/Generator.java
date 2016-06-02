package baseline.generator;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import baseline.construction.opaque.BaselineEnvelope;
import baseline.htmlparser.SizingHTMLParser;
import baseline.htmlparser.WindowWallRatioParser;
import baseline.hvac.BaselineHVAC;
import baseline.idfdata.BaselineInfo;
import baseline.idfdata.IdfReader;
import baseline.idfdata.building.EnergyPlusBuilding;
import baseline.lighting.LightingGenerator;
import baseline.runeplus.SizingRun;
import baseline.util.BuildingType;
import baseline.util.ClimateZone;
import lepost.shared.StatusMonitor;

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

    public Generator(File idfFile, 
    				 File wea, 
    				 ClimateZone zone,
    				 String buildingType, 
    				 boolean existing, 
    				 String tool,
    				 StatusMonitor monitor,
    				 String key,
    				 GeneratorStatusReport report) {
    	
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
	    
	    report.processError("Reading uploaed design model has error: "+e.getMessage(), null);
	}
	// set-up sizing simulator

	baselineModel = designModel.cloneIdf();

	eplusSizing = new SizingRun(weatherFile);
	modifyOutput();
	
	monitor.updateStatus(key, "Analyze design case...", false);

	// run first sizing simulations-
	// this simulation is mainly to create abstract building info
	// on top of the basic data structure
	try {
	    if(!firstSizingRun()){
	    	report.processError("Runing design model encounters problem", "Baseline_0.err");
	    	return;
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}
	// creating the abstract building info for deeper level information
	// process
	System.out.println("Finish first round sizing");
	
	monitor.updateStatus(key, "Analyze complete", false);
	monitor.updateStatus(key, "Generate baseline models...", false);
	
	//
	//
	// debug purpose
	//
	IdfReader temp = baselineModel.cloneIdf();
	building = new EnergyPlusBuilding(bldgType,cZone,temp, null);
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
	
	try{
	    if(!sizingRun()){
	    	report.processError("Runing generated Baseline 0 encountered error", "Baseline_0.err");
	    }
	}catch(IOException e){
	    e.printStackTrace();
	}
	System.out.println("Finish second round sizing");
	
	monitor.updateStatus(key, "Baseline models generation complete", false);
	monitor.updateStatus(key, "Run simulation on baseline models...", false);
	
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
	    baselineSimulation(monitor, key, report);
	    if(!report.isSuccess()){
	    	return;
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}
	postprocessInfo();
	
	monitor.updateStatus(key, "Simulations on baseline models complete", true);
	
	fileCleaner();
    }
    
    public BaselineInfo getBaselineInfo(){
	return building.getInfoObject();
    }

    private void modifyOutput() {
	// change output units
	baselineModel.removeEnergyPlusObject("OutputControl:Table:Style");
	baselineModel
		.removeEnergyPlusObject("OutputControl:ReportingTolerances");
	baselineModel.removeEnergyPlusObject("Output:Table:SummaryReports");
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
	
	//change the report summaries
	String[] reports = {"AllSummary","ZoneComponentLoadSummary"};
	String[] reportsDes = {"Report 1 Name","Report 2 Name"};
	baselineModel.addNewEnergyPlusObject("Output:Table:SummaryReports",reports, reportsDes);
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
	BuildingType type;
	if(bldgType.equals("Residential")){
	    type = BuildingType.RESIDENTIAL;
	}else{
	    type = BuildingType.NONRESIDENTIAL;
	}
	baselineHVAC = new BaselineHVAC(type, building);
	baselineHVAC.selectSystem();
	try {
	    baselineHVAC.replaceHVACObjects();
	} catch (IOException e) {
	    e.printStackTrace();
	}

    }

    private boolean sizingRun() throws IOException {
	building.generateEnergyPlusModel(energyplusFile.getParentFile()
		.getAbsolutePath(), "Baseline_0","0");
	eplusSizing.setEplusFile(new File(energyplusFile.getParentFile()
		.getAbsolutePath() + "\\Baseline_0.idf"));
	eplusSizing.setBaselineSizing();
	htmlOutput = eplusSizing.runEnergyPlus();
	
	if(htmlOutput != null){
		return true;
	}
	return false;
    }

    // simple write out method, needs to be update later
    private boolean firstSizingRun() throws IOException {
	baselineModel.WriteIdf(
		energyplusFile.getParentFile().getAbsolutePath(), "Baseline_0");
	eplusSizing.setEplusFile(new File(energyplusFile.getParentFile()
		.getAbsolutePath() + "\\Baseline_0.idf"));
	htmlOutput = eplusSizing.runEnergyPlus();

	if(htmlOutput != null){
		return true;
	}
	return false;
    }

    private void baselineSimulation(StatusMonitor monitor, String key, GeneratorStatusReport report) throws IOException {
	String[] baselineList = { "0", "90", "180", "270" };
	for (String degree : baselineList) {
		monitor.updateStatus(key, "Doing simulation on orientation "+degree+" degree...", false);
		
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
	    
	    monitor.updateStatus(key, "Simulation on orientation "+degree+" degree finished", false);
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
	}else if(building.getInfoObject().getSystemType().equals("System Type 5")){
	    building.getInfoObject().setHwPumpFlow(SizingHTMLParser.getPumpWaterFlowRate("HW"));
	}
    }
    
    protected void fileCleaner(){
    	FileFilter ff = new FileFilter(){
			@Override
			public boolean accept(File f) {
				String name = f.getName().toLowerCase();
				
				if(name.endsWith("idf") || name.endsWith("html")
						|| name.endsWith("err") || name.endsWith("epw")
						|| name.endsWith("savedinfo")){
					return false;
				}
				
				return true;
			}
    	};
    	
    	File dir = new File(energyplusFile.getParent()+"\\");
    	File[] files = dir.listFiles(ff);
    	
    	for(File f : files){
    		f.delete();
    	}
    }
}
