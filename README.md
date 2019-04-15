<p align="right">
  <img src="http://camunda.github.io/camunda-bpm-assert/resources/images/camunda.png" width="50" />
  <img src="https://avatars1.githubusercontent.com/u/4201559?s=400&v=4" width="50" />
</p>



# camunda-bpm-reactor

[![Build Status](https://travis-ci.org/camunda/camunda-bpm-reactor.svg?branch=master)](https://travis-ci.org/camunda/camunda-bpm-reactor)  [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.camunda.bpm.extension.reactor/camunda-bpm-reactor-core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.camunda.bpm.extension.reactor/camunda-bpm-reactor-core)

for the previous version, please go to https://github.com/camunda/camunda-bpm-reactor/tree/1.2


Event-based listeners and delegates for camunda.

## What is this about? 

This extension provides a process engine plugin that registers Execution- and TaskListeners to all possible elements and all possible events. These listeners then publish their delegates (DelegateTask, DelegateExcution) to an event bus.

Custom implementations that are interested in certain events may register on that bus and will get notified and executed when the registered listeners fire.

Publishing and subscribing uses a topic pattern `/camunda/{context}/{type}/{process}/{element}/{event}` so it is possible to register on all that happens on the engine or the assignement event of a concrete userTask in a concrete process.

Reference: a similar approach was already done in the engine-cdi module using CDI observers and qualifiers. 

## Why do I need this?

Using an event bus decouples registration and implementation of listeners. The bpmn file has not to be touched for this. This is useful for implementations that can be considered "aspects" of the engine like task assignment and monitoring.

Instead of registering listeners all over your bpmn files that always call the same rule service to determine the candidate groups of a task or write runtime information to a data source, you just hook into the event bus stream and wait for notification.

While this could be achieved with custom plugins/parselisteners, these share the problem that the engine has to have access to the code of the listeners added. With the decoupling via eventbus, this is avoided.

## How is it done?

This extension uses the event bus provided by projectreactor.io. This bus is fairly advanced and stable and allows separation of event-payload (the DelegateExpression) and event topic, so we do not need any additional qualifiers or concrete types to distinct between "listen to create of task B" or "listen to all events of task A".

Using the extension is straight forward, you need to:

* access the eventBus instance used in the plugin by calling CamundaReactor.eventBus().
* Use this bus to register an instance of an appropriate listener and specify the topic parts you are interested in.

### Examples

Register a listener that is fired for all "create" events on any user task in the system. 

```java
@CamundaSelector(type = "userTask", event = TaskListener.EVENTNAME_CREATE)
public class TaskCreateListener implements TaskListener {
  
  public TaskCreateListener(EventBus eventBus) {
    eventBus.register(this);
  }

  @Override
  public void notify(DelegateTask delegateTask) {
   ...
  }
}
```

More examples can be found in the sub-module [examples](examples).

### Configuration

If you have multiple task listener for an event, per default the task listener from this extension gets called in the end.
When setting the following property to true, the reactor task listener get's always called first!

```java
camunda.bpm.reactor.reactor-listener-first-on-user-task: true
```

## Noteworthy

This extensions works with delegateTasks and delegateEvents directly. These cannot be used outside the current thread, so the eventBus used is synchronous. 

## Next Steps

* provide extension for CDI (?)
* use eventbus for message correlation end/start
* ...

## Maintainer

* [Jan Galinski](https://github.com/jangalinski), [Holisticon AG](http://www.holisticon.de/)
* [Philipp Ossler](https://github.com/saig0), [Camunda Services GmbH](http://www.camunda.org/)

## Contributors

* [Malte Sörensen](https://github.com/malteser), [Holisticon AG](http://www.holisticon.de/)
* [Patrick Schalk](https://github.com/pschalk), [Holisticon AG](http://www.holisticon.de/)

## Sponsors

![Kühne+Nagel](https://raw.githubusercontent.com/camunda/camunda-bpm-custom-batch/master/docs/sponsor_kn.jpeg)


## License

* [Apache License, Version 2.0](./LICENSE)
