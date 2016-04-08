package baseline.geometry;

import java.util.List;

public class Window extends Polygon{
	private String name;
	private String id;
	
	public Window(List<Coordinate3D> coords, String name, String id) {
		super(coords);
		this.name = name;
		this.id = id;
	}

	public String getName() {
		return name;
	}
	
	public String getId(){
	    return id;
	}

	public void setName(String name) {
		this.name = name;
	}
}
