import javax.swing.*;
import java.awt.*;
import java.util.concurrent.TimeUnit;

public class Soldier extends Thread
{
    private Battle battle;
    private boolean camp;
    private int cycle;
    private String locator;
    private Image appearance;
    private Object ready;
    Soldier(Battle battle, boolean camp, String locator)
    {
        AnnotationParser.parseAnnotation(getClass());
        this.battle = battle;
        this.locator = locator;
        this.appearance =  new ImageIcon(getClass().getClassLoader().getResource(locator)).getImage();
        this.camp = camp;
        cycle = 1000;
        ready = new Object();
    }
    @CustomizedAnnotation(function = "wave the flag")
    public boolean getCamp()
    {
        return camp;
    }
    @Override
    public void run()
    {
        try
        {
            while (true)
            {
                if (Thread.interrupted())
                    break;
                if (Math.random() < 0.5)
                    synchronized (ready)
                    {
                        battle.march(this);
                    }
                TimeUnit.MILLISECONDS.sleep(cycle);
            }
        } catch (InterruptedException interruptedException) {}
    }
    @CustomizedAnnotation(function = "tell what to cast, for exhibition")
    public Image getAppearance()
    {
        return appearance;
    }
    @CustomizedAnnotation(function = "wait when exiting (a concurrency modifier, not so elegant)")
    public void stayStill()
    {
        synchronized (ready)
        {
            interrupt();
        }
    }
    @Override
    public String toString()
    {
        return locator;
    }
}