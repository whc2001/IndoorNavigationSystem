package yorku.indoor_navigation_system.backend.models;

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
	public Coordinate c;
	@ManyToMany
	@JoinTable(
			name = "node_relationship",
			joinColumns = @JoinColumn(name = "node_id"),
			inverseJoinColumns = @JoinColumn(name = "related_node_id")
	)
	public List<Node> Nodes;
	public String name;
	public String position;
	public double nodeId;
	public int floor;
	public String building;
	public int type = 0;
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
		String s = "[";
		for(Node n:Nodes) {
			s+=n.nodeId+" ";
		}
		s+="]";
		return "type:"+type+"  id:"+nodeId+"  position:"+position;
//		return "x:"+c.x+",y:"+c.y+"  name:"+name+"  type:"+type+"  id:"+nodeId+" floor:"+floor+"  position:"+position+"  nodes:"+s;
	}
}
