package net.botwithus;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.botwithus.api.game.hud.inventories.Backpack;
import net.botwithus.internal.scripts.ScriptDefinition;
import net.botwithus.rs3.events.impl.ChatMessageEvent;
import net.botwithus.rs3.game.Client;
import net.botwithus.rs3.game.hud.interfaces.Component;
import net.botwithus.rs3.game.hud.interfaces.Interfaces;
import net.botwithus.rs3.game.js5.types.NpcType;
import net.botwithus.rs3.game.login.LoginManager;
import net.botwithus.rs3.game.minimenu.MiniMenu;
import net.botwithus.rs3.game.minimenu.actions.ComponentAction;
import net.botwithus.rs3.game.movement.Movement;
import net.botwithus.rs3.game.movement.NavPath;
import net.botwithus.rs3.game.movement.TraverseEvent;
import net.botwithus.rs3.game.queries.builders.animations.ProjectileQuery;
import net.botwithus.rs3.game.queries.builders.characters.NpcQuery;
import net.botwithus.rs3.game.queries.builders.objects.SceneObjectQuery;
import net.botwithus.rs3.game.scene.entities.characters.npc.Npc;
import net.botwithus.rs3.game.scene.entities.characters.player.LocalPlayer;
import net.botwithus.rs3.game.scene.entities.object.SceneObject;
import net.botwithus.rs3.script.Execution;
import net.botwithus.rs3.script.LoopingScript;
import net.botwithus.rs3.script.config.ScriptConfig;
import net.botwithus.rs3.events.impl.SkillUpdateEvent;
import net.botwithus.rs3.game.skills.Skills;
import net.botwithus.rs3.game.*;
import net.botwithus.rs3.game.actionbar.ActionBar;
import net.botwithus.rs3.events.EventBus;
import net.botwithus.rs3.game.login.LoginManager.*;

import java.util.Random;

public class SkeletonScript extends LoopingScript {

    private BotState botState = BotState.IDLE;
    private boolean someBool = true;
    private Random random = new Random();

    //World Hopping List//
    private int[] memberWorlds = {
            1, 5, 6, 9, 10, 12, 14, 15, 16, 21, 22, 23, 24, 25, 26, 27, 28, 31, 32, 35, 36, 37, 39, 40, 44, 45,
            46, 49, 50, 51, 53, 54, 58, 59, 60, 62, 63, 64, 65, 67, 68, 69, 70, 71, 72, 73, 74, 76, 77, 78, 79,
            82, 83, 85, 88, 89, 91, 92, 97, 98, 99, 100, 103, 104, 105, 106, 116, 117, 119, 123, 124, 134, 138,
            140, 139, 252, 257, 258, 259
    };
    //Botstate//
    enum BotState {
        //define your own states here
        IDLE,
        Skilling,
    }
    //ChatMessage Stunned + No Food//
    public SkeletonScript(String s, ScriptConfig scriptConfig, ScriptDefinition scriptDefinition) {
        super(s, scriptConfig, scriptDefinition);
        this.sgc = new SkeletonScriptGraphicsContext(getConsole(), this);
    }

    @Override
    public void onLoop() {
        //Loops every 100ms by default, to change:
        //this.loopDelay = 500;
        LocalPlayer player = Client.getLocalPlayer();
        if (player == null || Client.getGameState() != Client.GameState.LOGGED_IN || botState == BotState.IDLE) {
            //wait some time so we dont immediately start on login.
            Execution.delay(random.nextLong(3000,7000));
            return;
        }

        //Botstate//
        switch (botState) {
            case IDLE -> {
                //do nothing
                println("We're idle!");
                Execution.delay(random.nextLong(1000,3000));
            }
            case Skilling -> {
                //do some code that handles your skilling
                Execution.delay(handleSkilling(player));
            }
        }
    }
    //Health Percentage Calculator//
    public boolean shouldEat(LocalPlayer player) {
        double healthPercentage = ((double) player.getCurrentHealth() / player.getMaximumHealth()) * 100;
        return healthPercentage < 40;
    }
    //Skilling Class: Interfaces, Traversal, Worldhopping, Backpack & Eating//
    private long handleSkilling(LocalPlayer player) {
        //Check for Skilling Interfaces and delay if true//
        if (Interfaces.isOpen(1251))
            return random.nextLong(250, 1500);
        //Let's Check our Traversal//
        Area ManureArea = new Area.Rectangular(new Coordinate(2657, 3363, 0), new Coordinate(2660,3366,0));
        if (Movement.traverse(NavPath.resolve(ManureArea)) == TraverseEvent.State.FINISHED) {
            Execution.delay(random.nextLong(500,1500));
            botState = BotState.Skilling;
        }
        //Turning Manure//
        SceneObject ManureMound = SceneObjectQuery.newQuery().name("Manure mound").option("Turn").results().first();
        if (ManureMound != null) {
            if (player.getAnimationId() == -1) {
                ((SceneObject) ManureMound).interact("Turn");
                Execution.delay(random.nextLong(1500,3000));
                println("Attempting to Shovel");
            } else if (player.getAnimationId() == 32263) {
                // Do nothing for animation ID 32263
                println("Shovelling In Progress");
                return random.nextLong(1500,3000);
            }
        }
        else if (ManureMound == null) {
            println("No Manure mound, going Idle");
            botState = BotState.IDLE;
        }
        return random.nextLong(1500,3000);
    }
    //STATISTICS//
    //XP Gain & Level Gain base is set to zero,
    private int xpGained = 0;
    private int levelsGained = 0;
    private long startTime;
    private int xpPerHour;
    private String ttl; // Time to level

    //XP Gain & Level Gain is calculated and added to base
    @Override
    public boolean initialize() {
        startTime = System.currentTimeMillis();
        xpGained = 0;
        levelsGained = 0;

        subscribe(SkillUpdateEvent.class, skillUpdateEvent -> {
            if (skillUpdateEvent.getId() == Skills.THIEVING.getId()) {
                xpGained += (skillUpdateEvent.getExperience() - skillUpdateEvent.getOldExperience());
                if (skillUpdateEvent.getOldActualLevel() < skillUpdateEvent.getActualLevel()) {
                    levelsGained++;
                }
            }
        });

        return super.initialize();
    }

    public String levelsGained() {
        return levelsGained + " Levels";
    }

    public String xpPerHour() {
        long currentTime = System.currentTimeMillis();
        if (currentTime > startTime) {
            xpPerHour = (int) (xpGained * 3600000.0 / (currentTime - startTime));
        }
        return xpPerHour + " XP/hr";
    }

    public String ttl() {
        if (xpPerHour > 0) {
            int xpToNextLevel = Skills.THIEVING.getExperienceToNextLevel();
            int totalSeconds = (int) (xpToNextLevel * 3600.0 / xpPerHour);
            int hours = totalSeconds / 3600;
            int minutes = (totalSeconds % 3600) / 60;
            int seconds = totalSeconds % 60;
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
        return "N/A";
    }

    public String timePassed() {
        long currentTime = System.currentTimeMillis();
        long elapsedMillis = currentTime - startTime;
        long hours = elapsedMillis / 3600000;
        long minutes = (elapsedMillis % 3600000) / 60000;
        long seconds = (elapsedMillis % 60000) / 1000;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public String xpGained() {
        return xpGained + " XP";
    }



    ////////////////////Botstate/////////////////////
    public BotState getBotState() {
        return botState;
    }

    public void setBotState(BotState botState) {
        this.botState = botState;
    }

    public boolean isSomeBool() {
        return someBool;
    }

    public void setSomeBool(boolean someBool) {
        this.someBool = someBool;
    }
}
