package com.hoatv.action.manager;

import com.hoatv.action.manager.collections.ActionDocument;
import com.hoatv.action.manager.repositories.ActionDocumentRepository;
import com.hoatv.action.manager.services.JobResult;
import com.hoatv.action.manager.services.ScriptEngineService;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.io.File;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableAutoConfiguration
@ComponentScan({"com.hoatv.action.manager", "com.hoatv.springboot.common"})
@EnableMongoRepositories
public class ActionManagerApplication {
    public static void main (String[] args) {
        SpringApplication.run(ActionManagerApplication.class, args);
    }
}
