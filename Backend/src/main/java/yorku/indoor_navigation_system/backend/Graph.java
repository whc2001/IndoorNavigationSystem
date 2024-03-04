package yorku.indoor_navigation_system.backend;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Graph {
	@Id
	@GeneratedValue
	Integer id;
	@ManyToMany
	@JoinTable(
			name = "graph_node_relationship",
			joinColumns = @JoinColumn(name = "graph_id"),
			inverseJoinColumns = @JoinColumn(name = "node_id")
	)
	List<Node> graph_node;
	String name;

	Integer floor;
	String mapPath;
	String graphPath;

	public Graph(ArrayList<Node> graph_node, String name, Integer floor, String path, String graphPath) {
		super();
		this.graph_node = graph_node;
		this.name = name;
		this.floor = floor;
		this.mapPath = path;
		this.graphPath = graphPath;
	}

	public Graph() {
		this.graph_node = new ArrayList<>();
	}

	public String getMapPath() {
		return mapPath;
	}

	public void setMapPath(String mapPath) {
		this.mapPath = mapPath;
	}

	public String getGraphPath() {
		return graphPath;
	}

	public void setGraphPath(String graphPath) {
		this.graphPath = graphPath;
	}

	public ArrayList<Node> getGraph_node() {
		return new ArrayList<>(graph_node);
	}

	public void setGraph_node(ArrayList<Node> graph_node) {
		this.graph_node = graph_node;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getFloor() {
		return floor;
	}

	public void setFloor(Integer floor) {
		this.floor = floor;
	}
}
