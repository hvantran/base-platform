package com.hoatv.ext.endpoint.controllers;

import com.hoatv.ext.endpoint.dtos.EndpointResponseVO;
import com.hoatv.ext.endpoint.dtos.EndpointSettingVO;
import com.hoatv.ext.endpoint.services.ExtRestDataService;
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
    public List<EndpointSettingVO> getAllExtEndpoints(@RequestParam String application) {
        return extRestDataService.getAllExtEndpoints(application);
    }

    @GetMapping(value = "/endpoints/{application}/responses", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<EndpointResponseVO> getEndpointResponses(@PathVariable("application") String application) {
        return extRestDataService.getEndpointResponses(application);
    }
}