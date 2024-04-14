package net.botwithus;

import net.botwithus.rs3.imgui.ImGui;
import net.botwithus.rs3.imgui.ImGuiWindowFlag;
import net.botwithus.rs3.script.ScriptConsole;
import net.botwithus.rs3.script.ScriptGraphicsContext;

public class SkeletonScriptGraphicsContext extends ScriptGraphicsContext {

    private SkeletonScript script;

    public SkeletonScriptGraphicsContext(ScriptConsole scriptConsole, SkeletonScript script) {
        super(scriptConsole);
        this.script = script;
    }

    @Override
    public void drawSettings() {
        if (ImGui.Begin("POF Manure Turner", ImGuiWindowFlag.None.getValue())) {
            if (ImGui.BeginTabBar("My bar", ImGuiWindowFlag.None.getValue())) {
                if (ImGui.BeginTabItem("Settings", ImGuiWindowFlag.None.getValue())) {
                    ImGui.Text("Simple Player Owned Farms Manure Turner");
                    ImGui.Text("The script is: " + script.getBotState());
                    if (ImGui.Button("Start")) {
                        //button has been clicked
                        script.setBotState(SkeletonScript.BotState.Skilling);
                    }
                    ImGui.SameLine();
                    if (ImGui.Button("Stop")) {
                        //has been clicked
                        script.setBotState(SkeletonScript.BotState.IDLE);
                    }
                    ImGui.EndTabItem();
                }
                if (ImGui.BeginTabItem("Instructions", ImGuiWindowFlag.None.getValue())) {
                    ImGui.Text("1. Start the script from anywhere");
                    ImGui.Text("2. The script will automatically traverse then start skilling");
                    ImGui.Text("3. It takes roughly 3 hours to get to 30 farming, then use BushesWithUs");
                    ImGui.Text("4. Happy Farming, enjoy!");
                    ImGui.EndTabItem();
                }
                if (ImGui.BeginTabItem("Stats", ImGuiWindowFlag.None.getValue())) {
                    ImGui.Text("Time Passed: " + script.timePassed());
                    ImGui.Text("TTL: " + script.ttl());
                    ImGui.Text("XP Gained: " + script.xpGained());
                    ImGui.Text("Levels Gained: " + script.levelsGained());
                    ImGui.Text("XP/HR: " + script.xpPerHour());
                    ImGui.EndTabItem();
                }
                ImGui.EndTabBar();
            }
            ImGui.End();
        }

    }

    @Override
    public void drawOverlay() {
        super.drawOverlay();
    }
}
