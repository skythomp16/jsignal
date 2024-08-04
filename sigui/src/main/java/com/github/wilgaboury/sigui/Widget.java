package com.github.wilgaboury.sigui;

import com.github.wilgaboury.jsignal.JSignalUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static com.github.wilgaboury.jsignal.JSignalUtil.createMemo;

public class Widget {
  public static Supplier<List<Node>> empty() {
    return Collections::emptyList;
  }

  static Supplier<List<Node>> compose(Collection<? extends Supplier<? extends List<Node>>> widgets) {
    var memos = widgets.stream().map(JSignalUtil::createMemo).toList();
    return createMemo(() -> memos.stream().flatMap(w -> w.get().stream()).toList());
  }
}
