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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: Alexey.Chursin
 * Date: Aug 25, 2010
 * Time: 2:09:00 PM
 */
public class Base64ToolWindowFactory implements ToolWindowFactory {

    private JPanel myToolWindowContent;
    private JTextArea encodeText;
    private JTextArea decodeText;
    private ToolWindow myToolWindow;

    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);

    public Base64ToolWindowFactory() {

        encodeText.addFocusListener(new FocusListener() {
            private ScheduledFuture<?> decodeHandle;

            @Override
            public void focusGained(FocusEvent e) {
                decodeHandle = decodeText(encodeText, decodeText);
            }

            @Override
            public void focusLost(FocusEvent e) {
                decodeHandle.cancel(true);
            }
        });

        decodeText.addFocusListener(new FocusListener() {
            private ScheduledFuture<?> encodeHandle;

            @Override
            public void focusGained(FocusEvent e) {
                encodeHandle = encodeText(encodeText, decodeText);
            }

            @Override
            public void focusLost(FocusEvent e) {
                encodeHandle.cancel(true);
            }
        });
    }

    private ScheduledFuture<?> decodeText(JTextArea from, JTextArea to) {
        final ComparableRunnable<String> decoder = new ComparableRunnable<String>() {
            @Override
            public void run(String value) {
                to.setText(decode(value));
            }

            @Override
            public String getValue() {
                return from.getText();
            }
        };
        return scheduler.scheduleAtFixedRate(decoder, 1, 1, TimeUnit.SECONDS);
    }

    private ScheduledFuture<?> encodeText(JTextArea from, JTextArea to) {
        final ComparableRunnable<String> encoder = new ComparableRunnable<String>() {
            @Override
            public void run(String value) {
                from.setText(encode(value));
            }

            @Override
            public String getValue() {
                return to.getText();
            }
        };
        return scheduler.scheduleAtFixedRate(encoder, 1, 1, TimeUnit.SECONDS);
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