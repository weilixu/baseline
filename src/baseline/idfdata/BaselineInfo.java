package baseline.idfdata;

public class BaselineInfo {
    private String heatSource;
    
    /*
     * System 1-10 data
     */
    private String systemType;
    private String systemDesignation;
    private int numOfSystem;
    private double coolingCapacity;
    private double[] unitaryCoolingCapacity;
    private double coolingEER;
    private double coolingIEER;
    private double heatingCpacity;
    private double heatingEfficiency;
    private double UnitaryHeatingCOP;
    private String fanControlType;
    private double SupplyAirFlow;
    private double outdoorAirFlow;
    private boolean demandControl;
    private double hasEconomizer;
    private boolean energyRecovery;
    private double recoveryEffect;
    private double fanPower;
    private double returnFanPower;
    
    /**
     * Water-side HVAC Baseline
     */
    private int numChiller;
    private double chillerCapacity;
    private double chillerCOP;
    private double chillerIPLV;
    private int numCHWPump;
    private double chwPumpFlow;
    private double cwPumpFlow;
    private double buildingArea;
    private double boilerCapacity;
    private double boilerEfficiency;
    private double hwPumpFlow;
    
    /**
     * results related data
     */
    private String baselineDirectory;
    private final String BASELINE = "baseline_0";
    private final String BASELINE90 = "baseline_90";
    private final String BASELINE180 = "baseline_180";
    private final String BASELINE270 = "baseline_270";
    
    public String getHeatSource() {
        return heatSource;
    }
    public void setHeatSource(String heatSource) {
        this.heatSource = heatSource;
    }
    public String getSystemType() {
        return systemType;
    }
    public void setSystemType(String systemType) {
        this.systemType = systemType;
    }
    public String getSystemDesignation() {
        return systemDesignation;
    }
    public void setSystemDesignation(String systemDesignation) {
        this.systemDesignation = systemDesignation;
    }
    public int getNumOfSystem() {
        return numOfSystem;
    }
    public void setNumOfSystem(int numOfSystem) {
        this.numOfSystem = numOfSystem;
    }
    public double getCoolingCapacity() {
        return coolingCapacity;
    }
    public void setCoolingCapacity(double coolingCapacity) {
        this.coolingCapacity = coolingCapacity;
    }
    public double[] getUnitaryCoolingCapacity() {
        return unitaryCoolingCapacity;
    }
    public void setUnitaryCoolingCapacity(double[] unitaryCoolingCapacity) {
        this.unitaryCoolingCapacity = unitaryCoolingCapacity;
    }
    public double getCoolingEER() {
        return coolingEER;
    }
    public void setCoolingEER(double coolingEER) {
        this.coolingEER = coolingEER;
    }
    public double getCoolingIEER() {
        return coolingIEER;
    }
    public void setCoolingIEER(double coolingIEER) {
        this.coolingIEER = coolingIEER;
    }
    public double getHeatingCpacity() {
        return heatingCpacity;
    }
    public void setHeatingCpacity(double heatingCpacity) {
        this.heatingCpacity = heatingCpacity;
    }
    public double getHeatingEfficiency() {
        return heatingEfficiency;
    }
    public void setHeatingEfficiency(double heatingEfficiency) {
        this.heatingEfficiency = heatingEfficiency;
    }
    public double getUnitaryHeatingCOP() {
        return UnitaryHeatingCOP;
    }
    public void setUnitaryHeatingCOP(double unitaryHeatingCOP) {
        UnitaryHeatingCOP = unitaryHeatingCOP;
    }
    public String getFanControlType() {
        return fanControlType;
    }
    public void setFanControlType(String fanControlType) {
        this.fanControlType = fanControlType;
    }
    public double getSupplyAirFlow() {
        return SupplyAirFlow;
    }
    public void setSupplyAirFlow(double supplyAirFlow) {
        SupplyAirFlow = supplyAirFlow;
    }
    public double getOutdoorAirFlow() {
        return outdoorAirFlow;
    }
    public void setOutdoorAirFlow(double outdoorAirFlow) {
        this.outdoorAirFlow = outdoorAirFlow;
    }
    public boolean isDemandControl() {
        return demandControl;
    }
    public void setDemandControl(boolean demandControl) {
        this.demandControl = demandControl;
    }
    public double getHasEconomizer() {
        return hasEconomizer;
    }
    public void setHasEconomizer(double hasEconomizer) {
        this.hasEconomizer = hasEconomizer;
    }
    public boolean isEnergyRecovery() {
        return energyRecovery;
    }
    public void setEnergyRecovery(boolean energyRecovery) {
        this.energyRecovery = energyRecovery;
    }
    public double getRecoveryEffect() {
        return recoveryEffect;
    }
    public void setRecoveryEffect(double recoveryEffect) {
        this.recoveryEffect = recoveryEffect;
    }
    public double getFanPower() {
        return fanPower;
    }
    public void setFanPower(double fanPower) {
        this.fanPower = fanPower;
    }
    public double getReturnFanPower() {
        return returnFanPower;
    }
    public void setReturnFanPower(double returnFanPower) {
        this.returnFanPower = returnFanPower;
    }
    public int getNumChiller() {
        return numChiller;
    }
    public void setNumChiller(int numChiller) {
        this.numChiller = numChiller;
    }
    public double getChillerCapacity() {
        return chillerCapacity;
    }
    public void setChillerCapacity(double chillerCapacity) {
        this.chillerCapacity = chillerCapacity;
    }
    public double getChillerCOP() {
        return chillerCOP;
    }
    public void setChillerCOP(double chillerCOP) {
        this.chillerCOP = chillerCOP;
    }
    public double getChillerIPLV() {
        return chillerIPLV;
    }
    public void setChillerIPLV(double chillerIPLV) {
        this.chillerIPLV = chillerIPLV;
    }
    public int getNumCHWPump() {
        return numCHWPump;
    }
    public void setNumCHWPump(int numCHWPump) {
        this.numCHWPump = numCHWPump;
    }
    public double getChwPumpFlow() {
        return chwPumpFlow;
    }
    public void setChwPumpFlow(double chwPumpFlow) {
        this.chwPumpFlow = chwPumpFlow;
    }
    public double getCwPumpFlow() {
        return cwPumpFlow;
    }
    public void setCwPumpFlow(double cwPumpFlow) {
        this.cwPumpFlow = cwPumpFlow;
    }
    public double getBuildingArea() {
        return buildingArea;
    }
    public void setBuildingArea(double buildingArea) {
        this.buildingArea = buildingArea;
    }
    public double getBoilerCapacity() {
        return boilerCapacity;
    }
    public void setBoilerCapacity(double boilerCapacity) {
        this.boilerCapacity = boilerCapacity;
    }
    public double getBoilerEfficiency() {
        return boilerEfficiency;
    }
    public void setBoilerEfficiency(double boilerEfficiency) {
        this.boilerEfficiency = boilerEfficiency;
    }
    public double getHwPumpFlow() {
        return hwPumpFlow;
    }
    public void setHwPumpFlow(double hwPumpFlow) {
        this.hwPumpFlow = hwPumpFlow;
    }
    public String getBaselineDirectory() {
        return baselineDirectory;
    }
    public void setBaselineDirectory(String baselineDirectory) {
        this.baselineDirectory = baselineDirectory;
    }
    public String getBASELINE() {
        return BASELINE;
    }
    public String getBASELINE90() {
        return BASELINE90;
    }
    public String getBASELINE180() {
        return BASELINE180;
    }
    public String getBASELINE270() {
        return BASELINE270;
    } 
}
