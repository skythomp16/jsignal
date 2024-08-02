package com.github.wilgaboury.jsignal;

import javax.swing.*;
import java.util.*;

public class Batch {
  static final ThreadLocal<Batch> batch = ThreadLocal.withInitial(Batch::new);

  private boolean inBatch;
  private final LinkedHashMap<Integer, Entry> effects;

  private DebugTreeNode root;
  private DebugTreeNode cur;

  public Batch() {
    inBatch = false;
    effects = new LinkedHashMap<>();
  }

  public void run(Runnable inner) {
    if (inBatch) {
      inner.run();
    } else {
      inBatch = true;
      root = new DebugTreeNode(null, null, null, new ArrayList<>());
      cur = root;
      try {
        inner.run();
        while (!effects.isEmpty()) {
          var effect = effects.firstEntry();
          effects.remove(effect.getKey());

          cur = effect.getValue().node();
          effect.getValue().ref().run();
        }
      } finally {
        inBatch = false;
        if (!root.children.isEmpty()) {
          System.out.println("FINISHED REACTIVE EXECUTION:");
          System.out.println(root);
        }
      }
    }
  }

  void add(EffectRef ref, Signal<?> signal) {
    assert inBatch;

    var node = new DebugTreeNode(cur, signal, (Effect) ref.getEffect().get(), new ArrayList<>());
    if (node.parent != null) {
      node.parent.children.add(node);
    }
    effects.putIfAbsent(ref.getId(), new Entry(ref, node));
  }

  private record Entry(EffectRef ref, DebugTreeNode node) {
  }

  private record DebugTreeNode(DebugTreeNode parent, Signal<?> signal, Effect effect, List<DebugTreeNode> children) {
    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder();
      if (signal != null) {
        builder.append("Signal:\n");
        for (int i = 0; i < 10; i++) {
          builder.append(signal.declared[i]);
          builder.append("\n");
        }
      }
      if (effect != null) {
        builder.append("Effect:\n");
        for (int i = 0; i < 10; i++) {
          builder.append(effect.declared[i]);
          builder.append("\n");
        }
      }
      for (var child : children) {
        var childStr = child.toString();
        if (!childStr.isEmpty()) {
          builder.append("\t");
          builder.append(childStr.replaceAll("\n(?!$)", "\n\t"));
        }
      }
      return builder.toString();
    }
  }
}
