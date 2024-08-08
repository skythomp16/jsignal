package com.github.wilgaboury.sigui;

import static com.github.wilgaboury.jsignal.JSignalUtil.untrack;

public interface Renderable extends Nodes {
  default Nodes get() {
    return RenderInstrumentation.context.use().instrument(this, () -> untrack(this::render));
  }

  /**
   * This method should be overridden but never called externally, use GetNodes
   */
  default Nodes render() {
    return Nodes.empty();
  }
}
