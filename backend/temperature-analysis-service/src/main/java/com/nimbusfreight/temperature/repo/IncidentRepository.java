package com.nimbusfreight.temperature.repo;
import com.nimbusfreight.temperature.model.Incident;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface IncidentRepository extends JpaRepository<Incident, Long> {
    @EntityGraph(attributePaths = "truck")
    List<Incident> findByStatusOrderByStartTimeDesc(String status);

    @EntityGraph(attributePaths = "truck")
    Optional<Incident> findByIncidentId(Long incidentId);

    Optional<Incident> findFirstByTruckTruckIdAndStatusOrderByStartTimeDesc(String truckId, String status);
}
