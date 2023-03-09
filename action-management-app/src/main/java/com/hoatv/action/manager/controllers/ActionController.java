package com.hoatv.action.manager.controllers;


import com.hoatv.action.manager.dtos.ActionDefinitionDTO;
import com.hoatv.action.manager.services.ActionManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
public class ActionController {

    private ActionManagerService actionManagerService;

    @Autowired
    public ActionController(ActionManagerService actionManagerService) {
        this.actionManagerService = actionManagerService;
    }

    @PostMapping(value = "/actions", consumes = MediaType.APPLICATION_JSON_VALUE)
    public String doAction(@RequestBody ActionDefinitionDTO actionDefinition) {
        return actionManagerService.executeAction(actionDefinition);
    }
}
