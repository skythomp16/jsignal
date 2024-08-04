package com.github.wilgaboury.sigui;

import com.github.wilgaboury.jsignal.Constant;
import com.github.wilgaboury.jsignal.Context;

import java.util.List;
import java.util.function.Supplier;

public interface RenderInstrumentation {
    Context<RenderInstrumentation> context = new Context<>(RenderInstrumentation.empty());

    Supplier<List<Node>> instrument(Renderable component, Supplier<List<Node>> render);

    default RenderInstrumentation add(RenderInstrumentation that) {
        return (component, render) -> this.instrument(component,
          () -> that.instrument(component, render).get());
    }

    static RenderInstrumentation empty() {
        return (component, render) -> {
            var nodes  = render.get();
            return Constant.of(nodes);
        };
    }
}
