package com.github.wilgaboury.jsigwig.examples;

import com.github.wilgaboury.sigwig.Button;
import io.github.humbleui.skija.*;
import io.github.humbleui.skija.paragraph.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

public class InterFontUtil {
  private static final Logger logger = LoggerFactory.getLogger(InterFontUtil.class);

  private static final Typeface interRegular;

  private static final String[] interResourceLocations = new String[]{
    "/fonts/Inter-Regular.ttf",
    "/fonts/Inter-Bold.ttf",
    "/fonts/Inter-Italic.ttf",
  };

  private static final TypefaceFontProvider interFontMgr = new TypefaceFontProvider();
  private static final FontCollection fontCollection = new FontCollection();

  static {
    FontMgr mgr = FontMgr.getDefault();
    int families = mgr.getFamiliesCount();
    for (int i = 0; i < families; i++) {
      System.out.println(mgr.getFamilyName(i));
    }

    try {
      for (var location : interResourceLocations) {
        try (InputStream resource = InterFontUtil.class.getResourceAsStream(location)) {
          if (resource != null) {
            interFontMgr.registerTypeface(Typeface.makeFromData(Data.makeFromBytes(resource.readAllBytes())));
          }
        }
      }
      try (InputStream resource = InterFontUtil.class.getResourceAsStream("/fonts/Inter-Regular.ttf")) {
        if (resource != null) {
          interRegular = Typeface.makeFromData(Data.makeFromBytes(resource.readAllBytes()));
        } else {
          throw new RuntimeException("inter-regular font resource is missing");
        }
      }
    } catch (IOException e) {
      logger.error("failed to load inter font");
      throw new RuntimeException(e);
    }

    fontCollection.setDefaultFontManager(interFontMgr);
    fontCollection.setTestFontManager(interFontMgr);
    fontCollection.setEnableFallback(false);
  }

  public static Paragraph createParagraph(String text, int color, float size) {
    var style = new TextStyle();
    style.setColor(color);
    style.setFontSize(size);
    style.setFontFamily("Inter");

    var paraStyle = new ParagraphStyle();
    paraStyle.setTextStyle(style);

    var builder = new ParagraphBuilder(paraStyle, fontCollection);
    builder.pushStyle(style);
    builder.addText(text);
    builder.popStyle();

    return builder.build();
  }

  public static TextLine createTextLine(String string, float size) {
    var font = new Font();
    font.setTypeface(interRegular);
    font.setSize(size);
    return TextLine.make(string, font);
  }

  public static Button.Children createButtonText(String string) {
    return createButtonText(() -> string);
  }

    public static Button.Children createButtonText(Supplier<String> string) {
    return (textSize, textColor) -> com.github.wilgaboury.sigwig.text.TextLine.builder()
      .setLine(() -> createTextLine(string.get(), textSize.get()))
      .setColor(textColor)
      .build();
  }
}
