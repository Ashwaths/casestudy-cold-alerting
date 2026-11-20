package com.nimbusfreight.temperature.repo;
import com.nimbusfreight.temperature.model.Truck;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface TruckRepository extends JpaRepository<Truck, String> {
    List<Truck> findAllByOrderByTruckIdAsc();
    boolean existsByRegistrationNumber(String registrationNumber);
    boolean existsByRegistrationNumberAndTruckIdNot(String registrationNumber, String truckId);
}
