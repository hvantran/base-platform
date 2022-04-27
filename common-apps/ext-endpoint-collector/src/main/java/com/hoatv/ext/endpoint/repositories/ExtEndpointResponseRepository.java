package com.hoatv.ext.endpoint.repositories;

import com.hoatv.ext.endpoint.models.EndpointResponse;
import com.hoatv.ext.endpoint.models.EndpointSetting;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public interface ExtEndpointResponseRepository extends JpaRepository<EndpointResponse, Long> {

    List<EndpointResponse> findEndpointResponsesByEndpointSettingIn(List<EndpointSetting> endpointConfigSettings, Pageable pageable);
    List<EndpointResponse> findEndpointResponsesByColumn3IsNotNullAndColumn10IsNull();

    boolean existsEndpointResponseByColumn1(String columnValue);

    boolean existsEndpointResponseByColumn2(String columnValue);

    boolean existsEndpointResponseByColumn3(String columnValue);

    boolean existsEndpointResponseByColumn4(String columnValue);

    boolean existsEndpointResponseByColumn5(String columnValue);

    boolean existsEndpointResponseByColumn6(String columnValue);

    boolean existsEndpointResponseByColumn7(String columnValue);

    boolean existsEndpointResponseByColumn8(String columnValue);

    boolean existsEndpointResponseByColumn9(String columnValue);

    boolean existsEndpointResponseByColumn10(String columnValue);

}
