package com.github.wilgaboury.sigwig;

import com.github.wilgaboury.sigui.Node;
import org.lwjgl.util.yoga.Yoga;

public class Flex implements Node.Layouter {
    private final Insets margins;
    private final Float border;
    private final Insets padding;
    private final Integer justify;
    private final Integer align;
    private final Integer direction;
    private final Integer wrap;
    private final Float gap;
    private final MaybePercent<Float> height;
    private final MaybePercent<Float> width;

    public Flex(Builder builder) {
        this.margins = builder.margins;
        this.border = builder.border;
        this.padding = builder.padding;
        this.justify = builder.justify;
        this.align = builder.align;
        this.direction = builder.direction;
        this.wrap = builder.wrap;
        this.gap = builder.gap;
        this.height = builder.height;
        this.width = builder.width;
    }
    
    @Override
    public void layout(long yoga) {
        if (margins != null) {
            margins.set(Yoga::YGNodeStyleSetMargin, yoga);
        }

        if (border != null) {
            Yoga.YGNodeStyleSetBorder(yoga, Yoga.YGEdgeAll, border);
        }

        if (padding != null) {
            padding.set(Yoga::YGNodeStyleSetPadding, yoga);
        }

        if (justify != null) {
            Yoga.YGNodeStyleSetJustifyContent(yoga, justify);
        }

        if (align != null) {
            Yoga.YGNodeStyleSetAlignItems(yoga, align);
        }

        if (direction != null) {
            Yoga.YGNodeStyleSetFlexDirection(yoga, direction);
        }

        if (wrap != null) {
            Yoga.YGNodeStyleSetFlexWrap(yoga, wrap);
        }

        if (width != null) {
            if (width.isPercent()) {
                Yoga.YGNodeStyleSetWidthPercent(yoga, width.value());
            } else {
                Yoga.YGNodeStyleSetWidth(yoga, width.value());
            }
        }

        if (height != null) {
            if (height.isPercent()) {
                Yoga.YGNodeStyleSetHeightPercent(yoga, height.value());
            } else {
                Yoga.YGNodeStyleSetHeight(yoga, height.value());
            }
        }

        if (gap != null) {
            Yoga.YGNodeStyleSetGap(yoga, Yoga.YGGutterAll, gap);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Insets margins;
        private Float border;
        private Insets padding;
        private Integer justify;
        private Integer align;
        private Integer direction;
        private Integer wrap;
        private Float gap;
        private MaybePercent<Float> height;
        private MaybePercent<Float> width;

        public Builder center() {
            this.justify = Yoga.YGJustifyCenter;
            this.align = Yoga.YGAlignCenter;
            return this;
        }

        public Builder stretch() {
            this.width = new MaybePercent<>(true, 100f);
            this.height = new MaybePercent<>(true, 100f);
            return this;
        }

        public Builder row() {
            this.direction = Yoga.YGFlexDirectionRow;
            return this;
        }

        public Builder column() {
            this.direction = Yoga.YGFlexDirectionColumn;
            return this;
        }

        public Builder margins(Insets margins) {
            this.margins = margins;
            return this;
        }

        public Builder padding(Insets padding) {
            this.padding = padding;
            return this;
        }

        public Builder border(Float border) {
            this.border = border;
            return this;
        }

        public Builder wrap() {
            this.wrap = Yoga.YGWrapWrap;
            return this;
        }

        public Builder gap(Float gap) {
            this.gap = gap;
            return this;
        }

        public Builder height(float height) {
            this.height = new MaybePercent<>(false, height);
            return this;
        }

        public Builder width(float width) {
            this.width = new MaybePercent<>(false, width);
            return this;
        }

        public Builder heightPercent(float height) {
            this.height = new MaybePercent<>(true, height);
            return this;
        }

        public Builder widthPercent(float width) {
            this.width = new MaybePercent<>(true, width);
            return this;
        }

        public Flex build() {
            return new Flex(this);
        }
    }

    private record MaybePercent<T>(boolean isPercent, T value) {};
}
