package edu.shmonin.birtdemo.config;

import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import static org.eclipse.birt.core.framework.Platform.createFactoryObject;
import static org.eclipse.birt.core.framework.Platform.startup;
import static org.eclipse.birt.report.engine.api.IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY;

@Component
public class BirtConfig {

    @Bean
    public IReportEngine reportEngine() throws BirtException {
        var config = new EngineConfig();
        startup(config);
        var factory = (IReportEngineFactory) createFactoryObject(EXTENSION_REPORT_ENGINE_FACTORY);
        return factory.createReportEngine(config);
    }
}