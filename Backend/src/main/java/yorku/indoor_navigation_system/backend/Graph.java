package yorku.indoor_navigation_system.backend;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.persistence.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
