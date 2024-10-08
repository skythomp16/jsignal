package com.github.wilgaboury.sigwig;

import com.github.wilgaboury.jsignal.Computed;
import com.github.wilgaboury.jsignal.ComputedContext;
import com.github.wilgaboury.jsignal.Constant;
import com.github.wilgaboury.jsignal.Context;
import com.github.wilgaboury.jsignal.Effect;
import com.github.wilgaboury.jsignal.Provider;
import com.github.wilgaboury.sigui.*;
import com.github.wilgaboury.sigui.layout.LayoutConfig;
import com.github.wilgaboury.sigui.paint.SurfacePaintCacheStrategy;
import com.github.wilgaboury.sigui.paint.UpgradingPaintCacheStrategy;
import com.github.wilgaboury.sigwig.ez.EzNode;
import io.github.humbleui.skija.Data;
import io.github.humbleui.skija.FontStyle;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.skija.Typeface;
import io.github.humbleui.skija.paragraph.Alignment;
import io.github.humbleui.skija.paragraph.BaselineMode;
import io.github.humbleui.skija.paragraph.DecorationStyle;
import io.github.humbleui.skija.paragraph.Direction;
import io.github.humbleui.skija.paragraph.FontCollection;
import io.github.humbleui.skija.paragraph.HeightMode;
import io.github.humbleui.skija.paragraph.Paragraph;
import io.github.humbleui.skija.paragraph.ParagraphBuilder;
import io.github.humbleui.skija.paragraph.ParagraphStyle;
import io.github.humbleui.skija.paragraph.StrutStyle;
import io.github.humbleui.skija.paragraph.TypefaceFontProvider;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.wilgaboury.jsignal.JSignalUtil.createMemo;

@SiguiComponent
public class Para implements Renderable {
  private static final FontCollection defaultCollection = new FontCollection();

  public static ParaStyleContext style = new ParaStyleContext();
  public static Context<FontCollection> fonts = Context.create(defaultCollection);

  public static final TypefaceFontProvider fontManager = new TypefaceFontProvider();

  static {
    defaultCollection.setDefaultFontManager(io.github.humbleui.skija.FontMgr.getDefault());
    defaultCollection.setDynamicFontManager(fontManager);
  }

  public static void loadFontFromResource(String location) throws IOException {
    try (InputStream resource = Para.class.getResourceAsStream(location)) {
      if (resource != null) {
        fontManager.registerTypeface(Typeface.makeFromData(Data.makeFromBytes(resource.readAllBytes())));
      }
    }
  }

  private final Supplier<Paragraph> para;
  private final Supplier<Boolean> line;

  public Para(Builder builder) {
    this.para = builder.paragraph;
    this.line = builder.line;
  }

  public static Para from(Paragraph paragraph) {
    return Para.builder().setParagraph(paragraph).build();
  }

  public static Para from(Supplier<Paragraph> paragraph) {
    return Para.builder().setParagraph(paragraph).build();
  }

  public static Para fromString(String string) {
    return Para.builder().setString(string).build();
  }

  public static Para fromString(Supplier<String> string) {
    return Para.builder().setString(string).build();
  }

  @Override
  public NodesSupplier render() {
    var provider = Provider.get();
    return EzNode.builder()
      .ref(meta -> meta.setPaintCacheStrategy(
        new UpgradingPaintCacheStrategy(SurfacePaintCacheStrategy::new)))
      .id("para")
      .layout(config -> {
        SiguiThread.queueMicrotask(() -> {
          provider.provide(() -> {
            Effect.create(() -> {
              // track state
              para.get();
              line.get();

              config.markDirty();
              SiguiWindow.context.use().requestLayout();
            });
          });
        });
        config.setMeasure((width, widthMode, height, heightMode) -> {
          var p = para.get();
          p.layout(width);
          float intrinsicWidth = Math.round(p.getMaxIntrinsicWidth() + 0.5f);
          return new LayoutConfig.Size(
            line.get() ? intrinsicWidth : Math.min(width, intrinsicWidth),
            p.getHeight()
          );
        });
      })
      .paint((canvas, layout) -> {
        var p = para.get();
        // Extra layout call prevents wierd cutoff errors.
        // I know you, at some point you will see this and think, "this is a repeat call to
        // layout, let's just delete this line to make the code simpler and faster".
        // DON'T DELETE IT!
        p.layout(layout.getWidth());
        p.paint(canvas, 0f, 0f);
      })
      .build();
  }

  public Paragraph getParagraph() {
    return para.get();
  }



  public static BuilderSetContent builder() {
    return new Builder();
  }

  public interface BuilderSetContent {
    BuilderContentString setString(String string);
    BuilderContentString setString(Supplier<String> string);
    BuilderContentParagraph setParagraph(Paragraph paragraph);
    BuilderContentParagraph setParagraph(Supplier<Paragraph> paragraph);
  }

  public interface BuilderContentString {
    BuilderContentString setStyle(Style style);
    BuilderContentString setStyle(Supplier<Style> style);
    BuilderContentString setStyle(Function<StyleBuilder, StyleBuilder> customize);
    BuilderContentParagraph setLine(boolean line);
    BuilderContentParagraph setLine(Supplier<Boolean> line);
    Para build();
  }

  public interface BuilderContentParagraph {
    BuilderContentParagraph setLine(boolean line);
    BuilderContentParagraph setLine(Supplier<Boolean> line);
    Para build();
  }

  public static class Builder implements BuilderSetContent, BuilderContentParagraph, BuilderContentString {
    private @Nullable Supplier<String> string;
    private Supplier<Style> style = Para.style.use();

    private Supplier<Paragraph> paragraph;
    private Supplier<Boolean> line = Constant.of(false);

    @Override
    public Builder setString(String string) {
      this.string = Constant.of(string);
      return this;
    }

    @Override
    public Builder setString(Supplier<String> string) {
      this.string = createMemo(string);
      return this;
    }

    @Override
    public Builder setStyle(Style style) {
      return this.setStyle(Constant.of(style));
    }

    @Override
    public Builder setStyle(Supplier<Style> style) {
      this.style = createMemo(style);
      return this;
    }

    @Override
    public Builder setStyle(Function<StyleBuilder, StyleBuilder> customize) {
      this.style = createMemo(() -> customize.apply(Para.style.use().get().toBuilder()).build());
      return this;
    }

    @Override
    public Builder setParagraph(Paragraph paragraph) {
      return this.setParagraph(Constant.of(paragraph));
    }

    @Override
    public Builder setParagraph(Supplier<Paragraph> paragraph) {
      this.paragraph = paragraph;
      return this;
    }

    @Override
    public Builder setLine(boolean line) {
      return this.setLine(Constant.of(line));
    }

    @Override
    public Builder setLine(Supplier<Boolean> line) {
      this.line = line;
      return this;
    }

    private void setParagraphFromString() {
      assert string != null;

      Supplier<Paragraph> compute = () -> {
        var result = new ParagraphBuilder(style.get().toSkia(), fonts.use());
        result.addText(string.get());
        return result.build();
      };

      if (string instanceof Constant<String> && style instanceof Constant<Style>) {
        paragraph = Constant.of(compute.get());
      } else {
        paragraph = Computed.create(compute);
      }
    }

    public Para build() {
      if (string != null) {
        setParagraphFromString();
      }
      return new Para(this);
    }
  }

  public static class ParaStyleContext extends ComputedContext<Style> {
    public ParaStyleContext() {
      super(Constant.of(styleBuilder().build()));
    }

    public Provider.Entry customize(Function<StyleBuilder, StyleBuilder> customize) {
      return withComputed(s -> customize.apply(s.toBuilder()).build());
    }
  }

  public static StyleBuilder styleBuilder() {
    return new StyleBuilder();
  }

  public static TextStyleBuilder textStyleBuilder() {
    return new TextStyleBuilder();
  }

  public static class TextStyle {
    private final Collection<String> fontFamilies;
    private boolean placeholder;
    private final @Nullable FontStyle fontStyle;
    private final @Nullable Integer color;
    private final @Nullable Float fontSize;
    private final @Nullable Float fontHeight;
    private final @Nullable io.github.humbleui.skija.Paint foreground;
    private final @Nullable Paint background;
    private final @Nullable BaselineMode baselineMode;
    private final @Nullable DecorationStyle decorationStyle;
    private final @Nullable Float letterSpacing;
    private final @Nullable String locale;
    private final @Nullable Typeface typeface;
    private final @Nullable Float wordSpacing;

    public TextStyle(TextStyleBuilder builder) {
      fontFamilies = Collections.unmodifiableCollection(builder.fontFamilies);
      placeholder = builder.placeholder;
      fontStyle = builder.fontStyle;
      color = builder.color;
      fontSize = builder.fontSize;
      fontHeight = builder.fontHeight;
      foreground = builder.foreground;
      background = builder.background;
      baselineMode = builder.baselineMode;
      decorationStyle = builder.decorationStyle;
      letterSpacing = builder.letterSpacing;
      locale = builder.locale;
      typeface = builder.typeface;
      wordSpacing = builder.wordSpacing;
    }

    public io.github.humbleui.skija.paragraph.TextStyle toSkia() {
      var text = new io.github.humbleui.skija.paragraph.TextStyle();
      text.setFontFamilies(fontFamilies.toArray(new String[0]));
      if (placeholder) text.setPlaceholder();
      if (fontStyle != null) text.setFontStyle(fontStyle);
      if (color != null) text.setColor(color);
      if (fontSize != null) text.setFontSize(fontSize);
      if (fontHeight != null) text.setHeight(fontHeight);
      if (foreground != null) text.setForeground(foreground);
      if (background != null) text.setBackground(background);
      if (baselineMode != null) text.setBaselineMode(baselineMode);
      if (decorationStyle != null) text.setDecorationStyle(decorationStyle);
      if (letterSpacing != null) text.setLetterSpacing(letterSpacing);
      if (locale != null) text.setLocale(locale);
      if (typeface != null) text.setTypeface(typeface);
      if (wordSpacing != null) text.setWordSpacing(wordSpacing);
      return text;
    }

    public TextStyleBuilder toBuilder() {
      return textStyleBuilder()
        .setFontFamilies(fontFamilies)
        .setPlaceholder(placeholder)
        .setFontStyle(fontStyle)
        .setColor(color)
        .setFontSize(fontSize)
        .setFontHeight(fontHeight)
        .setForeground(foreground)
        .setBackground(background)
        .setBaselineMode(baselineMode)
        .setDecorationStyle(decorationStyle)
        .setLetterSpacing(letterSpacing)
        .setLocale(locale)
        .setTypeface(typeface)
        .setWordSpacing(wordSpacing);
    }

    public Collection<String> getFontFamilies() {
      return fontFamilies;
    }

    public boolean isPlaceholder() {
      return placeholder;
    }

    @Nullable
    public FontStyle getFontStyle() {
      return fontStyle;
    }

    @Nullable
    public Integer getColor() {
      return color;
    }

    @Nullable
    public Float getFontSize() {
      return fontSize;
    }

    @Nullable
    public Float getFontHeight() {
      return fontHeight;
    }

    @Nullable
    public Paint getForeground() {
      return foreground;
    }

    @Nullable
    public Paint getBackground() {
      return background;
    }

    @Nullable
    public BaselineMode getBaselineMode() {
      return baselineMode;
    }

    @Nullable
    public DecorationStyle getDecorationStyle() {
      return decorationStyle;
    }

    @Nullable
    public Float getLetterSpacing() {
      return letterSpacing;
    }

    @Nullable
    public String getLocale() {
      return locale;
    }

    @Nullable
    public Typeface getTypeface() {
      return typeface;
    }

    @Nullable
    public Float getWordSpacing() {
      return wordSpacing;
    }
  }

  public static class TextStyleBuilder {
    private Collection<String> fontFamilies = Collections.emptyList();
    private boolean placeholder = false;
    private @Nullable FontStyle fontStyle;
    private @Nullable Integer color;
    private @Nullable Float fontSize;
    private @Nullable Float fontHeight;
    private @Nullable Paint foreground;
    private @Nullable Paint background;
    private @Nullable BaselineMode baselineMode;
    private @Nullable DecorationStyle decorationStyle;
    private @Nullable Float letterSpacing;
    private @Nullable String locale;
    private @Nullable Typeface typeface;
    private @Nullable Float wordSpacing;

    public TextStyleBuilder setFontFamilies(String... fontFamilies) {
      return setFontFamilies(List.of(fontFamilies));
    }

    public TextStyleBuilder setFontFamilies(Collection<String> fontFamilies) {
      this.fontFamilies = List.copyOf(fontFamilies);
      return this;
    }

    public TextStyleBuilder setPlaceholder(boolean placeholder) {
      this.placeholder = placeholder;
      return this;
    }

    public TextStyleBuilder setFontStyle(@Nullable FontStyle fontStyle) {
      this.fontStyle = fontStyle;
      return this;
    }

    public TextStyleBuilder setColor(@Nullable Integer color) {
      this.color = color;
      return this;
    }

    public TextStyleBuilder setFontSize(@Nullable Float fontSize) {
      this.fontSize = fontSize;
      return this;
    }

    public TextStyleBuilder setFontHeight(@Nullable Float fontHeight) {
      this.fontHeight = fontHeight;
      return this;
    }

    public TextStyleBuilder setForeground(@Nullable Paint foreground) {
      this.foreground = foreground;
      return this;
    }

    public TextStyleBuilder setBackground(@Nullable Paint background) {
      this.background = background;
      return this;
    }

    public TextStyleBuilder setBaselineMode(@Nullable BaselineMode baselineMode) {
      this.baselineMode = baselineMode;
      return this;
    }

    public TextStyleBuilder setDecorationStyle(@Nullable DecorationStyle decorationStyle) {
      this.decorationStyle = decorationStyle;
      return this;
    }

    public TextStyleBuilder setLetterSpacing(@Nullable Float letterSpacing) {
      this.letterSpacing = letterSpacing;
      return this;
    }

    public TextStyleBuilder setLocale(@Nullable String locale) {
      this.locale = locale;
      return this;
    }

    public TextStyleBuilder setTypeface(@Nullable Typeface typeface) {
      this.typeface = typeface;
      return this;
    }

    public TextStyleBuilder setWordSpacing(@Nullable Float wordSpacing) {
      this.wordSpacing = wordSpacing;
      return this;
    }

    public TextStyle build() {
      return new TextStyle(this);
    }
  }

  public static class Style {
    private final TextStyle textStyle;
    private final @Nullable Alignment alignment;
    private final @Nullable Float height;
    private final @Nullable Direction direction;
    private final @Nullable StrutStyle strutStyle;
    private final @Nullable String ellipsis;
    private final @Nullable HeightMode heightMode;
    private final @Nullable Long maxLinesCount;

    public Style(StyleBuilder builder) {
      this.textStyle = builder.textStyle;
      this.alignment = builder.alignment;
      this.height = builder.height;
      this.direction = builder.direction;
      this.strutStyle = builder.strutStyle;
      this.ellipsis = builder.ellipsis;
      this.heightMode = builder.heightMode;
      this.maxLinesCount = builder.maxLinesCount;
    }

    public ParagraphStyle toSkia() {
      var para = new ParagraphStyle();
      para.setTextStyle(textStyle.toSkia());
      if (alignment != null) para.setAlignment(alignment);
      if (height != null) para.setHeight(height);
      if (direction != null) para.setDirection(direction);
      if (strutStyle != null) para.setStrutStyle(strutStyle);
      if (ellipsis != null) para.setEllipsis(ellipsis);
      if (heightMode != null) para.setHeightMode(heightMode);
      if (maxLinesCount != null) para.setMaxLinesCount(maxLinesCount);
      return para;
    }

    public StyleBuilder toBuilder() {
      return styleBuilder()
        .setTextStyle(textStyle)
        .setAlignment(alignment)
        .setHeight(height)
        .setDirection(direction)
        .setStrutStyle(strutStyle)
        .setEllipsis(ellipsis)
        .setHeightMode(heightMode)
        .setMaxLinesCount(maxLinesCount);
    }

    public TextStyle getTextStyle() {
      return textStyle;
    }

    @Nullable
    public Alignment getAlignment() {
      return alignment;
    }

    @Nullable
    public Float getHeight() {
      return height;
    }

    @Nullable
    public Direction getDirection() {
      return direction;
    }

    @Nullable
    public StrutStyle getStrutStyle() {
      return strutStyle;
    }

    @Nullable
    public String getEllipsis() {
      return ellipsis;
    }

    @Nullable
    public HeightMode getHeightMode() {
      return heightMode;
    }

    @Nullable
    public Long getMaxLinesCount() {
      return maxLinesCount;
    }
  }

  public static class StyleBuilder {
    private TextStyle textStyle = textStyleBuilder().build();
    private @Nullable Alignment alignment;
    private @Nullable Float height;
    private @Nullable Direction direction;
    private @Nullable StrutStyle strutStyle;
    private @Nullable String ellipsis;
    private @Nullable HeightMode heightMode;
    private @Nullable Long maxLinesCount;

    public TextStyle getTextStyle() {
      return textStyle;
    }

    public StyleBuilder setTextStyle(TextStyle textStyle) {
      this.textStyle = textStyle;
      return this;
    }

    public StyleBuilder setTextStyle(Function<TextStyleBuilder, TextStyleBuilder> func) {
      this.textStyle = func.apply(textStyle.toBuilder()).build();
      return this;
    }

    @Nullable
    public Alignment getAlignment() {
      return alignment;
    }

    public StyleBuilder setAlignment(@Nullable Alignment alignment) {
      this.alignment = alignment;
      return this;
    }

    @Nullable
    public Float getHeight() {
      return height;
    }

    public StyleBuilder setHeight(@Nullable Float height) {
      this.height = height;
      return this;
    }

    @Nullable
    public Direction getDirection() {
      return direction;
    }

    public StyleBuilder setDirection(@Nullable Direction direction) {
      this.direction = direction;
      return this;
    }

    @Nullable
    public StrutStyle getStrutStyle() {
      return strutStyle;
    }

    public StyleBuilder setStrutStyle(@Nullable StrutStyle strutStyle) {
      this.strutStyle = strutStyle;
      return this;
    }

    @Nullable
    public String getEllipsis() {
      return ellipsis;
    }

    public StyleBuilder setEllipsis(@Nullable String ellipsis) {
      this.ellipsis = ellipsis;
      return this;
    }

    @Nullable
    public HeightMode getHeightMode() {
      return heightMode;
    }

    public StyleBuilder setHeightMode(@Nullable HeightMode heightMode) {
      this.heightMode = heightMode;
      return this;
    }

    @Nullable
    public Long getMaxLinesCount() {
      return maxLinesCount;
    }

    public StyleBuilder setMaxLinesCount(@Nullable Long maxLinesCount) {
      this.maxLinesCount = maxLinesCount;
      return this;
    }

    public Style build() {
      return new Style(this);
    }
  }
}
