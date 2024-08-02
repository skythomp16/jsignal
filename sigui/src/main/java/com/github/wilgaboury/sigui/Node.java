package com.github.wilgaboury.sigui;

import com.github.wilgaboury.sigui.layout.Layout;
import com.github.wilgaboury.sigui.layout.Layouter;
import io.github.humbleui.types.Point;
import io.github.humbleui.types.Rect;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The primary layout and rendering primitive of Sigui
 */
public interface Node {
  default MetaNode toMeta() {
    return MetaNodeInitInstrumentation.context.use().instrument(() -> new MetaNode(this));
  }

  default List<Node> getChildren() {
    return List.of();
  }

  default @Nullable Layouter getLayouter() {
    return null;
  }

  default @Nullable Transformer getTransformer() {
    return null;
  }

  default @Nullable Painter getPainter() {
    return null;
  }

  default @Nullable Painter getAfterPainter() {
    return null;
  }

  // coordinates are in "paint space" for ease of calculation
  default HitTestResult hitTest(Point p, Layout layout) {
    return MathUtil.contains(Rect.makeWH(layout.getSize()), p)
      ? HitTestResult.HIT : HitTestResult.MISS;
  }

  enum HitTestResult {
    HIT,
    MISS,
    PASSTHROUGH
  }
}