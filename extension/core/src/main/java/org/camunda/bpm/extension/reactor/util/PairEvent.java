package org.camunda.bpm.extension.reactor.util;


import reactor.bus.Event;

/**
 * Reactor event that holds a {@link Pair} as data payload.
 * @param <L>
 * @param <R>
 */
public class PairEvent<L,R> extends Event<Pair<L,R>> {

  public PairEvent(final L left, final R right) {
    this(Pair.of(left,right));
  }

  public PairEvent(final Pair<L, R> data) {
    super(data);
  }

  public L getLeft() {
    return getData().getLeft();
  }

  public R getRight() {
    return getData().getRight();
  }
}
