package io.github.Egg101.IPManager;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import lib.PatPeter.SQLibrary.Database;
import lib.PatPeter.SQLibrary.MySQL;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class IPManager extends JavaPlugin implements Listener{
	Logger log;
	private Database mysql;
	FileConfiguration data = null;
	Plugin p;
	
    @Override
    public void onEnable(){
		log = this.getLogger();
		log.info("[IPManager] Enabled");
		
		getServer().getPluginManager().registerEvents(this, this);
    	
	    File file = new File(this.getDataFolder(), "config.yml");
	    if (!file.exists()) {
			log.info("[IPManager] Creating config.yml...");
	        this.saveDefaultConfig();
			log.info("[IPManager] Successfully created config.yml!");
	    }
	    
	    File dfile = new File(getDataFolder(), "data.yml");
	    this.data = YamlConfiguration.loadConfiguration(dfile);
	    if (!p.getDataFolder().exists()) {
	      p.getDataFolder().mkdir();
	    }
	    if (!dfile.exists()) {
	        this.log.info("[IPManager] Creating data.yml...");
	        p.saveResource("data.yml", true);
	        this.log.info("[IPManager] Successfully created data.yml!");
	    }
        sqlConnection();
    }
    @Override
    public void onDisable() {
    	
    }
    
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		Player player = (Player) sender;
		String playername = player.getName();
		Player[] list = getServer().getOnlinePlayers();
		
    	if(cmd.getName().equalsIgnoreCase("ipm")){
    		// --------
    		if (args.length == 2 && args[0].equalsIgnoreCase("lookupip")){
    			for (Player p : list){
    			    if (player.getAddress().toString().substring(1).split(":")[0] == args[1]){
    			    	sender.sendMessage(ChatColor.DARK_BLUE + "Players with IP " + args[1] + ":");
    			        sender.sendMessage(ChatColor.BLUE + p.getName());
    			    }
    			}
    		}

    		// --------
    		if (args.length == 2 && args[0].equalsIgnoreCase("lookupplayer")){
    			for (Player p : list){
    				if (p.getName() == args[1]) {
    					sender.sendMessage(p.getName() + " has IP: " + player.getAddress().toString().substring(1).split(":")[0]);
    				}
    			}
    		}
    		return true;
    	} // end /ipm

    	return false; 
    }
    
    
    public void sqlConnection() {
	    log.info("[IPManager] Connecting to MySQL...");
		String host = getConfig().getString("mysqlinfo.host");
		String port = getConfig().getString("mysqlinfo.port");
		String dbname = getConfig().getString("mysqlinfo.database");
		String user = getConfig().getString("mysqlinfo.username");
		String password = getConfig().getString("mysqlinfo.password");
		
		mysql = new MySQL(this.getLogger(),"[IPManager] ",host,Integer.parseInt(port),dbname,user,password);
		try {
			mysql.open();
		    log.info("[IPManager] Successfully connected to MySQL!");
		} catch (Exception e) {
			log.info("[IPManager] Could not connect to MySQL:");
			log.info(e.getMessage());
		}

	}

	public void sqlDoesDatabaseExist() {
        if(!(mysql.isTable("rewardtable"))){
    	    log.info("[IPManager] Creating MySQL table...");
        	try {
    	    	mysql.query("CREATE TABLE IPlist (playername VARCHAR(50), IP VARCHAR(20));");
        	    log.info("[IPManager] Successfully created MySQL table!");
        	} catch (Exception e) {
        		log.info("[IPManager] Could not create MySQL table:");
    			log.info(e.getMessage());
        	}
    	    	
        }
    }
	
				// ============================//
				//			  Events		   //
				// ============================//
	
	@EventHandler
    public void loginEvent(PlayerLoginEvent event) {
		ResultSet rs;
		String loginEventIP = event.getPlayer().getAddress().toString().substring(1).split(":")[0];
		
		try {
			rs = mysql.query("SELECT EXISTS(SELECT 1 FROM IPlist WHERE playername = "+event.getPlayer().getName()+");");
		} catch (SQLException e) {
            rs = null;
        	e.printStackTrace();
        }
		
		try {
			if (rs.getInt("EXISTS(SELECT 1 FROM IPlist WHERE playername = "+event.getPlayer().getName()+")") == 0) {
			    mysql.query("INSERT INTO IPlist VALUES ("+event.getPlayer().getName()+","+loginEventIP+");");
			}
		} catch (SQLException e) {
			rs = null;
			e.printStackTrace();
		}
    }
				// ============================//
				//			 Methods	 	   //
				// ============================//
	public String get_playername(String varSelect, String varWhere, String valWhere) {
		// Get string from MySQL
		ResultSet rs;
		try {
			rs = mysql.query("SELECT "+varSelect+" FROM corporations WHERE "+varWhere+"='"+valWhere+"';");
		} catch (SQLException e) {
            rs = null;
        	e.printStackTrace();
        }
		
		// Return string
        try {
            if (rs.first()) {
                try {
                    return rs.getString(varSelect);
                } catch (SQLException e) {
                    rs = null;
                	e.printStackTrace();
                }
            }
        } catch (SQLException e) {
       
            e.printStackTrace();
        }
        
        // If failed
        return null;
	}
	public String get_ip(String varSelect, String varWhere, String valWhere) {
		// Get string from MySQL
		ResultSet rs;
		try {
			rs = mysql.query("Select "+varSelect+" FROM corporations WHERE "+varWhere+"='"+valWhere+"';");
		} catch (SQLException e) {
            rs = null;
        	e.printStackTrace();
        }
		
		// Return string
        try {
            if (rs.first()) {
                try {
                    return rs.getInt(varSelect);
                } catch (SQLException e) {
                    rs = null;
                	e.printStackTrace();
                }
            }
        } catch (SQLException e) {
       
            e.printStackTrace();
        }
        
        // If failed
        return 0;
	}
	
}
