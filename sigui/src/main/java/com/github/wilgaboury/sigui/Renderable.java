package com.github.wilgaboury.sigui;

import static com.github.wilgaboury.jsignal.JSignalUtil.untrack;

public interface Renderable extends NodesSupplier {
  default Nodes getNodes() {
    return untrack(() -> RenderInstrumentation.context.use().instrument(this, this::render));
  }

  /**
   * This method should be overridden but never called externally, use GetNodes
   */
  default Nodes render() {
    return Nodes.empty();
  }
}
