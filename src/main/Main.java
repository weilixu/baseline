package main;

import java.io.File;
import java.io.IOException;

import baseline.generator.Generator;
import baseline.util.BuildingType;
import baseline.util.ClimateZone;

public class Main {

    
    public static void main(String[] args) throws IOException{
	File energyplusFile = new File("C:\\Users\\Weili\\Desktop\\AssetScoreTool\\1MPTest\\1MP.idf");
	File weatherFile = new File("C:\\Users\\Weili\\Desktop\\AssetScoreTool\\1MPTest\\USA_PA_Pittsburgh-Allegheny.County.AP.725205_TMY3");
	ClimateZone zone = ClimateZone.CLIMATEZONE5A;
	
	Generator generator = new Generator(energyplusFile,weatherFile,zone,false);
	System.out.println("done!");
	System.out.println("this is a test!");
    }
}
