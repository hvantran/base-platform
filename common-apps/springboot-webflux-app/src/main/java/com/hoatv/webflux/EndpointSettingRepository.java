package com.hoatv.webflux;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EndpointSettingRepository extends ReactiveCrudRepository<EndpointSetting, Long> {
}
