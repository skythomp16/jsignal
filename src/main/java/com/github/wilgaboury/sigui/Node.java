package com.github.wilgaboury.sigui;

import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Matrix33;
import org.lwjgl.util.yoga.Yoga;

import java.util.function.Consumer;

/**
 * The primary layout and rendering primitive of Sigui
 */
public interface Node {
    default Nodes children() {
        return Nodes.empty();
    }

    default void ref(MetaNode node) {}

    default void layout(long yoga) {}

    default Matrix33 offset(long yoga) {
        return defaultOffsetter(yoga);
    }

    default void paint(Canvas canvas, long yoga) {}

    default void paintAfter(Canvas canvas, long yoga) {}

    static Builder builder() {
        return new Builder();
    }

    static interface Offsetter {
        Matrix33 offset(long yoga);
    }

    static Matrix33 defaultOffsetter(long yoga) {
        return Matrix33.makeTranslate(Yoga.YGNodeLayoutGetLeft(yoga), Yoga.YGNodeLayoutGetTop(yoga));
    }

    class Builder {
        private Nodes children = Nodes.empty();
        private Consumer<MetaNode> ref = n -> {};
        private Layouter layout = (yoga) -> {};
        private Offsetter offsetter = Node::defaultOffsetter;
        private Painter paint = (canvas, yoga) -> {};
        private Painter paintAfter = (canvas, yoga) -> {};

        public Builder children(Nodes nodes) {
            this.children = nodes;
            return this;
        }

        public Builder ref(Consumer<MetaNode> ref) {
            this.ref = ref;
            return this;
        }
        
        public Builder layout(Layouter layouter) {
            this.layout = layouter;
            return this;
        }

        public Builder offset(Offsetter offsetter) {
            this.offsetter = offsetter;
            return this;
        }
        
        public Builder paint(Painter paint) {
            this.paint = paint;
            return this;
        }
        
        public Builder paintAfter(Painter paintAfter) {
            this.paintAfter = paintAfter;
            return this;
        }

        public Node build() {
            return new Composed(this);
        }
    }

    class Composed implements Node {
        private final Nodes children;
        private final Consumer<MetaNode> ref;
        private final Layouter layout;
        private final Painter paint;
        private final Painter paintAfter;

        public Composed(Builder builder) {
            this.children = builder.children;
            this.ref = builder.ref;
            this.layout = builder.layout;
            this.paint = builder.paint;
            this.paintAfter = builder.paintAfter;
        }

        public Nodes children() {
            return children;
        }

        public void ref(MetaNode node) {
            ref.accept(node);
        }

        public void layout(long yoga) {
            layout.layout(yoga);
        }

        public void paint(Canvas canvas, long yoga) {
            paint.paint(canvas, yoga);
        }

        public void paintAfter(Canvas canvas, long yoga) {
            paintAfter.paint(canvas, yoga);
        }
    }
}