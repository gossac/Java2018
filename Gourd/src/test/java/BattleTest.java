import org.junit.*;

public class BattleTest
{
    private static Battle battle;
    @BeforeClass
    public static void prepareBattle()
    {
        battle = new Battle();
        battle.begin();
    }
    @Test
    public void testIsTriggered() throws Exception
    {
        if (!battle.isTriggered())
            throw new Exception();
    }
    @AfterClass
    public static void completeBattle()
    {
        while (true)
            if (battle.isTerminated())
                break;
    }
}