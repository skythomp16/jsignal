package com.github.wilgaboury.sigui;

import com.github.wilgaboury.jsignal.Computed;
import com.github.wilgaboury.jsignal.JSignalUtil;

import java.util.*;
import java.util.function.Supplier;

import static com.github.wilgaboury.jsignal.JSignalUtil.createMemo;

public interface Nodes extends Supplier<List<Node>>
{
    static Nodes from(Supplier<List<Node>> supplier) {
        return supplier::get;
    }

    static Nodes empty() {
        return Collections::emptyList;
    }

    static Nodes compose(Collection<? extends Supplier<? extends List<Node>>> compose) {
        var memos = compose.stream().map(JSignalUtil::createMemo).toList();
        return Nodes.from(createMemo(() -> memos.stream().flatMap(memo -> memo.get().stream()).toList()));
    }

    static <T> Dynamic forEach(Supplier<? extends List<T>> list, BiFunction<T, Supplier<Integer>, Nodes> map) {
        var mapped = JSignalUtil.createMapped(list, map);
        var composed = Computed.create(() -> mapped.get().stream().flatMap(n -> n.getNodeList().stream()).toList());
        return new Dynamic(composed);
    }
}
