package org.camunda.bpm.extension.reactor;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineAssertions.assertThat;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;
import static org.camunda.bpm.extension.reactor.CamundaSelector.Queue.tasks;
import static org.camunda.bpm.extension.reactor.plugin.ReactorProcessEnginePlugin.CAMUNDA_EVENTBUS;

import java.util.ArrayList;
import java.util.Arrays;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.task.TaskDefinition;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.extension.reactor.listener.PublisherTaskListener;
import org.camunda.bpm.extension.reactor.listener.SubscriberTaskListener;
import org.camunda.bpm.extension.reactor.plugin.ReactorProcessEnginePlugin;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Rule;
import org.junit.Test;

/**
 * Spike that assures that the general approach is working.
 */
public class CandidateGroupsViaPluginSpike {


  /**
   * Plugin with PostParseListener that registers "publishTaskCreate".
   */
  private final ProcessEnginePlugin plugin = new AbstractProcessEnginePlugin(){
    @Override
    public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
      processEngineConfiguration.getCustomPostBPMNParseListeners().add(new AbstractBpmnParseListener(){
        @Override
        public void parseUserTask(Element userTaskElement, ScopeImpl scope, ActivityImpl activity) {
          TaskDefinition taskDefinition = ((UserTaskActivityBehavior) activity.getActivityBehavior()).getTaskDefinition();
          taskDefinition.addTaskListener(TaskListener.EVENTNAME_CREATE, new PublisherTaskListener(CAMUNDA_EVENTBUS));
        }
      });
    }
  };

  @CamundaSelector(queue = tasks, type="userTask", event="create")
  public static class OnCreateListener extends SubscriberTaskListener {

    public OnCreateListener() {
      register(CAMUNDA_EVENTBUS);
    }

    @Override
    public void notify(DelegateTask delegateTask) {
      delegateTask.addCandidateGroup("group");
      delegateTask.addCandidateGroups(Arrays.asList("foo","bar"));
    }

  }

  /**
   * Configuration with plugin.
   */
  private final ProcessEngineConfigurationImpl processEngineConfiguration = new StandaloneInMemProcessEngineConfiguration() {{
    //setDatabaseSchemaUpdate(ProcessEngineConfigurationImpl.DB_SCHEMA_UPDATE_DROP_CREATE);
    setCustomPostBPMNParseListeners(new ArrayList<BpmnParseListener>());
    getProcessEnginePlugins().add(new ReactorProcessEnginePlugin());
  }};

  @Rule
  public final ProcessEngineRule processEngineRule = new ProcessEngineRule(processEngineConfiguration.buildProcessEngine());

  /**
   * Small process with on user task.
   */
  private final BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("process")
      .startEvent()
      .userTask("task1").name("Do something")
      .endEvent()
      .done();

  @Test
  @Deployment(resources="ProcessA.bpmn")
  public void addCandidateGroup() {
    // register onCreate
    new OnCreateListener();

    // create process Engine
  //  processEngineConfiguration.buildProcessEngine();

    //repositoryService().createDeployment().addModelInstance("process.bpmn", modelInstance).deploy();

   //repositoryService().createDeployment().addClasspathResource("ProcessA.bpmn").deploy();


    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("process_a");

    assertThat(processInstance).isWaitingAt("task_a");
    assertThat(task()).hasCandidateGroup("group");
    assertThat(task()).hasCandidateGroup("foo");
    assertThat(task()).hasCandidateGroup("bar");


    complete(task());

    assertThat(processInstance).isWaitingAt("task_b");
    assertThat(task()).hasCandidateGroup("group");
    assertThat(task()).hasCandidateGroup("foo");
    assertThat(task()).hasCandidateGroup("bar");
    complete(task());

    assertThat(processInstance).isEnded();
  }
}
