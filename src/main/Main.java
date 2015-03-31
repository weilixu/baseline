package main;

import java.io.File;
import java.io.IOException;

import baseline.generator.Generator;
import baseline.util.ClimateZone;

public class Main {

    
    public static void main(String[] args) throws IOException{
	File energyplusFile = new File("C:\\Users\\Weili\\Desktop\\AssetScoreTool\\TESTFile\\CSL_SKYLIGHT.idf");
	File weatherFile = new File("C:\\Users\\Weili\\Desktop\\AssetScoreTool\\TESTFile\\USA_PA_Pittsburgh-Allegheny.County.AP.725205_TMY3");
	ClimateZone zone = ClimateZone.CLIMATEZONE3;
	
	Generator generator = new Generator(energyplusFile,weatherFile,zone,false);
	generator.writeBaselineIdf();
	System.out.println("done!");
    }
}
