package com.github.wilgaboury.jsignal;

import com.github.wilgaboury.experimental.Component3;
import com.github.wilgaboury.sigui.Node;
import com.github.wilgaboury.sigui.Sigui;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.*;

public class HotSwapTest {



    @Test
    public static void main(String[] args) throws IOException {
        Sigui.start(() -> {
            Component3 component3 = new MyComponent();
            var computed = component3.render();
        });
    }

    public static class MyComponent extends Component3 {
        @Override
        public Computed<Node> render() {
            return ReactiveUtil.createComputed(() -> {
                System.out.println("bruh4");
                return null;
            });
        }
    }
}
