package com.eloli.sodioncoreexample;

import com.eloli.sodioncore.orm.SodionEntity;
import com.sun.istack.NotNull;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "uusseerrss") //remove it to use "user" as table name
public class User extends SodionEntity {
    @NotNull
    public String name;

    @Id
    @Column(name = "usss")
    public UUID uuid;

    @Transient
    public Player player;

    public User() {
        // MUST have noArgs constructor
    }

    public User(Player player) {
        this.player = player;
        this.name = player.getName();
        this.uuid = player.getUniqueId();
    }

    public Player getPlayer() {
        if (player == null) {
            return Bukkit.getPlayer(uuid);
        }
        return player;
    }
}
