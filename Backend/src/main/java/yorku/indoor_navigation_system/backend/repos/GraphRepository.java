package yorku.indoor_navigation_system.backend.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import yorku.indoor_navigation_system.backend.models.Graph;

import java.util.List;

public interface GraphRepository extends JpaRepository<Graph, Integer> {


    List<Graph> findByName(String name);

    @Query("SELECT DISTINCT e.name FROM Graph e")
    List<String> findAllDistinctNames();

    List<Graph> findByNameAndFloor(String name, Integer floor);

    @Query("SELECT DISTINCT e.floor FROM Graph e WHERE e.name = ?1")
    List<Integer> findAllFloorsByBuilding(String name);
}
