package com.github.wilgaboury.sigwig;

import com.github.wilgaboury.jsignal.Ref;
import com.github.wilgaboury.jsignal.Signal;
import com.github.wilgaboury.sigui.*;
import com.github.wilgaboury.sigui.event.EventListener;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Matrix33;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.types.Rect;
import org.lwjgl.util.yoga.Yoga;

import static com.github.wilgaboury.jsignal.ReactiveUtil.*;
import static com.github.wilgaboury.sigui.event.EventListener.onMouseOut;
import static com.github.wilgaboury.sigui.event.EventListener.onMouseOver;

public class Scroller extends Component {
    private final Nodes children;

    private final Signal<Float> yOffset;
//    private final Signal<Float> xOffset;
    private final Signal<Boolean> overBar;
    private final Ref<MetaNode> inner;

    public Scroller(Nodes children) {
        this.children = children;
        this.yOffset = createSignal(0f);
        this.overBar = createSignal(false);
        this.inner = new Ref<>();
    }

    @Override
    public Nodes render() {

        return Nodes.single(Node.builder()
                .ref(node -> node.listen(
                        EventListener.onScroll(e -> {
                            var height = node.getLayout().getSize().getY();
                            var max = inner.get().getLayout().getSize().getY() - height;
                            yOffset.accept(v -> Math.min(0, Math.max(-max, v + e.getDeltaY())));
                        })
                ))
                .layout(yoga -> {
                    Yoga.YGNodeStyleSetWidthPercent(yoga, 100f);
                    Yoga.YGNodeStyleSetHeightPercent(yoga, 100f);
                    Yoga.YGNodeStyleSetOverflow(yoga, Yoga.YGOverflowScroll);
                })
                .children(Nodes.multiple(
                        Node.builder()
                                .ref(inner::set)
                                .layout(yoga -> Yoga.YGNodeStyleSetWidthPercent(yoga, 100f))
                                .transform(node -> {

                                    var height = node.getParent().getLayout().getSize().getY();
                                    var max = node.getLayout().getSize().getY() - height;
                                    // TODO: bypass
                                    yOffset.accept(Math.min(0, Math.max(-max, yOffset.get())));
                                    return Matrix33.makeTranslate(0, yOffset.get());
                                })
                                .children(children)
                                .build(),
                        Node.builder()
                                .ref(node -> node.listen(
                                        onMouseOver(e -> overBar.accept(true)),
                                        onMouseOut(e -> overBar.accept(false))
                                ))
                                .layout(Flex.builder()
                                        .width(15f)
                                        .heightPercent(100f)
                                        .absolute()
                                        .top(0)
                                        .right(0)
                                        .build()
                                )
                                .paint(this::paintScrollBar)
                                .build()
                ))
                .build()
        );
    }

    private void paintScrollBar(Canvas canvas, MetaNode node) {
        var viewSize = node.getLayout().getSize();
        var contentSize = inner.get().getLayout().getSize();

        var bounds = Rect.makeWH(node.getLayout().getSize());

        var yScale = viewSize.getY() / contentSize.getY();

        try (var paint = new Paint()) {
            paint.setColor(EzColors.BLACK);
            canvas.drawRect(Rect.makeXYWH(
                    0,
                    yScale * -yOffset.get(),
                    bounds.getWidth(),
                    yScale * viewSize.getY()
            ), paint);
        }
    }
}
