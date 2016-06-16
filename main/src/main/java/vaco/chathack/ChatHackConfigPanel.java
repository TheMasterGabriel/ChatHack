package vaco.chathack;

import org.lwjgl.input.Keyboard;

import com.mumfrey.liteloader.core.LiteLoader;
import com.mumfrey.liteloader.modconfig.ConfigPanel;
import com.mumfrey.liteloader.modconfig.ConfigPanelHost;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiTextField;

public class ChatHackConfigPanel extends Gui implements ConfigPanel
{
	protected static final Minecraft mc = Minecraft.getMinecraft();
	protected GuiLabel labels;
	protected GuiTextField typeMessage;
	protected GuiTextField typeMessage2;

	@Override
	public void onPanelShown(ConfigPanelHost host)
	{
		Keyboard.enableRepeatEvents(true);
		this.labels = new GuiLabel(mc.fontRendererObj, 0, 20, 10, 200, 20, -1);
		
		this.labels.addLine("The chat message for Type (before the actual word)");
		
		this.typeMessage = new GuiTextField(1, mc.fontRendererObj, 20, 30, 200, 20);
		this.typeMessage.setText("Type: the first person to Type");
		
		this.labels.addLine("The chat message for Type (before the actual word)");
		
		this.typeMessage2 = new GuiTextField(2, mc.fontRendererObj, 20, 60, 200, 20);
		this.typeMessage2.setText("Type: the first person to Type");
	}
	
	@Override
	public String getPanelTitle()
	{
		return "ChatHack Config Options";
	}

	@Override
	public int getContentHeight()
	{
		return 4;
	}

	@Override
	public void onPanelResize(ConfigPanelHost host) { }

	@Override
	public void onPanelHidden()
	{
		LiteLoader.getInstance().writeConfig(LiteModChatHack.instance);
	}

	@Override
	public void drawPanel(ConfigPanelHost host, int mouseX, int mouseY, float partialTicks)
	{
		this.labels.drawLabel(mc, mouseX, mouseY);
		this.typeMessage.drawTextBox();
		this.typeMessage2.drawTextBox();
	}

	@Override
	public void mousePressed(ConfigPanelHost host, int mouseX, int mouseY, int mouseButton) { }

	@Override
	public void mouseReleased(ConfigPanelHost host, int mouseX, int mouseY, int mouseButton) { }

	@Override
	public void mouseMoved(ConfigPanelHost host, int mouseX, int mouseY) { }

	@Override
	public void keyPressed(ConfigPanelHost host, char keyChar, int keyCode)
	{
		if(keyCode == Keyboard.KEY_ESCAPE) host.close();
	}
	
	@Override
	public void onTick(ConfigPanelHost host) { }
}