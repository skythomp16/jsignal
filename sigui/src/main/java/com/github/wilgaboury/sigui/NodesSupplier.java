package com.github.wilgaboury.sigui;

import java.util.List;

@FunctionalInterface
public interface NodesSupplier {
  List<Node> get();
}
