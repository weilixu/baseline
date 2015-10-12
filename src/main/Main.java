package main;

import java.io.File;
import java.io.IOException;

import baseline.generator.Generator;
import baseline.util.BuildingType;
import baseline.util.ClimateZone;

public class Main {

    
    public static void main(String[] args) throws IOException{
	//File energyplusFile = new File("C:\\Users\\Weili\\Desktop\\AssetScoreTool\\1MPTest\\1MP.idf");
	//File weatherFile = new File("C:\\Uers\\Weili\\Desktop\\AssetScoreTool\\1MPTest\\USA_PA_Pittsburgh-Allegheny.County.AP.725205_TMY3");
	File energyplusFile = new File("E:\\02_Weili\\01_Projects\\12_ILEED\\Standard_Model\\Automate\\SH_VRF\\Scaife_Hall_Simple_VRF.idf");
	File weatherFile = new File("E:\\02_Weili\\01_Projects\\12_ILEED\\Standard_Model\\Automate\\SH_VRF\\USA_MD_Baltimore-Washington.Intl.AP.724060_TMY3");
	
	ClimateZone zone = ClimateZone.CLIMATEZONE4A;
	String tool = "DesignBuilder";
	//String tool = "Asset Score Tool";
	
	Generator generator = new Generator(energyplusFile,weatherFile,zone,"Office",false, tool);
	System.out.println("done!");
	System.out.println("this is a test!");
    }
}
