package main;

import java.io.File;

import baseline.generator.Generator;
import baseline.util.ClimateZone;

public class Main {

    
    public static void main(String[] args){
	File energyplusFile = new File("C:\\Users\\Weili\\Desktop\\AssetScoreTool\\TESTFile\\CSL_SKYLIGHT.idf");
	File weatherFile = new File("");
	ClimateZone zone = ClimateZone.CLIMATEZONE3;
	
	Generator generator = new Generator(energyplusFile,weatherFile,zone,false);
	generator.writeBaselineIdf();
	System.out.println("done!");
    }
}
