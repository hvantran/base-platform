package com.hoatv.action.manager.repositories;

import com.hoatv.action.manager.collections.ActionDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActionDocumentRepository extends MongoRepository<ActionDocument, String> {

    @Query("{actionName: {$regex : ?0, $options: 'i'}}")
    Page<ActionDocument> findActionByName(String actionName, Pageable pageable);
}
