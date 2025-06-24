package org.winlogon.asynccraftr;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class AsyncCraftr {
    private static final boolean IS_FOLIA;
    
    static {
        boolean foliaCheck;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            foliaCheck = true;
        } catch (ClassNotFoundException e) {
            foliaCheck = false;
        }
        IS_FOLIA = foliaCheck;
    }
    
    public static boolean isRunningOnFolia() {
        return IS_FOLIA;
    }

    // Global scheduler methods
    public static Task runGlobalTask(Plugin plugin, Runnable task) {
        if (IS_FOLIA) {
            return new FoliaTask(plugin.getServer().getGlobalRegionScheduler()
                .run(plugin, t -> task.run()));
        } else {
            return new BukkitTask(plugin.getServer().getScheduler()
                .runTask(plugin, task));
        }
    }
    
    public static Task runGlobalTaskLater(Plugin plugin, Runnable task, Duration delay) {
        long ticks = toTicks(delay);
        if (IS_FOLIA) {
            return new FoliaTask(plugin.getServer().getGlobalRegionScheduler()
                .runDelayed(plugin, t -> task.run(), ticks));
        } else {
            return new BukkitTask(plugin.getServer().getScheduler()
                .runTaskLater(plugin, task, ticks));
        }
    }
    
    public static Task runGlobalTaskTimer(Plugin plugin, Runnable task, Duration delay, Duration period) {
        long delayTicks = toTicks(delay);
        long periodTicks = toTicks(period);
        if (IS_FOLIA) {
            return new FoliaTask(plugin.getServer().getGlobalRegionScheduler()
                .runAtFixedRate(plugin, t -> task.run(), delayTicks, periodTicks));
        } else {
            return new BukkitTask(plugin.getServer().getScheduler()
                .runTaskTimer(plugin, task, delayTicks, periodTicks));
        }
    }

    // Entity scheduler methods
    public static Optional<Task> runEntityTask(Plugin plugin, Entity entity, Runnable task) {
        if (IS_FOLIA) {
            return Optional.of(new FoliaTask(entity.getScheduler()
                .run(plugin, t -> task.run(), null)));
        } else {
            return Optional.of(new BukkitTask(plugin.getServer().getScheduler()
                .runTask(plugin, task)));
        }
    }
    
    public static Optional<Task> runEntityTaskLater(Plugin plugin, Entity entity, Runnable task, Duration delay) {
        long ticks = toTicks(delay);
        if (IS_FOLIA) {
            return Optional.of(new FoliaTask(entity.getScheduler()
                .runDelayed(plugin, t -> task.run(), null, ticks)));
        } else {
            return Optional.of(new BukkitTask(plugin.getServer().getScheduler()
                .runTaskLater(plugin, task, ticks)));
        }
    }
    
    public static Optional<Task> runEntityTaskTimer(Plugin plugin, Entity entity, Runnable task, Duration delay, Duration period) {
        long delayTicks = toTicks(delay);
        long periodTicks = toTicks(period);
        if (IS_FOLIA) {
            return Optional.of(new FoliaTask(entity.getScheduler()
                .runAtFixedRate(plugin, t -> task.run(), null, delayTicks, periodTicks)));
        } else {
            return Optional.of(new BukkitTask(plugin.getServer().getScheduler()
                .runTaskTimer(plugin, task, delayTicks, periodTicks)));
        }
    }

    // Region scheduler methods
    public static Task runRegionTask(Plugin plugin, Location location, Runnable task) {
        if (IS_FOLIA) {
            return new FoliaTask(plugin.getServer().getRegionScheduler()
                .run(plugin, location, t -> task.run()));
        } else {
            return new BukkitTask(plugin.getServer().getScheduler()
                .runTask(plugin, task));
        }
    }
    
    public static Task runRegionTaskLater(Plugin plugin, Location location, Runnable task, Duration delay) {
        long ticks = toTicks(delay);
        if (IS_FOLIA) {
            return new FoliaTask(plugin.getServer().getRegionScheduler()
                .runDelayed(plugin, location, t -> task.run(), ticks));
        } else {
            return new BukkitTask(plugin.getServer().getScheduler()
                .runTaskLater(plugin, task, ticks));
        }
    }
    
    public static Task runRegionTaskTimer(Plugin plugin, Location location, Runnable task, Duration delay, Duration period) {
        long delayTicks = toTicks(delay);
        long periodTicks = toTicks(period);
        if (IS_FOLIA) {
            return new FoliaTask(plugin.getServer().getRegionScheduler()
                .runAtFixedRate(plugin, location, t -> task.run(), delayTicks, periodTicks));
        } else {
            return new BukkitTask(plugin.getServer().getScheduler()
                .runTaskTimer(plugin, task, delayTicks, periodTicks));
        }
    }

    // Async scheduler methods
    public static Task runAsyncTask(Plugin plugin, Runnable task) {
        if (IS_FOLIA) {
            return new FoliaTask(plugin.getServer().getAsyncScheduler()
                .runNow(plugin, t -> task.run()));
        } else {
            return new BukkitTask(plugin.getServer().getScheduler()
                .runTaskAsynchronously(plugin, task));
        }
    }
    
    public static Task runAsyncTaskLater(Plugin plugin, Runnable task, Duration delay) {
        if (IS_FOLIA) {
            return new FoliaTask(plugin.getServer().getAsyncScheduler()
                .runDelayed(plugin, t -> task.run(), delay.toNanos(), TimeUnit.NANOSECONDS));
        } else {
            return new BukkitTask(plugin.getServer().getScheduler()
                .runTaskLaterAsynchronously(plugin, task, toTicks(delay)));
        }
    }
    
    public static Task runAsyncTaskTimer(Plugin plugin, Runnable task, Duration delay, Duration period) {
        if (IS_FOLIA) {
            return new FoliaTask(plugin.getServer().getAsyncScheduler()
                .runAtFixedRate(plugin, t -> task.run(), delay.toNanos(), period.toNanos(), TimeUnit.NANOSECONDS));
        } else {
            long delayTicks = toTicks(delay);
            long periodTicks = toTicks(period);
            return new BukkitTask(plugin.getServer().getScheduler()
                .runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks));
        }
    }

    // CompletableFuture-based methods
    public static <T> CompletableFuture<T> runAsync(Plugin plugin, Callable<T> callable) {
        CompletableFuture<T> future = new CompletableFuture<>();
        runAsyncTask(plugin, () -> {
            try {
                future.complete(callable.call());
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }
    
    public static <T> CompletableFuture<T> runSync(Plugin plugin, Callable<T> callable) {
        CompletableFuture<T> future = new CompletableFuture<>();
        runGlobalTask(plugin, () -> {
            try {
                future.complete(callable.call());
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }
    
    public static <T> CompletableFuture<T> runSyncForEntity(Plugin plugin, Entity entity, Callable<T> callable) {
        CompletableFuture<T> future = new CompletableFuture<>();
        runEntityTask(plugin, entity, () -> {
            try {
                future.complete(callable.call());
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        }).ifPresentOrElse(
            task -> {}, 
            () -> future.completeExceptionally(new IllegalStateException("Failed to schedule entity task"))
        );
        return future;
    }
    
    public static <T> CompletableFuture<T> runSyncForLocation(Plugin plugin, Location location, Callable<T> callable) {
        CompletableFuture<T> future = new CompletableFuture<>();
        runRegionTask(plugin, location, () -> {
            try {
                future.complete(callable.call());
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    // Utility methods
    private static long toTicks(Duration duration) {
        return duration.toMillis() / 50;
    }
    
    public interface Task {
        void cancel();
        boolean isCancelled();
    }
    
    private static class BukkitTask implements Task {
        private final org.bukkit.scheduler.BukkitTask task;
        
        public BukkitTask(org.bukkit.scheduler.BukkitTask task) {
            this.task = task;
        }
        
        @Override
        public void cancel() {
            task.cancel();
        }
        
        @Override
        public boolean isCancelled() {
            return task.isCancelled();
        }
    }
    
    private static class FoliaTask implements Task {
        private final io.papermc.paper.threadedregions.scheduler.ScheduledTask task;
        
        public FoliaTask(io.papermc.paper.threadedregions.scheduler.ScheduledTask task) {
            this.task = task;
        }
        
        @Override
        public void cancel() {
            task.cancel();
        }
        
        @Override
        public boolean isCancelled() {
            return task.getCancelledState() == io.papermc.paper.threadedregions.scheduler.ScheduledTask.CancelledState.CANCELLED_BY_CALLER;
        }
    }
}
