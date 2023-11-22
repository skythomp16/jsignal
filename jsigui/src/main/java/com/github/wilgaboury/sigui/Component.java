package com.github.wilgaboury.sigui;

import com.github.wilgaboury.jsignal.Provider;

import static com.github.wilgaboury.jsignal.Provide.currentProvider;
import static com.github.wilgaboury.jsignal.Provide.provide;

public abstract class Component {
    public abstract Nodes render();

    public static void onMount(Runnable inner) {
        Provider provider = currentProvider();
        SiguiUtil.invokeLater(() -> provide(provider, inner));
    }
}
