package baseline.htmlparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import baseline.geometry.Coordinate3D;
import baseline.geometry.Wall;
import baseline.geometry.Window;
import baseline.idfdata.IdfReader;
import baseline.idfdata.IdfReader.ValueNode;

/**
 * Implement later
 * 
 * @author wanghp18
 *
 */
public class WindowWallRatioParser {
    public static final int INVALID_POLYGON = -1;
    public static final int ZERO_AREA_WALL = -2;

    public static double THRESHOLD = 0.4;
    private static final int SurfaceNumVerticeLoc = 9;
    private static final int SurfaceCoordsOffset = 10;

    private LinkedList<Wall> walls;
    private HashMap<String, ArrayList<ValueNode>> feneSurfaces;

    public WindowWallRatioParser(IdfReader reader) {
	this.walls = new LinkedList<Wall>();
	int buildSurfaceNameLoc = 3;

	HashMap<String, Wall> idfWalls = new HashMap<String, Wall>();

	HashMap<String, ArrayList<ValueNode>> buildSurfaces = reader
		.getObjectListCopy("BuildingSurface:Detailed");

	Set<String> names = buildSurfaces.keySet();
	for (String name : names) {
	    ArrayList<ValueNode> info = buildSurfaces.get(name);
	    ValueNode type = info.get(1);
	    if (type.getAttribute().equals("Wall")) {
		List<Coordinate3D> coords = this.readSurfaceCoords(info);
		idfWalls.put(info.get(0).getAttribute(), new Wall(coords));
	    }
	}

	this.feneSurfaces = reader
		.getObjectListCopy("FenestrationSurface:Detailed");
	names = feneSurfaces.keySet();
	for (String name : names) {
	    ArrayList<ValueNode> info = feneSurfaces.get(name);
	    ValueNode type = info.get(1);
	    if (type.getAttribute().equals("Window")) {
		List<Coordinate3D> coords = this.readSurfaceCoords(info);
		String buildSurfaceName = info.get(buildSurfaceNameLoc)
			.getAttribute();
		if (idfWalls.containsKey(buildSurfaceName)) {
		    idfWalls.get(buildSurfaceName).addWindow(coords,
			    info.get(0).getAttribute(), name);
		} else {
		    System.err.println("Window has no wall: " + name
			    + ", missing wall name:" + buildSurfaceName);
		}
	    }
	}

	// remove walls has no window
	names = idfWalls.keySet();
	for (String name : names) {
	    Wall wall = idfWalls.get(name);
	    if (wall.hasWindow()) {
		this.walls.add(wall);
	    }
	}
    }

    /**
     * Same for BuildingSurface:Detailed and FenestrationSurface:Detailed
     * 
     * @param attrs
     * @return
     */
    private List<Coordinate3D> readSurfaceCoords(ArrayList<ValueNode> attrs) {
	List<Coordinate3D> coords = new LinkedList<Coordinate3D>();
	
	//assume the vertices starts at line 10th
	int numVertices = SurfaceNumVerticeLoc+1;
	while(numVertices < attrs.size()){
	    double x = Double.parseDouble(
		    attrs.get(numVertices).getAttribute());
	    double y = Double.parseDouble(
		    attrs.get(numVertices + 1).getAttribute());
	    double z = Double.parseDouble(
		    attrs.get(numVertices + 2).getAttribute());
	    Coordinate3D coord = new Coordinate3D(x, y, z);
	    coords.add(coord);
	    numVertices +=3;
	}
	return coords;
    }

    private double getWindowWallRatio() {
	double wallArea = 0, winArea = 0;
	for (Wall wall : walls) {
	    wallArea += wall.getWallArea();
	    winArea += wall.getWindowArea();
	}

	return winArea / wallArea;
    }
    
    public void updateThreshold(double threshold){
	if(threshold>=10 && threshold<=90){
	    THRESHOLD = threshold;
	}
    }

    /**
     * Return whether the window is adjusted
     * 
     * @return
     */
    public boolean adjustToThreshold() {
	double ratio = this.getWindowWallRatio();
	if (ratio > WindowWallRatioParser.THRESHOLD) {
	    double scaleRatio = WindowWallRatioParser.THRESHOLD / ratio;
	    for (Wall wall : walls) {
		wall.scaleWindows(scaleRatio);
		this.saveWindowCoordsToReader(wall);
	    }

	    return true;
	}
	return false;
    }

    private void saveWindowCoordsToReader(Wall wall) {
	List<Window> wins = wall.getWindows();
	for (Window win : wins) {
	    String name = win.getId();
	    //System.out.println(name);
	    ArrayList<ValueNode> winInfo = this.feneSurfaces.get(name);
	    //System.out.println(winInfo.get(0).getAttribute());
	    List<Coordinate3D> points = win.getCoords();
	    for (int i = 0; i < points.size(); i++) {
		Coordinate3D point = points.get(i);
		winInfo.get(i * 3 + SurfaceCoordsOffset)
			.setAttribute(String.valueOf(point.getX()));
		winInfo.get(i * 3 + SurfaceCoordsOffset + 1)
			.setAttribute(String.valueOf(point.getY()));
		winInfo.get(i * 3 + SurfaceCoordsOffset + 2)
			.setAttribute(String.valueOf(point.getZ()));
	    }
	}
    }
}
