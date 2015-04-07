package baseline.hvac;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import baseline.generator.IdfReader;
import baseline.idfdata.EnergyPlusBuilding;
import baseline.util.ClimateZone;

public class BaselineHVAC {
    private final IdfReader baselineModel;
    
    //use for determine whether need a economizer or not
    private final ClimateZone zone;
    private final EnergyPlusBuilding building;
    
    //EnergyPlus Objects that requrie to be removed
    private static final String FILE_NAME = "HVACObjects.txt";
    private String[] objectList;
    
    public BaselineHVAC(IdfReader m, ClimateZone c, EnergyPlusBuilding bldg){
	building = bldg;
	baselineModel = m;
	zone = c;
    }
    
    /**
     * Remove the whole HVAC system in the original file
     * @throws IOException
     */
    public void removeHVACObjects() throws IOException{
	processObjectLists();
	for(String s: objectList){
	    baselineModel.removeEnergyPlusObject(s);
	}
    }
    
    //HVAC objects list is read from local list file
    private void processObjectLists() throws IOException{
	BufferedReader br = new BufferedReader(new FileReader(FILE_NAME));
	
	try{
	    StringBuilder sb = new StringBuilder();
	    String line = br.readLine();
	    
	    while(line !=null){
		sb.append(line);
		sb.append(":");
		line = br.readLine();
	    }
	    objectList = sb.toString().split(":");
	}finally{
	    br.close();
	}
    }
    

}
