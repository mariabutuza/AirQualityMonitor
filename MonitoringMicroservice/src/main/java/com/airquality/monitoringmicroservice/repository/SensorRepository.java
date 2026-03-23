package com.airquality.monitoringmicroservice.repository;

import com.airquality.monitoringmicroservice.entity.Sensor;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface SensorRepository extends JpaRepository<Sensor, Long> {

    Optional<Sensor> findByDeviceId(String deviceId);

    List<Sensor> findByActiveTrueOrderByLocationAscNameAsc();

    @Query("select distinct s.location from Sensor s where s.active = true and s.location is not null order by s.location")
    List<String> findDistinctActiveLocations();

    @Query("select s from Sensor s where s.active = true and s.location = :location order by s.name")
    List<Sensor> findActiveByLocation(@Param("location") String location);

}