package com.hoatv.action.manager.services;

import com.hoatv.action.manager.dtos.ActionDefinitionDTO;

public interface ActionManagerService {

    String executeAction(ActionDefinitionDTO actionDefinition);
}
