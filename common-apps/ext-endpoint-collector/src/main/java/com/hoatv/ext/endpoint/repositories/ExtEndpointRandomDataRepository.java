package com.hoatv.ext.endpoint.repositories;

import com.hoatv.ext.endpoint.models.EndpointRandomData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
@Transactional
public interface ExtEndpointRandomDataRepository extends JpaRepository<EndpointRandomData, Long> {

    boolean existsByRandom(String random);
}
