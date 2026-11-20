package com.nimbusfreight.temperature.repo;
import com.nimbusfreight.temperature.model.SensorReading;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface SensorReadingRepository extends JpaRepository<SensorReading, Long> {
    boolean existsByEventKey(String eventKey);
    List<SensorReading> findByTruckTruckIdOrderByTimestampAsc(String truckId);
    Optional<SensorReading> findFirstByTruckTruckIdOrderByTimestampDesc(String truckId);
    void deleteAllByTruckTruckId(String truckId);
}
