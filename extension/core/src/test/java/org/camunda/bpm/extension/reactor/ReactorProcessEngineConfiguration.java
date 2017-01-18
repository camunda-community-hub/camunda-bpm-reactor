package org.camunda.bpm.extension.reactor;

import java.util.Arrays;

import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.MockExpressionManager;
import org.camunda.bpm.extension.reactor.bus.CamundaEventBus;
import org.camunda.bpm.extension.reactor.plugin.ReactorProcessEnginePlugin;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class ReactorProcessEngineConfiguration extends  StandaloneInMemProcessEngineConfiguration {
  static {
    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();
  }

  public static ProcessEngineRule buildRule() {
    return buildRule(new CamundaEventBus());
  }

  public static ProcessEngineRule buildRule(final CamundaEventBus camundaEventBus) {
    final ReactorProcessEngineConfiguration configuration = new ReactorProcessEngineConfiguration(camundaEventBus);

    return new ProcessEngineRule(configuration.buildProcessEngine());
  }

  public ReactorProcessEngineConfiguration(final ProcessEnginePlugin... plugins) {
    this.history = HISTORY_FULL;
    this.databaseSchemaUpdate = DB_SCHEMA_UPDATE_DROP_CREATE;

    this.isMetricsEnabled = false;
    this.jobExecutorActivate = false;
    this.expressionManager = new MockExpressionManager();

    if (plugins != null) {
      Arrays.stream(plugins).forEach(this.processEnginePlugins::add);
    }
  }

  public ReactorProcessEngineConfiguration(final CamundaEventBus camundaEventBus) {
    this(new ReactorProcessEnginePlugin(camundaEventBus));
  }

  public ProcessEngineRule buildProcessEngineRule() {
    return new ProcessEngineRule(buildProcessEngine());
  }
}
