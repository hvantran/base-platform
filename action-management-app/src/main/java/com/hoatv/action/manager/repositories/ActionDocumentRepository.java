package com.hoatv.action.manager.repositories;

import com.hoatv.action.manager.collections.ActionDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ActionDocumentRepository extends MongoRepository<ActionDocument, String> {

    @Query("{actionName: '?0'}")
    ActionDocument findActionByName(String actionName);
}
