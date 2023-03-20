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

   @Autowired(required = false)
    private ActionDocumentRepository actionDocumentRepository;

   @Autowired
   private ScriptEngineService scriptEngineService;

    //@Bean
    public CommandLineRunner createActions() {
        return args -> {
            ActionDocument cleanupHome = ActionDocument.builder()
                .createdAt(System.currentTimeMillis()/1000)
                .actionName("Cleanup home")
                .configurations("{'tools': 'pen'}")
                .build();
            ActionDocument washYourDishes = ActionDocument.builder()
                .createdAt(System.currentTimeMillis()/1000)
                .actionName("Wash your dishes")
                .configurations("{'tools': 'pen'}")
                .build();
            ActionDocument cleanupYourBed = ActionDocument.builder()
                .createdAt(System.currentTimeMillis()/1000)
                .actionName("Cleanup your bed")
                .configurations("{'tools': 'pen'}")
                .build();
            actionDocumentRepository.saveAll(List.of(cleanupHome, washYourDishes, cleanupYourBed));
        };
    }
//    //@Bean
//    public CommandLineRunner queries() {
//        return args -> {
//            ActionDocument cleanupYourBed = actionDocumentRepository.findActionByName("Cleanup your bed");
//            System.out.println(cleanupYourBed);
//        };
//    }
//    @Bean
//    public CommandLineRunner executeJobByEngine() {
//        return args -> {
//            File file = new File(ActionManagerApplication.class.getClassLoader().getResource("violation-executor.js").getFile());
//            String data = FileUtils.readFileToString(file, "UTF-8");
//            JobResult result = scriptEngineService.execute(data, Collections.emptyMap());
//            System.out.println(result);
//        };
//    }

    public static void main (String[] args) {
        SpringApplication.run(ActionManagerApplication.class, args);
    }
}
