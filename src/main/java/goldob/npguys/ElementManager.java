/*
* NPGuys - Bukkit plugin for better NPC interaction
* Copyright (C) 2014 Adam Gotlib <Goldob>
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package goldob.npguys;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import me.ragan262.quester.Quester;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.CharacterManager;
import com.sucy.skill.SkillAPI;

import goldob.npguys.action.Action;
import goldob.npguys.commands.CommandUtils;
import goldob.npguys.conversation.Conversation;
import goldob.npguys.exception.ActionMissingException;
import goldob.npguys.exception.RequirementMissingException;
import goldob.npguys.exception.UIInitializationFailedException;
import goldob.npguys.exception.UIMissingException;
import goldob.npguys.quest.QuestHandler;
import goldob.npguys.quest.handler.QuesterHandler;
import goldob.npguys.requirement.Requirement;
import goldob.npguys.ui.ConversationUI;
import me.blackvein.quests.Quests;
import net.citizensnpcs.api.CitizensPlugin;
import net.milkbowl.vault.economy.Economy;

public class ElementManager {
	private static Economy economy = null;
	private static CitizensPlugin citizens = null;
	private static QuestHandler questHandler = null;
	private static CharacterManager heroesCharacterManager = null;
	private static SkillHandler skillHandler = null;
	
	private static Map<String, Class<? extends Action>> actions;
	private static Map<String, Class<? extends Requirement>> requirements;
	private static Map<String, Class<? extends ConversationUI>> uiTypes;
	
	private static String defaultUI;
	
	private ElementManager() {}
	
	public static void init(NPGuys plugin) {
		reload(plugin);
		
		actions = new HashMap<String, Class<? extends Action>>();
		requirements = new HashMap<String, Class<? extends Requirement>>();
		uiTypes = new HashMap<String, Class<? extends ConversationUI>>();
	}
	
	public static void reload(NPGuys plugin) {
		setupCitizens(plugin);
		setupEconomy(plugin);
		setupHeroes(plugin);
		setupQuestHandler(plugin);
		setupSkillHandler(plugin);
		
		defaultUI = NPGuys.getPlugin().getConfig().getString("ui.default").toUpperCase();
	}
	
	private static void setupSkillHandler(NPGuys plugin) {
		if(plugin.getServer().getPluginManager().isPluginEnabled("SkillAPI")) {
			skillHandler = new SkillHandler() {
				@Override
				public boolean hasSkill(Player player, String skill) {
					return SkillAPI.getPlayerData(player).hasSkill(skill);
				}
			};
		} else if(heroesCharacterManager != null) {
			skillHandler = new SkillHandler() {
				@Override
				public boolean hasSkill(Player player, String skill) {
					return heroesCharacterManager.getHero(player).hasAccessToSkill(skill);
				}
			};
		}
	}
	
	private static void setupQuestHandler(NPGuys plugin) {
		if(plugin.getServer().getPluginManager().isPluginEnabled("Quester")) {
			Plugin questerPlugin = plugin.getServer().getPluginManager().getPlugin("Quester");
			if(questerPlugin instanceof Quester) {
				questHandler = new QuesterHandler((Quester)questerPlugin);
				return;
			}
		}
	}

	private static void setupCitizens(NPGuys plugin) {
		citizens = (CitizensPlugin)plugin.getServer().getPluginManager().getPlugin("Citizens");
	}
	
	private static void setupEconomy(NPGuys plugin) {
		if(plugin.getServer().getPluginManager().isPluginEnabled("Vault")) {
			RegisteredServiceProvider<Economy> economyProvider = plugin.getServer()
					.getServicesManager().getRegistration(Economy.class);
	        if (economyProvider != null) {
	            economy = economyProvider.getProvider();
	        }
		}
	}
	
	private static void setupHeroes(NPGuys plugin) {
		if(plugin.getServer().getPluginManager().isPluginEnabled("Heroes")) {
			heroesCharacterManager = getHeroesCharacterManager();
		}
	}
	
	public static Action newAction(String name) throws ActionMissingException {
		name = name.toUpperCase();
		try {
			if(actions.containsKey(name)) {
				return actions.get(name).getConstructor(String.class).newInstance(name);
			}
			else {
				throw new ActionMissingException(name);
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void registerAction(String name, Class<? extends Action> clazz) {
		actions.put(name.toUpperCase(), clazz);
	}
	
	public static Requirement newRequirement(String name) throws RequirementMissingException {
		name = name.toUpperCase();
		try {
			if (requirements.containsKey(name)) {
				return requirements.get(name).getConstructor(String.class).newInstance(name);
			}
			else {
				throw new RequirementMissingException(name);
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void registerRequirement(String name, Class<? extends Requirement> clazz) {
		requirements.put(name.toUpperCase(), clazz);
	}
	
	public static Economy getEconomy() {
		return economy;
	}
	
	public static CitizensPlugin getCitizens() {
		return citizens;
	}
	
	public static QuestHandler getQuestHandler() {
		return questHandler;
	}
	
	public static CharacterManager getHeroesCharacterManager() {
		return heroesCharacterManager;
	}
	
	public static void setQuestHandler(QuestHandler questHandler) {
		ElementManager.questHandler = questHandler;
	}
	
	public static SkillHandler getSkillHandler() {
		return skillHandler;
	}
	
	public static void setSkillHander(SkillHandler skillHandler) {
		ElementManager.skillHandler = skillHandler;
	}
	
	public static String generateRequirementsList() {
		StringBuilder sb = new StringBuilder();
		sb.append("Requirements list:");
		for(String requirementType : requirements.keySet()) {
			sb.append("\n");
			sb.append(ChatColor.GOLD).append(requirementType).append(ChatColor.RESET);
			try {
				Requirement requirementInstance = newRequirement(requirementType);
				sb.append("\n").append(CommandUtils.PADDING);
				sb.append("Description: ").append(requirementInstance.getDescription());
				sb.append("\n").append(CommandUtils.PADDING);
				sb.append("Usage: ").append("/dialogue requirement add(r) ").append(requirementType)
					.append(" ").append(requirementInstance.getUsage());
			} catch (RequirementMissingException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
	
	public static String generatActionsList() {
		StringBuilder sb = new StringBuilder();
		sb.append("Actions list:");
		for(String actionType : actions.keySet()) {
			sb.append("\n");
			sb.append(ChatColor.GOLD).append(actionType).append(ChatColor.RESET);
			try {
				Action actionInstance = newAction(actionType);
				sb.append("\n").append(CommandUtils.PADDING);
				sb.append("Description: ").append(actionInstance.getDescription());
				sb.append("\n").append(CommandUtils.PADDING);
				sb.append("Usage: ").append("/dialogue action add ").append(actionType)
					.append(" ").append(actionInstance.getUsage());
			} catch (ActionMissingException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
	
	public static void registerUI(String name, Class<? extends ConversationUI> clazz) {
		uiTypes.put(name.toUpperCase(), clazz);
		ConfigurationSection uiConfig = NPGuys.getPlugin().getConfig().getConfigurationSection("ui.configs."+name.toLowerCase());
		try {
			newUI(name, null).init(uiConfig);
		} catch (UIMissingException e) {
			e.printStackTrace();
		} catch (UIInitializationFailedException e) {
			NPGuys.getPlugin().getLogger()
			.log(Level.WARNING, "Failed to initialize "+name+" UI: "+e.getMessage());
		}
	}
	
	public static ConversationUI newUI(Class<? extends ConversationUI> clazz, Conversation conversation) {
		try {
			return clazz.getConstructor(Conversation.class).newInstance(conversation);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		// TODO FailedToCreateUIException or similar
		return null;
	}
	
	public static ConversationUI newUI(String uiName, Conversation conversation) throws UIMissingException {
		if (uiTypes.containsKey(uiName)) {
			return newUI(uiTypes.get(uiName), conversation);
		} else {
			throw new UIMissingException(uiName);
		}
	}
	
	public static ConversationUI newUI(Conversation conversation) throws UIMissingException {
		return newUI(defaultUI, conversation);
	}
	
	public static interface SkillHandler {
		public boolean hasSkill(Player player, String skill);
	}
}
