package com.hoatv.action.manager.repositories;

import com.hoatv.action.manager.collections.ActionDocument;
import com.hoatv.action.manager.collections.ActionStatisticsDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ActionStatisticsDocumentRepository extends MongoRepository<ActionStatisticsDocument, String> {

}
