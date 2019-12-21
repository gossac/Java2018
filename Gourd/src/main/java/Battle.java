import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Battle
{
    private int scale;
    private int cycle;
    private Soldier[][] soldier;
    private Image background;
    private File record;
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;
    private boolean triggered;
    private String locator;
    class InnerThread extends Thread
    {
        private Battle parent;
        public InnerThread(Battle parent)
        {
            this.parent = parent;
        }
        @Override
        public void run()
        {
            String buffer = null;
            String identifier;
            int abscissa;
            int ordinate;
            while (true)
            {
                try
                {
                    TimeUnit.MILLISECONDS.sleep(cycle);
                } catch (InterruptedException interruptedException) {}
                try
                {
                    buffer = bufferedReader.readLine();
                } catch (IOException iOException) {}
                if (buffer == null)
                    break;
                for (int i = 0;i < scale;i ++)
                    for (int j = 0;j < scale;j ++)
                        soldier[i][j] = null;
                try
                {
                    while (true)
                    {
                        identifier = buffer.substring(0, buffer.indexOf("\t"));
                        buffer = buffer.substring(buffer.indexOf("\t") + 1);
                        abscissa = Integer.valueOf(buffer.substring(0, buffer.indexOf("\t")));
                        buffer = buffer.substring(buffer.indexOf("\t") + 1);
                        ordinate = Integer.valueOf(buffer.substring(0, buffer.indexOf("\t")));
                        buffer = buffer.substring(buffer.indexOf("\t") + 1);
                        if (identifier.equals("scorpion.png") || identifier.equals("snake.png") || identifier.equals("centipede.png"))
                            soldier[abscissa][ordinate] = new Soldier(parent, false, identifier);
                        else
                        if (identifier.equals("grandfather.png") || (identifier.substring(0, 5).equals("gourd") && identifier.substring(6).equals(".png") && identifier.charAt(5) > '0' && identifier.charAt(5) < '8'))
                            soldier[abscissa][ordinate] = new Soldier(parent, true, identifier);
                        if (buffer.indexOf("\t") < 0)
                        {
                            buffer = null;
                            break;
                        }
                    }
                } catch (Exception exception)
                {
                    System.err.println("unrecognized text message");
                }
            }
            interrupted();
        }
    }
    public Battle()
    {
        triggered = false;
        AnnotationParser.parseAnnotation(getClass());
        cycle = 500;
        scale = 8;
        soldier = new Soldier[scale][scale];
        bufferedWriter = null;
        bufferedReader = null;
        for (int i = 0;i < scale;i ++)
            for (int j = 0;j < scale;j ++)
                if (i == 0)
                {
                    if (j == 0)
                        soldier[i][j] = new Soldier(this, false, "scorpion.png");
                    else
                    if (j == scale - 1)
                        soldier[i][j] = new Soldier(this, false, "snake.png");
                    else
                        soldier[i][j] = new Soldier(this, false, "centipede.png");
                }
                else
                if (i == scale - 1)
                {
                    if (j == 0)
                        soldier[i][j] = new Soldier(this, true, "grandfather.png");
                    else
                        soldier[i][j] = new Soldier(this, true, "gourd" + j + ".png");
                }
                else
                    soldier[i][j] = null;
        locator = "mountain.png";
        background = new ImageIcon(getClass().getClassLoader().getResource(locator)).getImage();
    }

    @CustomizedAnnotation(function = "open a log and set everything on")
    public void begin()
    {
        String name = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date()) + ".jrec";
        record = new File(name);
        if (!record.exists())
            try
            {
                record.createNewFile();
                System.err.println("record saved as " + name);
            } catch (IOException iOException)
            {
                record = null;
            }
        try
        {
            bufferedWriter = new BufferedWriter(new FileWriter(record.getAbsoluteFile()));
        } catch (Exception exception) {}
        for (int i = 0;i < scale;i ++)
        {
            soldier[0][i].start();
            soldier[scale - 1][i].start();
        }
        triggered = true;
    }
    @CustomizedAnnotation(function = "open the log and activate a replay")
    public void load(String name)
    {
        record = new File(name);
        try
        {
            if (!record.exists())
                throw new FileNotFoundException();
            bufferedReader = new BufferedReader(new FileReader(record.getAbsoluteFile()));
        } catch (FileNotFoundException fileNotFoundException)
        {
            return;
        }
        triggered = true;
        new InnerThread(this).start();
    }
    @CustomizedAnnotation(function = "reveal the shift of a soldier, usually followed by a conflict detection")
    public void march(Soldier marcher)
    {
        int marcherAbscissa;
        int marcherOrdinate;
        boolean act = false;
        synchronized (soldier)
        {
            loop:
            for (marcherAbscissa = 0;marcherAbscissa < scale;marcherAbscissa ++)
                for (marcherOrdinate = 0;marcherOrdinate < scale;marcherOrdinate ++)
                    if (soldier[marcherAbscissa][marcherOrdinate] == marcher)
                    {
                        int targetAbscissa;
                        int targetOrdinate;
                        int entry = (int)(Math.random() * 4);
                        for (int i = 1;i <= 2 * (scale - 1);i ++)
                            for (int j = 0;j <= i;j ++)
                                for (int k = 0;k < 4;k ++)
                                {
                                    entry = (entry + k) % 4;
                                    if (entry == 0)
                                    {
                                        if (marcherAbscissa - j >= 0 && marcherOrdinate - (i - j) >= 0 && soldier[marcherAbscissa - j][marcherOrdinate - (i - j)] != null && soldier[marcherAbscissa - j][marcherOrdinate - (i - j)].getCamp() != marcher.getCamp())
                                        {
                                            targetAbscissa = marcherAbscissa - j;
                                            targetOrdinate = marcherOrdinate - (i - j);
                                            boolean subEntry = Math.random() < 0.5;
                                            for (int l = 0;l < 2;l ++)
                                            {
                                                subEntry = !subEntry;
                                                if (subEntry)
                                                {
                                                    if (marcherAbscissa > 0 && soldier[marcherAbscissa - 1][marcherOrdinate] == null)
                                                    {
                                                        soldier[marcherAbscissa - 1][marcherOrdinate] = marcher;
                                                        soldier[marcherAbscissa][marcherOrdinate] = null;
                                                        marcherAbscissa --;
                                                        act = true;
                                                        break;
                                                    }
                                                }
                                                else
                                                {
                                                    if (marcherOrdinate > 0 && soldier[marcherAbscissa][marcherOrdinate - 1] == null)
                                                    {
                                                        soldier[marcherAbscissa][marcherOrdinate - 1] = marcher;
                                                        soldier[marcherAbscissa][marcherOrdinate] = null;
                                                        marcherOrdinate --;
                                                        act = true;
                                                        break;
                                                    }
                                                }
                                            }
                                            if (act)
                                                if (Math.abs(targetAbscissa - marcherAbscissa) + Math.abs(targetOrdinate - marcherOrdinate) <= 1)
                                                    if (Math.random() < 0.5)
                                                    {
                                                        soldier[marcherAbscissa][marcherOrdinate] = null;
                                                        seize();
                                                        marcher.interrupt();
                                                    }
                                                    else
                                                    {
                                                        soldier[targetAbscissa][targetOrdinate].interrupt();
                                                        soldier[targetAbscissa][targetOrdinate] = null;
                                                        seize();
                                                    }
                                                else
                                                    seize();
                                            break loop;
                                        }
                                    }
                                    else
                                    if (entry == 1)
                                    {
                                        if (marcherAbscissa - j >= 0 && marcherOrdinate + (i - j) < scale && soldier[marcherAbscissa - j][marcherOrdinate + (i - j)] != null && soldier[marcherAbscissa - j][marcherOrdinate + (i - j)].getCamp() != marcher.getCamp())
                                        {
                                            targetAbscissa = marcherAbscissa - j;
                                            targetOrdinate = marcherOrdinate + (i - j);
                                            boolean subEntry = Math.random() < 0.5;
                                            for (int l = 0;l < 2;l ++)
                                            {
                                                subEntry = !subEntry;
                                                if (subEntry)
                                                {
                                                    if (marcherAbscissa > 0 && soldier[marcherAbscissa - 1][marcherOrdinate] == null)
                                                    {
                                                        soldier[marcherAbscissa - 1][marcherOrdinate] = marcher;
                                                        soldier[marcherAbscissa][marcherOrdinate] = null;
                                                        seize();
                                                        marcherAbscissa --;
                                                        act = true;
                                                        break;
                                                    }
                                                }
                                                else
                                                {
                                                    if (marcherOrdinate + 1 < scale && soldier[marcherAbscissa][marcherOrdinate + 1] == null)
                                                    {
                                                        soldier[marcherAbscissa][marcherOrdinate + 1] = marcher;
                                                        soldier[marcherAbscissa][marcherOrdinate] = null;
                                                        seize();
                                                        marcherOrdinate ++;
                                                        act = true;
                                                        break;
                                                    }
                                                }
                                            }
                                            if (act)
                                                if (Math.abs(targetAbscissa - marcherAbscissa) + Math.abs(targetOrdinate - marcherOrdinate) <= 1)
                                                    if (Math.random() < 0.5)
                                                    {
                                                        soldier[marcherAbscissa][marcherOrdinate] = null;
                                                        seize();
                                                        marcher.interrupt();
                                                    }
                                                    else
                                                    {
                                                        soldier[targetAbscissa][targetOrdinate].interrupt();
                                                        soldier[targetAbscissa][targetOrdinate] = null;
                                                        seize();
                                                    }
                                                else
                                                    seize();
                                            break loop;
                                        }
                                    }
                                    else
                                    if (entry == 2)
                                    {
                                        if (marcherAbscissa + j < scale && marcherOrdinate - (i - j) >= 0 && soldier[marcherAbscissa + j][marcherOrdinate - (i - j)] != null && soldier[marcherAbscissa + j][marcherOrdinate - (i - j)].getCamp() != marcher.getCamp())
                                        {
                                            targetAbscissa = marcherAbscissa + j;
                                            targetOrdinate = marcherOrdinate - (i - j);
                                            boolean subEntry = Math.random() < 0.5;
                                            for (int l = 0;l < 2;l ++)
                                            {
                                                subEntry = !subEntry;
                                                if (subEntry)
                                                {
                                                    if (marcherAbscissa + 1 < scale && soldier[marcherAbscissa + 1][marcherOrdinate] == null)
                                                    {
                                                        soldier[marcherAbscissa + 1][marcherOrdinate] = marcher;
                                                        soldier[marcherAbscissa][marcherOrdinate] = null;
                                                        seize();
                                                        marcherAbscissa ++;
                                                        act = true;
                                                        break;
                                                    }
                                                }
                                                else
                                                {
                                                    if (marcherOrdinate > 0 && soldier[marcherAbscissa][marcherOrdinate - 1] == null)
                                                    {
                                                        soldier[marcherAbscissa][marcherOrdinate - 1] = marcher;
                                                        soldier[marcherAbscissa][marcherOrdinate] = null;
                                                        seize();
                                                        marcherOrdinate --;
                                                        act = true;
                                                        break;
                                                    }
                                                }
                                            }
                                            if (act)
                                                if (Math.abs(targetAbscissa - marcherAbscissa) + Math.abs(targetOrdinate - marcherOrdinate) <= 1)
                                                    if (Math.random() < 0.5)
                                                    {
                                                        soldier[marcherAbscissa][marcherOrdinate] = null;
                                                        seize();
                                                        marcher.interrupt();
                                                    }
                                                    else
                                                    {
                                                        soldier[targetAbscissa][targetOrdinate].interrupt();
                                                        soldier[targetAbscissa][targetOrdinate] = null;
                                                        seize();
                                                    }
                                                else
                                                    seize();
                                            break loop;
                                        }
                                    }
                                    else
                                    if (marcherAbscissa + j < scale && marcherOrdinate + (i - j) < scale && soldier[marcherAbscissa + j][marcherOrdinate + (i - j)] != null && soldier[marcherAbscissa + j][marcherOrdinate + (i - j)].getCamp() != marcher.getCamp())
                                    {
                                        targetAbscissa = marcherAbscissa + j;
                                        targetOrdinate = marcherOrdinate + (i - j);
                                        boolean subEntry = Math.random() < 0.5;
                                        for (int l = 0;l < 2;l ++)
                                        {
                                            subEntry = !subEntry;
                                            if (subEntry)
                                            {
                                                if (marcherAbscissa + 1 < scale && soldier[marcherAbscissa + 1][marcherOrdinate] == null)
                                                {
                                                    soldier[marcherAbscissa + 1][marcherOrdinate] = marcher;
                                                    soldier[marcherAbscissa][marcherOrdinate] = null;
                                                    seize();
                                                    marcherAbscissa ++;
                                                    act = true;
                                                    break;
                                                }
                                            }
                                            else
                                            {
                                                if (marcherOrdinate + 1 < scale && soldier[marcherAbscissa][marcherOrdinate + 1] == null)
                                                {
                                                    soldier[marcherAbscissa][marcherOrdinate + 1] = marcher;
                                                    soldier[marcherAbscissa][marcherOrdinate] = null;
                                                    seize();
                                                    marcherOrdinate ++;
                                                    act = true;
                                                    break;
                                                }
                                            }
                                        }
                                        if (act)
                                            if (Math.abs(targetAbscissa - marcherAbscissa) + Math.abs(targetOrdinate - marcherOrdinate) <= 1)
                                                if (Math.random() < 0.5)
                                                {
                                                    soldier[marcherAbscissa][marcherOrdinate] = null;
                                                    seize();
                                                    marcher.interrupt();
                                                }
                                                else
                                                {
                                                    soldier[targetAbscissa][targetOrdinate].interrupt();
                                                    soldier[targetAbscissa][targetOrdinate] = null;
                                                    seize();
                                                }
                                            else
                                                seize();
                                        break loop;
                                    }
                                }
                        break loop;
                    }
            if (act)
            {
                try
                {
                    TimeUnit.MILLISECONDS.sleep(cycle);
                } catch (InterruptedException interruptedException) {}
            }
        }
    }
    @CustomizedAnnotation(function = "take the screenshot")
    private synchronized void seize()
    {
        try
        {
            for (int i = 0; i < scale; i++)
                for (int j = 0; j < scale; j++)
                    if (soldier[i][j] != null)
                        bufferedWriter.write(soldier[i][j] + "\t" + i + "\t" + j + "\t");
            bufferedWriter.write("\n");
            bufferedWriter.flush();
        } catch (IOException iOException) {}
    }
    @CustomizedAnnotation(function = "check if a battle is triggered, in which case the key listener is supposed disabled", revision = 2)
    public boolean isTriggered()
    {
        return triggered;
    }
    @CustomizedAnnotation(function = "check if a battle is terminated, in which case the I/O handler is supposed disabled")
    public boolean isTerminated()
    {
        Soldier survivor = null;
        for (int i = 0;i < scale;i ++)
            for (int j = 0;j < scale;j ++)
                if (soldier[i][j] != null)
                    if (survivor == null)
                        survivor = soldier[i][j];
                    else
                    {
                        if (survivor.getCamp() != soldier[i][j].getCamp())
                            return false;
                        survivor = soldier[i][j];
                    }
        for (int i = 0;i < scale;i ++)
            for (int j = 0;j < scale;j ++)
                if (soldier[i][j] != null)
                {
                    soldier[i][j].stayStill();
                    soldier[i][j] = null;
                }
        if (bufferedWriter != null)
            try
            {
                bufferedWriter.close();
            } catch (IOException iOException) {}
        if (bufferedReader != null)
            try
            {
                bufferedReader.close();
            } catch (IOException iOException) {}
        return true;
    }
    @CustomizedAnnotation(function = "inform the battlefield size, for exhibition")
    public int getScale()
    {
        return scale;
    }
    @CustomizedAnnotation(function = "tell what to cast, for exhibition")
    public Image getAppearance(int abscissa, int ordinate)
    {
        if (abscissa >= scale || ordinate >= scale || soldier[abscissa][ordinate] == null)
            return null;
        return soldier[abscissa][ordinate].getAppearance();
    }
    @CustomizedAnnotation(function = "pass back the tile, for exhibition")
    public Image getBackground()
    {
        return background;
    }
}