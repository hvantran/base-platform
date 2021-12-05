package com.hoatv.ext.endpoint.repositories;

import com.hoatv.ext.endpoint.models.EndpointExecutionResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
@Transactional
public interface ExtExecutionResultRepository extends JpaRepository<EndpointExecutionResult, Long> {
}
