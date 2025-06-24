package org.winlogon.asynccraftr;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Library for abstracting away server software checks for 
 * both Folia and standard Bukkit scheduling to run asynchronous tasks.
 */
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


    /**
     * Checks if the plugin is running on Folia.
     *
     * @return {@code true} if the server is running on Folia, {@code false} otherwise.
     */
    public static boolean isRunningOnFolia() {
        return IS_FOLIA;
    }

    /**
     * Runs a synchronous task on the global scheduler (main thread).
     *
     * @param plugin The plugin scheduling the task.
     * @param task The task to execute.
     * @return A {@link Task} representing the scheduled task.
     */
    public static Task runGlobalTask(Plugin plugin, Runnable task) {
        if (IS_FOLIA) {
            return new FoliaTask(plugin.getServer().getGlobalRegionScheduler()
                .run(plugin, t -> task.run()));
        } else {
            return new BukkitTask(plugin.getServer().getScheduler()
                .runTask(plugin, task));
        }
    }

    /**
      * Runs a synchronous task on the global scheduler after a delay.
      *
      * @param plugin The plugin scheduling the task.
      * @param task The task to execute.
      * @param delay The delay before the task is executed.
      * @return A {@link Task} representing the scheduled task.
      */
    public static Task runGlobalTaskLater(Plugin plugin, Runnable task, Duration delay) {
        long ticks = durationToTicks(delay);
        if (IS_FOLIA) {
            return new FoliaTask(plugin.getServer().getGlobalRegionScheduler()
                .runDelayed(plugin, t -> task.run(), ticks));
        } else {
            return new BukkitTask(plugin.getServer().getScheduler()
                .runTaskLater(plugin, task, ticks));
        }
    }

    /**
     * Runs a synchronous repeating task on the global scheduler.
     *
     * @param plugin The plugin scheduling the task.
     * @param task The task to execute.
     * @param delay The initial delay before the task is first run.
     * @param period The interval between successive executions.
     * @return A {@link Task} representing the scheduled task.
     */
    public static Task runGlobalTaskTimer(Plugin plugin, Runnable task, Duration delay, Duration period) {
        long delayTicks = durationToTicks(delay);
        long periodTicks = durationToTicks(period);
        if (IS_FOLIA) {
            return new FoliaTask(plugin.getServer().getGlobalRegionScheduler()
                .runAtFixedRate(plugin, t -> task.run(), delayTicks, periodTicks));
        } else {
            return new BukkitTask(plugin.getServer().getScheduler()
                .runTaskTimer(plugin, task, delayTicks, periodTicks));
        }
    }

    /**
     * Runs a synchronous task for a specific entity.
     *
     * @param plugin The plugin scheduling the task.
     * @param entity The entity context for the task.
     * @param task The task to execute.
     * @return An {@link Optional} containing the {@link Task}, or empty if not supported.
     */
    public static Optional<Task> runEntityTask(Plugin plugin, Entity entity, Runnable task) {
        if (IS_FOLIA) {
            return Optional.of(new FoliaTask(entity.getScheduler()
                .run(plugin, t -> task.run(), null)));
        } else {
            return Optional.of(new BukkitTask(plugin.getServer().getScheduler()
                .runTask(plugin, task)));
        }
    }

    /**
     * Runs a synchronous task for a specific entity after a delay.
     *
     * @param plugin The plugin scheduling the task.
     * @param entity The entity context for the task.
     * @param task The task to execute.
     * @param delay The delay before the task is executed.
     * @return An {@link Optional} containing the {@link Task}, or empty if not supported.
     */
    public static Optional<Task> runEntityTaskLater(Plugin plugin, Entity entity, Runnable task, Duration delay) {
        long ticks = durationToTicks(delay);
        if (IS_FOLIA) {
            return Optional.of(new FoliaTask(entity.getScheduler()
                .runDelayed(plugin, t -> task.run(), null, ticks)));
        } else {
            return Optional.of(new BukkitTask(plugin.getServer().getScheduler()
                .runTaskLater(plugin, task, ticks)));
        }
    }

    /**
     * Runs a synchronous repeating task for a specific entity.
     *
     * @param plugin The plugin scheduling the task.
     * @param entity The entity context for the task.
     * @param task The task to execute.
     * @param delay The initial delay before the first execution.
     * @param period The interval between executions.
     * @return An {@link Optional} containing the {@link Task}, or empty if not supported.
     */
    public static Optional<Task> runEntityTaskTimer(Plugin plugin, Entity entity, Runnable task, Duration delay, Duration period) {
        long delayTicks = durationToTicks(delay);
        long periodTicks = durationToTicks(period);
        if (IS_FOLIA) {
            return Optional.of(new FoliaTask(entity.getScheduler()
                .runAtFixedRate(plugin, t -> task.run(), null, delayTicks, periodTicks)));
        } else {
            return Optional.of(new BukkitTask(plugin.getServer().getScheduler()
                .runTaskTimer(plugin, task, delayTicks, periodTicks)));
        }
    }

    /**
     * Runs a synchronous task for a specific region based on location.
     *
     * @param plugin The plugin scheduling the task.
     * @param location The location used to determine the region.
     * @param task The task to execute.
     * @return A {@link Task} representing the scheduled task.
     */
    public static Task runRegionTask(Plugin plugin, Location location, Runnable task) {
        if (IS_FOLIA) {
            return new FoliaTask(
                 plugin.getServer()
                    .getRegionScheduler()
                    .run(plugin, location, t -> task.run())
            );
        } else {
            return new BukkitTask(
                plugin.getServer()
                    .getScheduler()
                    .runTask(plugin, task)
            );
        }
    }

    /**
     * Runs a synchronous task for a specific region after a delay.
     *
     * @param plugin The plugin scheduling the task.
     * @param location The location used to determine the region.
     * @param task The task to execute.
     * @param delay The delay before the task is executed.
     * @return A {@link Task} representing the scheduled task.
     */
    public static Task runRegionTaskLater(Plugin plugin, Location location, Runnable task, Duration delay) {
        var ticks = durationToTicks(delay);
        if (IS_FOLIA) {
            return new FoliaTask(plugin.getServer().getRegionScheduler()
                .runDelayed(plugin, location, t -> task.run(), ticks));
        } else {
            return new BukkitTask(plugin.getServer().getScheduler()
                .runTaskLater(plugin, task, ticks));
        }
    }

    
    /**
     * Runs a synchronous repeating task for a specific region.
     *
     * @param plugin The plugin scheduling the task.
     * @param location The location used to determine the region.
     * @param task The task to execute.
     * @param delay The initial delay before the first execution.
     * @param period The interval between executions.
     * @return A {@link Task} representing the scheduled task.
     */
    public static Task runRegionTaskTimer(Plugin plugin, Location location, Runnable task, Duration delay, Duration period) {
        var delayTicks = durationToTicks(delay);
        var periodTicks = durationToTicks(period);
        if (IS_FOLIA) {
            return new FoliaTask(plugin.getServer().getRegionScheduler()
                .runAtFixedRate(plugin, location, t -> task.run(), delayTicks, periodTicks));
        } else {
            return new BukkitTask(plugin.getServer().getScheduler()
                .runTaskTimer(plugin, task, delayTicks, periodTicks));
        }
    }

    /**
     * Runs a task asynchronously.
     *
     * @param plugin The plugin scheduling the task.
     * @param task The task to execute.
     * @return A {@link Task} representing the scheduled asynchronous task.
     */
    public static Task runAsyncTask(Plugin plugin, Runnable task) {
        if (IS_FOLIA) {
            return new FoliaTask(plugin.getServer().getAsyncScheduler()
                .runNow(plugin, t -> task.run()));
        } else {
            return new BukkitTask(plugin.getServer().getScheduler()
                .runTaskAsynchronously(plugin, task));
        }
    }

    /**
     * Runs a task asynchronously after a specified delay.
     *
     * @param plugin The plugin scheduling the task.
     * @param task The task to execute.
     * @param delay The delay before the task is executed.
     * @return A {@link Task} representing the scheduled task.
     */
    public static Task runAsyncTaskLater(Plugin plugin, Runnable task, Duration delay) {
        if (IS_FOLIA) {
            return new FoliaTask(plugin.getServer().getAsyncScheduler()
                .runDelayed(plugin, t -> task.run(), delay.toNanos(), TimeUnit.NANOSECONDS));
        } else {
            return new BukkitTask(plugin.getServer().getScheduler()
                .runTaskLaterAsynchronously(plugin, task, durationToTicks(delay)));
        }
    }

    /**
     * Runs a repeating task asynchronously.
     *
     * @param plugin The plugin scheduling the task.
     * @param task The task to execute.
     * @param delay The initial delay before the first execution.
     * @param period The interval between executions.
     * @return A {@link Task} representing the scheduled asynchronous task.
     */
    public static Task runAsyncTaskTimer(Plugin plugin, Runnable task, Duration delay, Duration period) {
        if (IS_FOLIA) {
            return new FoliaTask(plugin.getServer().getAsyncScheduler()
                .runAtFixedRate(plugin, t -> task.run(), delay.toNanos(), period.toNanos(), TimeUnit.NANOSECONDS));
        } else {
            var delayTicks = durationToTicks(delay);
            var periodTicks = durationToTicks(period);
            return new BukkitTask(plugin.getServer().getScheduler()
                .runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks));
        }
    }

    /**
     * Executes a {@link Callable} asynchronously and returns a {@link CompletableFuture} with the result.
     *
     * @param plugin The plugin scheduling the task.
     * @param callable The callable to execute asynchronously.
     * @param <T> The type of the result.
     * @return A {@link CompletableFuture} that completes with the callable's result.
     */
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

    /**
     * Runs a {@link Supplier} asynchronously, where the supplier returns a {@link CompletableFuture}.
     * Useful for chaining or async workflows.
     *
     * @param plugin The plugin scheduling the task.
     * @param supplier The supplier that returns a future.
     * @param <T> The result type.
     * @return A {@link CompletableFuture<Void>} that completes when the inner future completes.
     */
    public static <T> CompletableFuture<Void> runAsync(Plugin plugin, Supplier<CompletableFuture<T>> supplier) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        // schedule the supplier to run asynchronously
        runAsyncTask(plugin, () -> {
            CompletableFuture<T> inner;
            try {
                inner = supplier.get();
            } catch (Exception e) {
                // supplier threw immediately
                result.completeExceptionally(e);
                return;
            }
            // when inner completes, propagate to result (as Void)
            inner.whenComplete((value, ex) -> {
                if (ex != null) result.completeExceptionally(ex);
                else result.complete(null);
            });
        });
        return result;
    }

    /**
     * Runs a {@link Callable} on the main thread and returns a {@link CompletableFuture} with the result.
     *
     * @param plugin The plugin scheduling the task.
     * @param callable The callable to execute synchronously.
     * @param <T> The result type.
     * @return A {@link CompletableFuture} that completes with the result.
     */
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

    /**
     * Runs a {@link Supplier} on the main thread, where the supplier returns a {@link CompletableFuture}.
     *
     * @param plugin The plugin scheduling the task.
     * @param supplier The supplier that returns a future.
     * @param <T> The result type.
     * @return A {@link CompletableFuture<Void>} that completes when the inner future completes.
     */
    public static <T> CompletableFuture<Void> runSync(Plugin plugin, Supplier<CompletableFuture<T>> supplier) {
        var result = new CompletableFuture<Void>();
        // schedule the supplier to run on the global (sync) scheduler
        runGlobalTask(plugin, () -> {
            CompletableFuture<T> inner;
            try {
                inner = supplier.get();
            } catch (Exception e) {
                // supplier threw immediately
                result.completeExceptionally(e);
                return;
            }
            // when inner completes, propagate to result (as Void)
            inner.whenComplete((value, ex) -> {
                if (ex != null) result.completeExceptionally(ex);
                else result.complete(null);
            });
        });
        return result;
    }

    /**
     * Runs a {@link Callable} on the main thread for a specific entity context.
     *
     * @param plugin The plugin scheduling the task.
     * @param entity The entity context to run the task for.
     * @param callable The callable to execute.
     * @param <T> The result type.
     * @return A {@link CompletableFuture} that completes with the result.
     */
    public static <T> CompletableFuture<T> runSyncForEntity(Plugin plugin, Entity entity, Callable<T> callable) {
        var future = new CompletableFuture<T>();
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

    /**
     * Runs a {@link Callable} on the main thread for a specific location (region context).
     *
     * @param plugin The plugin scheduling the task.
     * @param location The region location to run the task for.
     * @param callable The callable to execute.
     * @param <T> The result type.
     * @return A {@link CompletableFuture} that completes with the result.
     */
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

    // Converts a java.time.Duration into Minecraft ticks
    private static long durationToTicks(Duration duration) {
        return duration.toMillis() / 50;
    }

    /**
     * Represents a handle to a scheduled task, allowing for cancellation and status checking.
     * This abstraction allows compatibility between Bukkit and Folia task implementations.
     */
    public interface Task {
    
        /**
         * Cancels the task if it has not yet run or is repeating.
         */
        void cancel();
    
        /**
         * Checks if the task has already been cancelled.
         *
         * @return {@code true} if the task is cancelled, {@code false} otherwise.
         */
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
        private final ScheduledTask task;

        public FoliaTask(ScheduledTask task) {
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
}
