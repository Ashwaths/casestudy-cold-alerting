package com.nimbusfreight.temperature.repo;
import com.nimbusfreight.temperature.model.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ConfigurationRepository extends JpaRepository<Configuration, String> {}
