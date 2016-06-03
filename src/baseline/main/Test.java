package baseline.main;

import java.util.LinkedList;
import java.util.List;

import baseline.geometry.Coordinate3D;
import baseline.geometry.Wall;
import baseline.geometry.Window;
import baseline.htmlparser.WindowWallRatioParser;

public class Test {
	public static void main(String[] args){
		List<Coordinate3D> wall = new LinkedList<Coordinate3D>();
		wall.add(new Coordinate3D());
		wall.add(new Coordinate3D(0, 10, 0));
		wall.add(new Coordinate3D(10, 10, 0));
		wall.add(new Coordinate3D(10, 0, 0));
		
		List<Coordinate3D> win = new LinkedList<Coordinate3D>();
		win.add(new Coordinate3D(1, 1, 0));
		win.add(new Coordinate3D(1, 9, 0));
		win.add(new Coordinate3D(9, 9, 0));
		win.add(new Coordinate3D(9, 1, 0));
		
		Wall testWall = new Wall(wall);
		testWall.addWindow(win, "win_name","d");
		
		double wallArea = testWall.getWallArea();
		System.out.println("Before wall: "+wallArea);
		double winArea = testWall.getWindowArea();
		System.out.println("Before win: "+winArea);
		
		double ratio = winArea/wallArea;
		double threshold = WindowWallRatioParser.THRESHOLD;
		
		testWall.scaleWindows(threshold/ratio);
		
		List<Window> testWins = testWall.getWindows();
		
		for(Window testWin : testWins){
			List<Coordinate3D> coords = testWin.getCoords();
			for(Coordinate3D coord : coords){
				System.out.println(coord.toString());
			}
			System.out.println("After win: "+testWin.getArea());
		}
	}
}
