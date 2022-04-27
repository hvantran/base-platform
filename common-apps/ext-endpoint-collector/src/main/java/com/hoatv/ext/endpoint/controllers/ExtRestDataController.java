package com.hoatv.ext.endpoint.controllers;

import com.hoatv.ext.endpoint.dtos.EndpointResponseVO;
import com.hoatv.ext.endpoint.dtos.EndpointSettingVO;
import com.hoatv.ext.endpoint.services.ExtRestDataService;
import lombok.NonNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(value = "/rest-data", produces = MediaType.APPLICATION_JSON_VALUE)
public class ExtRestDataController {

    private final ExtRestDataService extRestDataService;

    public ExtRestDataController(ExtRestDataService extRestDataService) {
        this.extRestDataService = extRestDataService;
    }

    @PostMapping(value = "/endpoints", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void addExtEndpoint(@Valid @RequestBody EndpointSettingVO endpointSettingVO) {
        extRestDataService.addExtEndpoint(endpointSettingVO);
    }

    @GetMapping(value = "/endpoints", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<EndpointSettingVO> getAllExtEndpoints(@NonNull @RequestParam String application, @RequestParam int pageIndex, @RequestParam int pageSize) {
        return extRestDataService.getAllExtEndpoints(application, PageRequest.of(pageIndex, pageSize));
    }

    @GetMapping(value = "/endpoints/{application}/responses", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<EndpointResponseVO> getEndpointResponses(@PathVariable("application") String application, @RequestParam int pageIndex, @RequestParam int pageSize) {
        return extRestDataService.getEndpointResponses(application, PageRequest.of(pageIndex, pageSize));
    }
}
