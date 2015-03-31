package baseline.runeplus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

/**
 * This class runs the EnergyPlus file for sizing purpose. The EnergyPlus
 * model's envelope thermal properties and lighting should be properly defined.
 * However, the window-wall ratio could be remain the same as proposed case.
 * This will be later judged from the sizing run results. If necessary,
 * fenestration area modification will be performed by other class and sizing
 * run is required to be performed again
 * 
 * This sizing simulation should not be version dependent, however, for the sake
 * of asset score tool usage, we fix it to V8.2 currently.
 * 
 * The Batch File is the EnergyPlus RunEplus.bat
 * 
 * @author Weili
 *
 */
public class SizingRun {
    private static final String EPLUSBAT = "RunEplus.bat";

    private File idfFile;
    private final File weatherFile;
    // folder should be the folder that contains the idf file
    private File folder;

    public SizingRun(File wea) {
	//idfFile = idf;
	weatherFile = wea;
    }
    
    /**
     * This method should always be called prior to runEnergyPlus method
     * @param idf
     */
    public void setEplusFile(File idf){
	idfFile = idf;
	folder = idfFile.getParentFile();
    }
    
    /**
     * Run EnergyPlus with defined idf file and weather file.
     * This is not parametric simulation - it only use for sizing purpose
     * This method will return the html results file once the simulation completed.
     * @throws IOException
     */
    public File runEnergyPlus() throws IOException {
	File resultsFile = null;
	
	String path = idfFile.getAbsolutePath();
	String pathToIDF = path.substring(0, path.indexOf("."));
	File eplusBatFile = createBatchFile();
	String[] commandline = { eplusBatFile.getAbsolutePath(), pathToIDF,
		weatherFile.getName() };

	try {
	    Process p = Runtime.getRuntime().exec(commandline, null, folder);
	    ThreadedInputStream errStr = new ThreadedInputStream(
		    p.getErrorStream());
	    errStr.start();
	    ThreadedInputStream outStr = new ThreadedInputStream(
		    p.getInputStream());
	    outStr.start();
	    p.waitFor();

	    errStr.join();
	    outStr.join();
	    eplusBatFile.delete();
	} catch (IOException | InterruptedException e) {
	    e.printStackTrace();
	}
	
	for(File f:folder.listFiles()){
	    if(f.getName().contains("html")){
		resultsFile = f;
	    }
	}
	return resultsFile;
    }

    /**
     * copy the batch file to the target folder with modified eplus directory
     * and weather file directory
     * 
     * @return
     * @throws IOException
     */
    private File createBatchFile() throws IOException {
	String keyWord = "set program_path=";
	String weaWord = "set weather_path=";
	File file = new File(folder.getAbsolutePath() + "\\" + EPLUSBAT);
	file.createNewFile();

	// reading file and write to the new file

	BufferedReader br = new BufferedReader(new FileReader(EPLUSBAT));
	StringBuilder sb = new StringBuilder();

	try {
	    String line = br.readLine();

	    while (line != null) {
		if (line.contains(keyWord)) {
		    sb.append(keyWord);
		    sb.append("E:\\01_Software\\EnergyPlusV8-2-0\\"); // fixed
								      // version
		} else if (line.contains(weaWord)) {
		    sb.append(weaWord);
		    sb.append(folder.getAbsolutePath() + "\\");
		} else {
		    sb.append(line);
		}
		sb.append(System.lineSeparator());
		line = br.readLine();
	    }
	} finally {
	    FileWriter results = null;
	    try {
		results = new FileWriter(file, true);
		PrintWriter pw = new PrintWriter(results);
		pw.append(sb.toString());
		pw.close();
	    } catch (IOException e) {
		// some warning??
	    }
	    // close the file
	    br.close();
	}
	return file;
    }

    private class ThreadedInputStream extends Thread {
	private IOException ioExc;
	private InputStream is;
	private StringBuffer sb = null;

	public ThreadedInputStream(InputStream inputStream) {
	    is = inputStream;
	    sb = new StringBuffer();
	    ioExc = null;
	}

	public void run() {
	    try {
		byte[] by = new byte[1];
		while (this != null) {
		    int ch = is.read(by);
		    if (ch != -1) { // -1 indicates the end of the stream
			System.out.print((char) by[0]);
			sb.append((char) by[0]);
		    } else {
			break;
		    }
		}
		is.close();
	    } catch (IOException e) {
		ioExc = e;
	    }
	}

	public void throwStoredException() throws IOException {
	    if (ioExc != null) {
		throw ioExc;
	    }
	}

	public String getInputStream() {
	    return new String(sb.toString());
	}
    }

}
