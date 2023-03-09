package com.hoatv.action.manager.services;

import com.hoatv.fwk.common.services.CheckedSupplier;
import com.hoatv.fwk.common.services.HttpClientService;
import com.hoatv.task.mgmt.services.TaskFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.script.Invocable;
import javax.script.ScriptEngine;

@Service
public class ScriptEngineService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScriptEngineService.class);

    public <TResult> TResult executeJobLauncher(String scriptContent) {
        return executeWithInterface(JobLauncher.class, scriptContent);
    }

    public <TResult> TResult executeWithInterface(Class<? extends Launcher> klass, String scriptContent) {
        ScriptEngine scriptEngine = ScriptEngineFactory.GRAAL_JS.getScriptEngine();

        scriptEngine.put("httpClientService", HttpClientService.INSTANCE);
        scriptEngine.put("taskFactory", TaskFactory.INSTANCE);

        LOGGER.info("Eval the script content {}", scriptContent);
        CheckedSupplier<Object> evalObjectSupplier = () -> scriptEngine.eval(scriptContent);
        evalObjectSupplier.get();

        Invocable invocable = (Invocable) scriptEngine;
        Launcher launcher = invocable.getInterface(klass);
        LOGGER.info("Executing the job {}", launcher);
        return launcher.execute();
    }
}
