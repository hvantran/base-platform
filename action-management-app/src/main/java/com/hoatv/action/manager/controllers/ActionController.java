package com.hoatv.action.manager.controllers;


import com.hoatv.action.manager.dtos.ActionDefinitionDTO;
import com.hoatv.action.manager.api.ActionManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
public class ActionController {

    private ActionManagerService actionManagerService;

    @Autowired
    public ActionController(ActionManagerService actionManagerService) {
        this.actionManagerService = actionManagerService;
    }

    @PostMapping(value = "/actions", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> doAction(@RequestBody ActionDefinitionDTO actionDefinition) {
        String actionId = actionManagerService.executeAction(actionDefinition);
        return ResponseEntity.ok(Map.of("actionId", actionId));
    }
}
