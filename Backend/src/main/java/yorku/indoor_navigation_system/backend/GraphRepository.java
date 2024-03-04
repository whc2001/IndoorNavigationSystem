package yorku.indoor_navigation_system.backend;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GraphRepository extends JpaRepository<Graph, Integer>{
	

	List<Graph> findByNameAndFloor(String name, Integer floor);


    List<Graph> findByGraphPath(String graphPath);
}
