package net.sothatsit.blockstore;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;

import net.sothatsit.blockstore.chunkstore.ChunkLoc;
import net.sothatsit.blockstore.chunkstore.ChunkManager;

/**
 * Main plugin class.
 */
public class BlockStore extends JavaPlugin implements Listener {

    private static BlockStore instance;
    private final Map<String, ChunkManager> managers = new ConcurrentHashMap<>();
    private final BlockStoreConfig blockStoreConfig = new BlockStoreConfig();

    @Override
    public void onEnable() {
        instance = this;

        blockStoreConfig.reload();

        Bukkit.getPluginManager().registerEvents(this, this);

        getCommand("blockstore").setExecutor(new BlockStoreCommand());

        if (Bukkit.getPluginManager().getPlugin("WorldEdit") != null) {
            Logger logger = getLogger();

            try {
                WorldEditHook hook = new WorldEditHook();

                hook.register();

                logger.info("Hooked WorldEdit.");
            } catch (Throwable e) {
                logger.severe("");
                logger.severe("     [!] Hooking WorldEdit has Failed [!] ");
                logger.severe("An error has been thrown hooking WorldEdit. This is");
                logger.severe("likely due to an outdated WorldEdit plugin. If you");
                logger.severe("wish for WorldEdit to clear the placed block stores");
                logger.severe("then please update WorldEdit to the latest version.");
                logger.severe("");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDisable() {
        for (ChunkManager manager : managers.values()) {
            manager.saveAll();
            manager.saveNames();
        }

        instance = null;
    }

    public BlockStoreConfig getBlockStoreConfig() {
        return blockStoreConfig;
    }

    public ChunkManager getManager(String worldName) {
        return managers.computeIfAbsent(worldName, key -> new ChunkManager(Bukkit.getWorld(key)));
    }

    public ChunkManager getManager(Location location) {
        return getManager(location.getWorld());
    }

    public ChunkManager getManager(World world) {
        return managers.computeIfAbsent(world.getName(), key -> new ChunkManager(world));
    }

    public Map<String, ChunkManager> getChunkManagers() {
        return managers;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockGrow(BlockGrowEvent event) {
        BlockStoreApi.setPlaced(event.getBlock(), false);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        BlockStoreApi.setPlaced(event.getBlock(), false);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        BlockStoreApi.setPlaced(event.getBlock(), true);
    }

    private BlockFace getPistonDirection(Block block) {
        if (block.getBlockData() instanceof Directional) {
            Directional directional = (Directional) block.getBlockData();
            return directional.getFacing();
        }
        return BlockFace.SELF;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        BlockFace direction = getPistonDirection(event.getBlock());

        Map<World, Set<Block>> byWorld = new HashMap<>();

        event.getBlocks().forEach(block -> {
            Set<Block> blockSet = byWorld.computeIfAbsent(block.getWorld(), world -> new HashSet<>());

            blockSet.add(block);
        });

        byWorld.forEach((world, blocks) -> {
            ChunkManager manager = getManager(world);

            manager.moveBlocksAsync(blocks, direction);
        });

        Block pistonArm = event.getBlock().getRelative(direction);
        boolean pistonPlaced = BlockStoreApi.isPlaced(event.getBlock());

        BlockStoreApi.setPlaced(pistonArm, pistonPlaced);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        BlockFace direction = getPistonDirection(event.getBlock());

        if (!event.isSticky() || event.getBlocks().size() == 0) {
            Block pistonArm = event.getBlock().getRelative(direction);

            BlockStoreApi.setPlaced(pistonArm, false);
            return;
        }

        Map<World, Set<Block>> byWorld = new HashMap<>();

        event.getBlocks().forEach(block -> {
            Set<Block> blockSet = byWorld.computeIfAbsent(block.getWorld(), world -> new HashSet<>());

            blockSet.add(block);
        });

        byWorld.forEach((world, blocks) -> {
            ChunkManager manager = getManager(world);

            manager.moveBlocksAsync(blocks, direction.getOppositeFace());
        });
    }

    @EventHandler
    public void onChunkLoad(final ChunkLoadEvent event) {
        if (blockStoreConfig.getPreloadStrategy() == PreloadStrategy.ALL) {
            getManager(event.getWorld()).preloadChunk(event.getChunk());
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (blockStoreConfig.getPreloadStrategy() == PreloadStrategy.CLOSE) {
            ChunkLoc before = ChunkLoc.fromLocation(event.getFrom());
            ChunkLoc after = ChunkLoc.fromLocation(event.getTo());

            if (!before.equals(after)) {
                getManager(event.getTo().getWorld()).preloadStoresAround(after);
            }
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (blockStoreConfig.getPreloadStrategy() == PreloadStrategy.CLOSE) {
            ChunkLoc before = ChunkLoc.fromLocation(event.getFrom());
            ChunkLoc after = ChunkLoc.fromLocation(event.getTo());

            if (!before.equals(after)) {
                getManager(event.getTo().getWorld()).preloadStoresAround(after);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (blockStoreConfig.getPreloadStrategy() == PreloadStrategy.CLOSE) {
            Player player = event.getPlayer();

            ChunkLoc chunkLoc = ChunkLoc.fromLocation(player.getLocation());

            ChunkManager manager = getManager(player.getWorld());

            manager.preloadStoresAround(chunkLoc);
        }
    }

    public static BlockStore getInstance() {
        return instance;
    }

}