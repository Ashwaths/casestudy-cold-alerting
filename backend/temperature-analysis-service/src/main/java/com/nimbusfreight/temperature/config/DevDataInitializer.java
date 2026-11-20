package com.nimbusfreight.temperature.config;

import com.nimbusfreight.temperature.model.Truck;
import com.nimbusfreight.temperature.repo.TruckRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DevDataInitializer {
    @Bean
    CommandLineRunner seedDemoTruck(TruckRepository trucks) {
        return args -> {
            if (!trucks.existsById("TRUCK-101")) {
                trucks.save(new Truck("TRUCK-101", "KA-01-NB-0101", "Nimbus Demo Customer"));
            }
        };
    }
}
