package net.minecraft.launcher.ui.tabs;

import com.mojang.util.QueueLogAppender;
import net.minecraft.launcher.Launcher;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ConsoleTab extends JScrollPane
{
    private static final Font MONOSPACED = new Font("Monospaced", Font.PLAIN, 12);
    private final JTextArea console;
    private final JPopupMenu popupMenu;
    private final JMenuItem copyTextButton;
    private final Launcher minecraftLauncher;
    
    public ConsoleTab(final Launcher minecraftLauncher) {
        this.console = new JTextArea();
        this.popupMenu = new JPopupMenu();
        this.copyTextButton = new JMenuItem("Copy All Text");
        this.minecraftLauncher = minecraftLauncher;
        this.popupMenu.add(this.copyTextButton);
        this.console.setComponentPopupMenu(this.popupMenu);
        this.copyTextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                try {
                    final StringSelection ss = new StringSelection(ConsoleTab.this.console.getText());
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
                }
                catch (Exception ignored) { }
            }
        });
        this.console.setFont(ConsoleTab.MONOSPACED);
        this.console.setEditable(false);
        this.console.setMargin(null);
        this.setViewportView(this.console);
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String line;
                while ((line = QueueLogAppender.getNextLogEvent("DevelopmentConsole")) != null) {
                    ConsoleTab.this.print(line);
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
    
    public Launcher getMinecraftLauncher() {
        return this.minecraftLauncher;
    }
    
    public void print(final String line) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    ConsoleTab.this.print(line);
                }
            });
            return;
        }
        final Document document = this.console.getDocument();
        final JScrollBar scrollBar = this.getVerticalScrollBar();
        boolean shouldScroll = false;
        if (this.getViewport().getView() == this.console) {
            shouldScroll = (scrollBar.getValue() + scrollBar.getSize().getHeight() + ConsoleTab.MONOSPACED.getSize() * 4 > scrollBar.getMaximum());
        }
        try {
            document.insertString(document.getLength(), line, null);
        }
        catch (BadLocationException ignored) { }
        if (shouldScroll) {
            scrollBar.setValue(Integer.MAX_VALUE);
        }
    }
}
