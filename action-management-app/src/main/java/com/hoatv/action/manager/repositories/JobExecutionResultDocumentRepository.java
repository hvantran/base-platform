package com.hoatv.action.manager.repositories;

import com.hoatv.action.manager.collections.JobDocument;
import com.hoatv.action.manager.collections.JobExecutionResultDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobExecutionResultDocumentRepository extends MongoRepository<JobExecutionResultDocument, String> {

}
