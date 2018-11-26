package ru.deysa.plugin.base64.base64ToolWindowFactory;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import ru.deysa.plugin.base64.ComparableRunnable;

import javax.swing.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.*;

public class Base64ToolWindowFactory implements ToolWindowFactory {

    private JPanel myToolWindowContent;
    private JTextArea encodeText;
    private JTextArea decodeText;
    private ToolWindow myToolWindow;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public Base64ToolWindowFactory() {

        encodeText.addFocusListener(new FocusListener() {
            private ScheduledFuture<?> future;
            private ComparableRunnable<String> decoder;

            @Override
            public void focusGained(FocusEvent e) {
                decoder = decodeText(encodeText, decodeText);
                future = scheduler.scheduleAtFixedRate(decoder, 1000, 500, TimeUnit.MILLISECONDS);
            }

            @Override
            public void focusLost(FocusEvent e) {
                CompletableFuture.runAsync(decoder).thenAccept(v -> future.cancel(true));
            }
        });

        decodeText.addFocusListener(new FocusListener() {
            private ScheduledFuture<?> future;
            private ComparableRunnable<String> encoder;

            @Override
            public void focusGained(FocusEvent e) {
                encoder = encodeText(encodeText, decodeText);
                future = scheduler.scheduleAtFixedRate(encoder, 1000, 500, TimeUnit.MILLISECONDS);
            }

            @Override
            public void focusLost(FocusEvent e) {
                CompletableFuture.runAsync(encoder).thenAccept(v -> future.cancel(true));
            }
        });
    }

    private ComparableRunnable<String> decodeText(JTextArea from, JTextArea to) {
        return new ComparableRunnable<String>() {
            @Override
            public void run(String value) {
                to.setText(decode(value));
            }

            @Override
            public String getValue() {
                return from.getText();
            }
        };
    }

    private ComparableRunnable<String> encodeText(JTextArea from, JTextArea to) {
        return new ComparableRunnable<String>() {
            @Override
            public void run(String value) {
                from.setText(encode(value));
            }

            @Override
            public String getValue() {
                return to.getText();
            }
        };
    }

    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        myToolWindow = toolWindow;

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(myToolWindowContent, "", false);
        toolWindow.getContentManager().addContent(content);
    }

    private static String decode(String data) {
        byte[] decode = Base64.getDecoder().decode(data);
        return new String(decode, StandardCharsets.UTF_8);
    }

    private static String encode(String data) {
        byte[] encode = Base64.getEncoder().encode(data.getBytes(StandardCharsets.UTF_8));
        return new String(encode, StandardCharsets.UTF_8);
    }

}