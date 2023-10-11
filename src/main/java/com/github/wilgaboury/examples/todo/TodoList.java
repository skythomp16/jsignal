package com.github.wilgaboury.examples.todo;

import com.github.wilgaboury.jsignal.Context;
import com.github.wilgaboury.jsignal.ReactiveList;
import com.github.wilgaboury.jsignal.ReactiveUtil;
import com.github.wilgaboury.jsignal.interfaces.SignalLike;
import com.github.wilgaboury.sigui.Component;
import com.github.wilgaboury.sigwig.Flex;
import com.github.wilgaboury.sigwig.Text;

import java.util.List;
import java.util.function.Supplier;

import static com.github.wilgaboury.jsignal.ReactiveUtil.createProvider;

public class TodoList {
    public static final Context<Supplier<Integer>> ItemIdxContext = ReactiveUtil.createContext(() -> -1);

    public static Component create(Supplier<List<Item>> items) {
        return Flex.builder().column().children((
                ReactiveList.createMapped(items, TodoList::createItem)
        ));
    }

    private static Component createItem(Item value, Supplier<Integer> idx) {
        return createProvider(ItemIdxContext.provide(idx), () ->
                Flex.builder().column().children(ReactiveList.of(Text.create(value.getText())))
        );
    }

    public static class Item {
        private final SignalLike<Boolean> checked;
        private final SignalLike<String> text;

        public Item(SignalLike<Boolean> checked, SignalLike<String> text) {
            this.checked = checked;
            this.text = text;
        }

        public SignalLike<Boolean> getChecked() {
            return checked;
        }

        public SignalLike<String> getText() {
            return text;
        }
    }
}
