package yorku.indoor_navigation_system.backend.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import yorku.indoor_navigation_system.backend.models.Coordinate;

import java.util.List;

public interface CoordinateRepository extends JpaRepository<Coordinate, Integer>{
	

	List<Coordinate> findById(int id);


	List<Coordinate> findAll();
	

	
	
}
