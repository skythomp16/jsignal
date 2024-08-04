package com.github.wilgaboury.sigui;

import java.util.List;
import java.util.function.Supplier;

import static com.github.wilgaboury.jsignal.JSignalUtil.untrack;

public interface Renderable extends Supplier<List<Node>> {
  @Override
  default List<Node> get() {
    return RenderInstrumentation.context.use().instrument(this,
      () -> untrack(() -> render().get()));
  }

  /**
   * This method should be overridden but never called externally, use GetNodes
   */
  default Supplier<List<Node>> render() {
    return Widget.empty();
  }
}
