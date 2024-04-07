package yorku.indoor_navigation_system.backend;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NodeRepository extends JpaRepository<Node, Integer>{
	

	List<Node> findById(int id);



	List<Node> findAll();
	

	
	
}
