package yorku.indoor_navigation_system.backend;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;


@Entity
public class Node{
	@Id
	@GeneratedValue
	Integer id;
	@ManyToOne
	@JoinColumn(name = "coordinate_id")
	Coordinate c;
	@ManyToMany
	@JoinTable(
			name = "node_relationship",
			joinColumns = @JoinColumn(name = "node_id"),
			inverseJoinColumns = @JoinColumn(name = "related_node_id")
	)
	List<Node> Nodes;
	String name;
	double position;
	double nodeId;
	int type = 0;
//-1: cyan, 0:red, 1:blue, 2:green, 3:yellow, 4:pink
	
	public Node(Coordinate c, ArrayList<Node> nodes, String name, int type) {
		super();
		this.c = c;
		Nodes = nodes;
		this.name = name;
		this.type = type;
	}



	public Node() {
		Nodes = new ArrayList<>();
	}

	public Coordinate getC() {
		return c;
	}

	public void setC(Coordinate c) {
		this.c = c;
	}

	public ArrayList<Node> getNodes() {
		return new ArrayList<>(Nodes);
	}

	public void setNodes(ArrayList<Node> nodes) {
		Nodes = nodes;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
	public String toString() {
		return c.x+","+c.y+"  "+name+"  "+type+"  ";
	}
}
