package com.eloli.sodioncoreexample;

import com.eloli.sodioncore.config.ConfigureService;
import com.eloli.sodioncore.dependency.DependencyManager;
import com.eloli.sodioncore.file.BaseFileService;
import com.eloli.sodioncore.logger.AbstractLogger;
import com.eloli.sodioncore.orm.OrmService;
import com.eloli.sodioncore.orm.SodionEntity;
import com.eloli.sodioncore.orm.configure.DatabaseConfigure;
import com.eloli.sodioncoreexample.config.MainConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BukkitLoader extends JavaPlugin implements Listener {
    public static BukkitLoader instance;
    public BaseFileService fileService;
    public ConfigureService<MainConfiguration> configureService;
    public AbstractLogger logger;
    public DependencyManager dependencyManager;
    public OrmService ormService;

    @Override
    public void onEnable() {
        instance = this;
        fileService = new BaseFileService(getDataFolder().getPath());

        try {
            configureService = new ConfigureService<>(fileService,"config.json","com.eloli.sodioncoreexample.config",2);
        } catch (Exception exception) {
            getLogger().warning("Failed to load config.");
            exception.printStackTrace();
            getServer().shutdown();
        }

        logger = new AbstractLogger() {
            @Override
            public void info(String info) {
                getLogger().info(info);
            }

            @Override
            public void info(String info, Exception exception) {
                getLogger().info(info);
                exception.printStackTrace();
            }

            @Override
            public void warn(String info) {
                getLogger().warning(info);
            }

            @Override
            public void warn(String info, Exception exception) {
                getLogger().warning(info);
                exception.printStackTrace();
            }
        };

        Map<String,String> relocateMap = new HashMap<>();
        relocateMap.put("org.mindrot.jbcrypt","com.eloli.sodioncoreexample.libs.jbcrypt");
        dependencyManager = new DependencyManager(fileService,logger,relocateMap,"https://maven.aliyun.com/repository/central/");
        dependencyManager.checkDependencyMaven("org.mindrot:jbcrypt:0.4:com.eloli.sodioncoreexample.libs.jbcrypt.BCrypt");

        List<Class<? extends SodionEntity>> entities = new ArrayList<>();
        entities.add(User.class);

        try {
            ormService = new OrmService(
                    dependencyManager,
                    entities,
                    (DatabaseConfigure) MainConfiguration.class.getField(configureService.instance.database).get(configureService.instance));
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        getServer().getPluginManager().registerEvents(this,this);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        getServer().getScheduler().runTaskAsynchronously(this,()->{
            Session session = ormService.sessionFactory.openSession();
            session.beginTransaction();
            User user = session.get(User.class,e.getPlayer().getUniqueId());
            if(user == null){
                user = new User(e.getPlayer());
                session.save(user);
            }else if(!e.getPlayer().getName().equals(user.name)){
                session.update(user);
            }
            session.getTransaction().commit();
        });
    }
}
