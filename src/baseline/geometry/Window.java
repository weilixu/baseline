package baseline.geometry;

import java.util.List;

public class Window extends Polygon{
	private String name;
	
	public Window(List<Coordinate3D> coords, String name) {
		super(coords);
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
