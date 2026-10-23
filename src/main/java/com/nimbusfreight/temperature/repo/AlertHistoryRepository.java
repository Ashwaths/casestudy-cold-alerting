package com.nimbusfreight.temperature.repo;
import com.nimbusfreight.temperature.model.AlertHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface AlertHistoryRepository extends JpaRepository<AlertHistory, Long> {
    List<AlertHistory> findAllByOrderByAlertTimeDesc();
}
