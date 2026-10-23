package com.nimbusfreight.temperature.repo;
import com.nimbusfreight.temperature.model.Truck;
import org.springframework.data.jpa.repository.JpaRepository;
public interface TruckRepository extends JpaRepository<Truck, String> {}
