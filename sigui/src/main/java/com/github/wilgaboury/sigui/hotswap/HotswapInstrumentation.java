package com.github.wilgaboury.sigui.hotswap;

import com.github.wilgaboury.jsignal.Computed;
import com.github.wilgaboury.sigui.Node;
import com.github.wilgaboury.sigui.RenderInstrumentation;
import com.github.wilgaboury.sigui.Renderable;
import com.github.wilgaboury.sigui.Widget;

import java.util.List;
import java.util.function.Supplier;

import static com.github.wilgaboury.jsignal.JSignalUtil.untrack;

public class HotswapInstrumentation implements RenderInstrumentation {
  @Override
  public Widget instrument(Renderable component, Widget render) {
    var haComponent = new HotswapComponent(component);
    return HotswapComponent.context.withValue(haComponent).provide(() -> {
      return (Widget) Computed.create(() -> {
        haComponent.getRerender().track();
        return render.get();
      });
    });
  }
}
