package yorku.indoor_navigation_system.backend;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Coordinate{
	@Id
	@GeneratedValue
	Integer id;
	public int x;
	public int y;
	
	public Coordinate(int x, int y) {
		super();
		this.x = x;
		this.y = y;
	}

	public Coordinate() {

	}

	@Override
    public String toString() {
        return "Coordinate{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
	
	public boolean equal(Coordinate c) {
		if(Math.abs(c.x -x)<=5 && Math.abs(c.y - y)<5) {
			return true;
		}else {
			return false;
		}
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}
}
