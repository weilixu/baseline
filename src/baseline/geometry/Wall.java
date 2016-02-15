package baseline.geometry;

import java.util.LinkedList;
import java.util.List;

public class Wall extends Polygon{
	private List<Window> windows;
	private List<Door> doors;
	
	public Wall(List<Coordinate3D> points){
		super(points);
		windows = new LinkedList<Window>();
		doors = new LinkedList<Door>();
	}
	
	/**
	 * Return false if exists fenestration's vertex not on or within wall
	 * polygon<br/>
	 * fenestration type refer to public static final fields
	 * @param fenestration
	 * @return
	 */
	private boolean isInWall(List<Coordinate3D> points){
		boolean flag = true;
		
		for(Coordinate3D point : points){
			if(!super.containsPoint(point)){
				flag = false;
			}
		}
		
		if(!flag){
			System.err.println("Fenestration is not on or within wall polygon");
		}
		return flag;
	}
	
	/**
	 * Return false if exists fenestration's vertex not on or within wall
	 * polygon
	 * @param fenestration
	 * @return
	 */
	public boolean addWindow(List<Coordinate3D> points, String name, String id){
		Window win = new Window(points, name, id);
		this.windows.add(win);
		
		return this.isInWall(points);
	}
	
	/**
	 * Return false if exists fenestration's vertex not on or within wall
	 * polygon
	 * @param fenestration
	 * @return
	 */
	public boolean addDoor(List<Coordinate3D> points){
		Door door = new Door(points);
		this.doors.add(door);
		
		return this.isInWall(points);
	}
	
	public double getWallArea(){
		return super.getArea();
	}
	
	public double getWindowArea(){
		double area = 0;
		for(Window win : windows){
			area += win.getArea();
		}
		return area;
	}
	
	/**
	 * ratio>0 => enlarge windows
	 * ratio<0 => shrink windows
	 * returns Polygon's scale related flag
	 */
	public int scaleWindows(double ratio){
		int flag = 0;
		for(Window win : windows){
			int tmp = win.scale(ratio);
			if(flag==0){
				flag = tmp; //capture the first error flag if any
			}
		}
		return flag;
	}
	
	public boolean hasWindow(){
		return !windows.isEmpty();
	}
	
	public List<Window> getWindows(){
		return this.windows;
	}
}
