package com.hoatv.action.manager.api;

import com.hoatv.action.manager.dtos.ActionDefinitionDTO;

public interface ActionManagerService {

    String executeAction(ActionDefinitionDTO actionDefinition);
}
