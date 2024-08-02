package com.github.wilgaboury.sigui.hotswap;

import com.github.wilgaboury.sigui.Nodes;
import com.github.wilgaboury.sigui.RenderInstrumentation;
import com.github.wilgaboury.sigui.Renderable;

import java.util.function.Supplier;

import static com.github.wilgaboury.jsignal.JSignalUtil.untrack;

public class HotswapInstrumentation implements RenderInstrumentation {
  @Override
  public Nodes instrument(Renderable component, Supplier<Nodes> render) {


    var haComponent = new HotswapComponent(component);
    return HotswapComponent.context.withValue(haComponent).provide(() -> {
      //var computed =
      return Nodes.compute(() -> {
        haComponent.getRerender().track();
        return untrack(() -> render.get().getNodes());
      });
//
//      ((Computed<?>)computed.children()).
//
//      return computed.getNodes();
    });
  }
}
