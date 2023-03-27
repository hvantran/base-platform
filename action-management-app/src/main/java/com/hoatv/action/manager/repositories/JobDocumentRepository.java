package com.hoatv.action.manager.repositories;

import com.hoatv.action.manager.collections.JobDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobDocumentRepository extends MongoRepository<JobDocument, String> {

    List<JobDocument> findJobByActionId(String actionId);

    Page<JobDocument> findJobByActionId(String actionId, Pageable pageable);

    void deleteByActionId(String actionId);
}
