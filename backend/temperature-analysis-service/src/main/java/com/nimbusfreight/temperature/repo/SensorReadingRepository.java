package com.nimbusfreight.temperature.repo;
import com.nimbusfreight.temperature.model.SensorReading;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface SensorReadingRepository extends JpaRepository<SensorReading, Long> {
    boolean existsByEventKey(String eventKey);
    List<SensorReading> findByTruckTruckIdOrderByTimestampAsc(String truckId);
}
