package com.nimbusfreight.temperature.repo;
import com.nimbusfreight.temperature.model.AlertHistory;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface AlertHistoryRepository extends JpaRepository<AlertHistory, Long> {
    @EntityGraph(attributePaths = {"incident", "incident.truck"})
    List<AlertHistory> findAllByOrderByAlertTimeDesc();
    void deleteAllByIncidentIn(List<com.nimbusfreight.temperature.model.Incident> incidents);
}
