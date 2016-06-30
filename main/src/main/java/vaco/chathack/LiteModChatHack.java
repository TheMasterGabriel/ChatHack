package vaco.chathack;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.Maps;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.mumfrey.liteloader.ChatListener;
import com.mumfrey.liteloader.Tickable;
import com.mumfrey.liteloader.core.LiteLoader;
import com.mumfrey.liteloader.modconfig.ConfigStrategy;
import com.mumfrey.liteloader.modconfig.ExposableOptions;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

@ExposableOptions(strategy = ConfigStrategy.Unversioned, filename="chathack.json")
public class LiteModChatHack implements Tickable, ChatListener
{
	public static LiteModChatHack instance;

	private static KeyBinding toggleChatHack = new KeyBinding("key.chathack.toggle", Keyboard.KEY_GRAVE, "key.categories.chathack");
	private static KeyBinding resetChatHack = new KeyBinding("key.chathack.reset", Keyboard.KEY_R, "key.categories.chathack");

	@Expose @SerializedName("scrablmer_words")
	private HashMap<String, String> scramblerMap = Maps.<String, String>newHashMap();

	@Expose @SerializedName("guess_words")
	private HashMap<String, String> guessMap = Maps.<String, String>newHashMap();
	
	@Expose @SerializedName("do_chathack")
	private boolean doChatHack = true;
	
	private boolean waitForScramblerMatch = false;
	private boolean waitForSentence = false;
	private boolean waitForWord = false;
	private boolean waitForSentenceFragment = false;
	private String storedMapKey = null;
	private String storedGuessKey = null;
	private long timeout;
	
	/**
	 * Default constructor. All LiteMods must have a default constructor. In general you should do very little
	 * in the mod constructor EXCEPT for initializing any non-game-interfacing components or performing
	 * sanity checking prior to initialization
	 */
	public LiteModChatHack()
	{
		instance = this;
	}
	
	/**
	 * getName() should be used to return the display name of your mod and MUST NOT return null
	 * 
	 * @see com.mumfrey.liteloader.LiteMod#getName()
	 */
	@Override
	public String getName()
	{
		return "ChatHack";
	}
	
	/**
	 * getVersion() should return the same version string present in the mod metadata, although this is
	 * not a strict requirement.
	 * 
	 * @see com.mumfrey.liteloader.LiteMod#getVersion()
	 */
	@Override
	public String getVersion()
	{
		return "1.4";
	}
	
	/**
	 * init() is called very early in the initialization cycle, before the game is fully initialized, this
	 * means that it is important that your mod does not interact with the game in any way at this point.
	 * 
	 * @see com.mumfrey.liteloader.LiteMod#init(java.io.File)
	 */
	@Override
	public void init(File configPath)
	{
		// The key bindings declared above won't do anything unless we register them, ModUtilties provides a convenience method for this
		LiteLoader.getInput().registerKeyBinding(LiteModChatHack.toggleChatHack);
		LiteLoader.getInput().registerKeyBinding(LiteModChatHack.resetChatHack);
	}
	
	/**
	 * upgradeSettings is used to notify a mod that its version-specific settings are being migrated
	 * 
	 * @see com.mumfrey.liteloader.LiteMod#upgradeSettings(java.lang.String, java.io.File, java.io.File)
	 */
	@Override
	public void upgradeSettings(String version, File configPath, File oldConfigPath) { }
	
	@Override
	public void onTick(Minecraft minecraft, float partialTicks, boolean inGame, boolean clock)
	{
		if(inGame)
		{
			if(LiteModChatHack.toggleChatHack.isPressed())
			{
				this.doChatHack = !this.doChatHack;
				minecraft.thePlayer.addChatMessage(new TextComponentTranslation("message.chathack.toggle." + this.doChatHack, new Object[0]));
			}
			
			if(LiteModChatHack.resetChatHack.isPressed())
			{
				this.waitForScramblerMatch = false;
				this.waitForSentence = false;
				this.waitForWord = false;
				this.scramblerMap.clear();
				this.guessMap.clear();
				minecraft.thePlayer.addChatMessage(new TextComponentTranslation("message.chathack.reset", new Object[0]));
			}
			
			// Our @Expose annotations control what properties get saved, this tells LiteLoader to actually write the properties to disk
			LiteLoader.getInstance().writeConfig(this);
		}
		
		if(System.currentTimeMillis() - this.timeout >= 60000)
		{
			this.storedMapKey = null;
			this.storedGuessKey = null;
			this.waitForScramblerMatch = false;
			this.waitForSentence = false;
			this.waitForWord = false;
			this.timeout = 0;
		}
	}

	@Override
	public void onChat(ITextComponent chat, String message)
	{
		if(this.doChatHack)
		{
			String rawMessage = getRawMessage(chat);
			
			if(message.startsWith("§r§9Type: §r§3the first person to Type ")) //Works
			{
				doDelayedResponse(rawMessage.substring(31, rawMessage.indexOf(" wins!") + 1));
			}
			else if(this.waitForScramblerMatch && message.startsWith("§r§9Unscramble: ") && message.contains(" unscrambled the word ") && message.endsWith(" §r§2seconds!§r"))
			{
				this.scramblerMap.put(this.storedMapKey, rawMessage.substring(rawMessage.indexOf(" unscrambled the word ") + 23, rawMessage.indexOf(" in ") + 1));
				this.storedMapKey = null;
				this.timeout = 0;
				this.waitForScramblerMatch = false;
			}
			else if(rawMessage.startsWith("Unscramble: the first person to Unscramble "))
			{
				char[] chars = rawMessage.substring(43, rawMessage.indexOf(" wins!")).toCharArray();
				Arrays.sort(chars);
				String key = String.valueOf(chars);
				
				if(this.scramblerMap.containsKey(key))
				{
					doDelayedResponse(this.scramblerMap.get(key));
				}
				else
				{
					this.storedMapKey = key;
					this.waitForScramblerMatch = true;
					this.timeout = System.currentTimeMillis();
				}
			}
			else if(this.waitForSentence && message.startsWith("§r§9Write: ")) //Works
			{
				this.waitForSentence = false;
				this.timeout = 0;
				doDelayedResponse(rawMessage.substring(7));
			}
			else if(rawMessage.equals("Write: the first person to Write the following sentence wins!")) //Works
			{
				this.waitForSentence = true;
				this.timeout = System.currentTimeMillis();
			}
			else if(this.waitForSentenceFragment && rawMessage.startsWith("Guess: ") && rawMessage.contains("_"))
			{
				String sentence = rawMessage.substring(7, rawMessage.indexOf(" _"));
				this.waitForSentenceFragment = false;
				
				if(this.guessMap.containsKey(sentence))
				{
					doDelayedResponse(this.guessMap.get(sentence));
				}
				else
				{
					this.storedGuessKey = sentence;
					this.waitForWord = true;
					this.timeout = System.currentTimeMillis();
				}
			}
			else if(this.waitForWord && rawMessage.startsWith("Guess: ") && message.contains(" Guessed the word ") && rawMessage.endsWith(" seconds!"))
			{
				this.guessMap.put(this.storedGuessKey, rawMessage.substring(rawMessage.indexOf(" Guessed the word ") + 18, rawMessage.indexOf(" in ")));
				this.storedGuessKey = null;
				this.timeout = 0;
				this.waitForWord = false;
			}
			else if(rawMessage.equals("Guess: the first person to Guess The word wins!"))
			{
				this.waitForSentenceFragment = true;
				this.timeout = System.currentTimeMillis();
			}
		}
	}
	
	private static void doDelayedResponse(String response)
	{
		try
		{
			new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						Thread.sleep(getDelay(response));
						Minecraft.getMinecraft().thePlayer.sendChatMessage(response);
					}
					catch(InterruptedException e) { }
				}
			}).start();	
		}
		catch(Exception e) { }
	}
	
	private static long getDelay(String response)
	{
		response = response.trim();
		int length = response.length() * 100;
		return (1200 + length) + (long)(new Random().nextDouble() * ((1750 + length) - (1200 + length)));
	}
	
	private static String getRawMessage(ITextComponent message)
	{
		return TextFormatting.getTextWithoutFormattingCodes(message.getFormattedText());
	}
}