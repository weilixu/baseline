package main;

import java.io.File;
import java.io.IOException;

import baseline.generator.Generator;
import baseline.util.ClimateZone;

public class Main {

    public static void main(String[] args) throws IOException{
	//File energyplusFile = new File("C:\\Users\\Weili\\Desktop\\AssetScoreTool\\1MPTest\\1MP.idf");
	//File weatherFile = new File("C:\\Uers\\Weili\\Desktop\\AssetScoreTool\\1MPTest\\USA_PA_Pittsburgh-Allegheny.County.AP.725205_TMY3.epw");
	//File energyplusFile = new File("E:\\02_Weili\\01_Projects\\12_ILEED\\Standard_Model\\Automate\\SH_FCU\\Scaife_Hall_Base.idf");
	//File weatherFile = new File("E:\\02_Weili\\01_Projects\\12_ILEED\\Standard_Model\\Automate\\SH_FCU\\USA_MD_Baltimore-Washington.Intl.AP.724060_TMY3.epw");
	//File energyplusFile = new File("E:\\02_Weili\\01_Projects\\07_Toshiba\\Year 2\\EnergyModel\\Baseline\\Baseline_Test.idf");
	//File weatherFile = new File("E:\\02_Weili\\01_Projects\\07_Toshiba\\Year 2\\EnergyModel\\Baseline\\JPN_Tokyo.Hyakuri.477150_IWEC");	
	//File energyplusFile = new File("E:\\02_Weili\\01_Projects\\12_ILEED\\Sample Buildings Folder\\System Type 3\\Sys3_Sample.idf");
	//File weatherFile = new File("E:\\02_Weili\\01_Projects\\12_ILEED\\Sample Buildings Folder\\System Type 3\\USA_MD_Baltimore-Washington.Intl.AP.724060_TMY3.epw");	
	//File energyplusFile = new File("E:\\02_Weili\\01_Projects\\12_ILEED\\Sample Buildings Folder\\System Type 5\\Sys5_Sample.idf");
	//File weatherFile = new File("E:\\02_Weili\\01_Projects\\12_ILEED\\Sample Buildings Folder\\System Type 5\\USA_MD_Baltimore-Washington.Intl.AP.724060_TMY3.epw");	
	//File energyplusFile = new File("E:\\02_Weili\\01_Projects\\12_ILEED\\Sample Buildings Folder\\System Type 7\\DistrictHeatCool\\Sys7_Sample.idf");
	//File weatherFile = new File("E:\\02_Weili\\01_Projects\\12_ILEED\\Sample Buildings Folder\\System Type 7\\DistrictHeatCool\\USA_MD_Baltimore-Washington.Intl.AP.724060_TMY3.epw");	
	//File energyplusFile = new File("E:\\02_Weili\\01_Projects\\12_ILEED\\Sample Buildings Folder\\System Type 8\\DistrictCool\\Sys8_Sample.idf");
	//File weatherFile = new File("E:\\02_Weili\\01_Projects\\12_ILEED\\Sample Buildings Folder\\System Type 8\\DistrictCool\\USA_MD_Baltimore-Washington.Intl.AP.724060_TMY3.epw");	
	ClimateZone zone = ClimateZone.CLIMATEZONE3A;
	//String tool = "DesignBuilder";
	//String tool = "Asset Score Tool";
	//File energyplusFile = new File("E:\\02_Weili\\01_Projects\\12_ILEED\\Sample Buildings Folder\\System Type 4\\Sys4_Sample.idf");
	//File weatherFile = new File("E:\\02_Weili\\01_Projects\\12_ILEED\\Sample Buildings Folder\\System Type 4\\USA_MD_Baltimore-Washington.Intl.AP.724060_TMY3.epw");

	//File energyplusFile = new File("E:\\02_Weili\\01_Projects\\12_ILEED\\Sample Buildings Folder\\System Type 3\\DistrictHeatCool\\Sys3_Sample.idf");
	//File weatherFile = new File("E:\\02_Weili\\01_Projects\\12_ILEED\\Sample Buildings Folder\\System Type 3\\DistrictHeatCool\\USA_MD_Baltimore-Washington.Intl.AP.724060_TMY3.epw");	
	//File energyplusFile = new File("C:\\Users\\Weili\\workspace\\.metadata\\.plugins\\org.eclipse.wst.server.core\\tmp0\\wtpwebapps\\MyWebProject\\74dbe85dd58226b2a5999a021a91b25e\\test.idf");
	//File weatherFile = new File("C:\\Users\\Weili\\workspace\\.metadata\\.plugins\\org.eclipse.wst.server.core\\tmp0\\wtpwebapps\\MyWebProject\\74dbe85dd58226b2a5999a021a91b25e\\weather.epw");	
	//Generator generator = new Generator(energyplusFile,weatherFile,zone,"Office",false, "DesignBuilder");
	
	//File asteplusFile = new File("E:\\02_Weili\\01_Projects\\12_ILEED\\PaperTest\\LEED Rating\\1mp.idf");
	//File astweatherFile = new File("E:\\02_Weili\\01_Projects\\12_ILEED\\PaperTest\\LEED Rating\\KPHL_10074_9999_amy.epw");
	
	//Generator astgenerator = new Generator(asteplusFile,astweatherFile,zone,"Office",false, "Asset Score Tool");
	File domeplusFile = new File("E:\\02_Weili\\01_Projects\\12_ILEED\\Sample Buildings Folder\\System Type 1\\Standard\\Sys1_Sample.idf");
	File domweatherFile = new File("E:\\02_Weili\\01_Projects\\12_ILEED\\Sample Buildings Folder\\System Type 1\\Standard\\USA_MD_Baltimore-Washington.Intl.AP.724060_TMY3.epw");
	
	Generator domgenerator = new Generator(domeplusFile,domweatherFile,zone,"Residential",false, "DesignBuilder");

	System.out.println("done!");
	System.out.println("this is a test!");
    }
}
