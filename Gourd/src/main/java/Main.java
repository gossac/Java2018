/**
 * Gourd-oriented Programming
 * @version 4.2
 * @author 151140042
 */

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.concurrent.TimeUnit;

public class Main extends JFrame
{
    class Display extends JPanel
    {
        public Display(final JFrame parent)
        {
            setFocusable(true);
            addKeyListener(new KeyAdapter()
            {
                @Override
                public void keyPressed(KeyEvent keyEvent)
                {
                    if (keyEvent.getKeyCode() == KeyEvent.VK_SPACE && !battle.isTriggered())
                        battle.begin();
                    if (keyEvent.getKeyCode() == KeyEvent.VK_L && !battle.isTriggered())
                    {
                        JFileChooser fileChooser = new JFileChooser();
                        fileChooser.setCurrentDirectory(new File("."));
                        fileChooser.setAcceptAllFileFilterUsed(false);
                        fileChooser.setFileFilter(new FileFilter()
                        {
                            @Override
                            public boolean accept(File candidate)
                            {
                                return candidate.getName().endsWith(".jrec") || candidate.isDirectory();
                            }
                            @Override
                            public String getDescription()
                            {
                                return "记录文件(*.jrec)";
                            }
                        });
                        fileChooser.setDialogTitle("jrec chooser");
                        if (fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION)
                        {
                            battle.load(fileChooser.getSelectedFile().getName());
                        }
                    }
                }
            });
        }
        @Override
        public void paint(Graphics graphics)
        {
            super.paint(graphics);
            graphics.drawImage(battle.getBackground(), 0, 0, scale * size, scale * size, this);
            for (int i = 0;i < scale;i ++)
                for (int j = 0;j < scale;j ++)
                    if (battle.getAppearance(i, j) != null)
                        graphics.drawImage(battle.getAppearance(i, j), i * size, j * size, size, size, this);
        }
    }
    private Main()
    {
        size = 100;
        battle = new Battle();
        scale = battle.getScale();
        cycle = 250;
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setTitle(getClass().getSimpleName());
        setSize(scale * size, scale * size);
        Display display = new Display(this);
        add(display);
    }
    private static int cycle;
    private static Battle battle;
    private static int scale;
    private static int size;
    public static void main(String[] args)
    {
        Main window = new Main();
        while (true)
        {
            if (battle.isTriggered() && battle.isTerminated())
            {
                window.repaint();
                break;
            }
            window.repaint();
            try
            {
                TimeUnit.MILLISECONDS.sleep(cycle);
            } catch (InterruptedException interruptedException) {}
        }
    }
}