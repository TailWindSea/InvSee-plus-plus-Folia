package com.janboerman.invsee.spigot.api.logging;

import com.janboerman.invsee.spigot.api.target.Target;
import com.janboerman.invsee.utils.Pair;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formattable;
import java.util.FormattableFlags;
import java.util.Formatter;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Collectors;

public interface LogOutput {

    public static LogOutput make(Plugin plugin, UUID spectatorId, String spectatorName, Target target, LogOptions logOptions) {
        if (LogOptions.isEmpty(logOptions)) return NoOutput.INSTANCE;

        return LogOutputImpl.of(plugin, spectatorId, spectatorName, target, logOptions);
    }

    public void log(Difference difference);

}

class NoOutput implements LogOutput {

    static final NoOutput INSTANCE = new NoOutput();

    private NoOutput() {}

    @Override
    public void log(Difference difference) {
        // no-op
    }
}

class LogOutputImpl implements LogOutput {

    private static final String LOG_FOLDER_NAME = "InvSee++ logs";

    private static final String FORMAT =
        "\n[%1$tF %1$tT] [%2$-7s]" +
        "\nSpectator UUID: %3s" +
        "\nSpectator Name: %4s" +
        "\nTaken:          %5s" +
        "\nGiven:          %6s" +
        "\nTarget:         %7s";
    private static final String FORMAT_WITHOUT_SPECTATOR =
        "\n[%1$tF %1$tT] [%2$-7s]" +
        "\nTaken:          %5s" +
        "\nGiven:          %6s" +
        "\nTarget:         %7s";

    private final UUID spectatorId;
    private final String spectatorName;
    private final Logger logger;
    private final Target target;

    LogOutputImpl(Plugin plugin, UUID spectatorId, String spectatorName, Target targetPlayer, Set<LogTarget> logTargets) {
        this.spectatorId = spectatorId;
        this.spectatorName = spectatorName;
        this.target = targetPlayer;
        this.logger = Logger.getLogger("InvSee++." + spectatorId);
        this.logger.setLevel(Level.ALL);

        for (LogTarget target : logTargets) {
            switch (target) {
                case SERVER_LOG_FILE:
                    logger.setParent(plugin.getLogger());
                    break;
                case PLUGIN_LOG_FILE:
                    try {
                        File file = new File(new File(plugin.getDataFolder(), LOG_FOLDER_NAME), "_global.log");
                        FileHandler fileHandler = new FileHandler(file.getAbsolutePath());
                        fileHandler.setLevel(Level.ALL);
                        fileHandler.setFormatter(new SimpleFormatter() {
                            @Override
                            public String format(LogRecord record) {
                                Object[] parameters = record.getParameters();
                                Date time = new Date(record.getMillis());
                                Level level = record.getLevel();
                                Action action = (Action) parameters[0];
                                return LogOutputImpl.this.format(FORMAT, time, level, action.outcome);
                            }
                        });
                        logger.addHandler(fileHandler);
                    } catch (IOException e) {
                        plugin.getLogger().log(Level.SEVERE, "Could not create new file handler", e);
                    }
                    break;
                case SPECTATOR_LOG_FILE:
                    try {
                        File file = new File(new File(plugin.getDataFolder(), LOG_FOLDER_NAME), spectatorId + ".log");
                        FileHandler fileHandler = new FileHandler(file.getAbsolutePath());
                        fileHandler.setLevel(Level.ALL);
                        fileHandler.setFormatter(new SimpleFormatter() {
                            @Override
                            public String format(LogRecord record) {
                                Object[] parameters = record.getParameters();
                                Date time = new Date(record.getMillis());
                                Level level = record.getLevel();
                                Action action = (Action) parameters[0];
                                return LogOutputImpl.this.format(FORMAT_WITHOUT_SPECTATOR, time, level, action.outcome);
                            }
                        });
                        logger.addHandler(fileHandler);
                    } catch (IOException e) {
                        plugin.getLogger().log(Level.SEVERE, "Could not create new file handler", e);
                    }
                    break;
                case CONSOLE:
                    if (logTargets.contains(LogTarget.SERVER_LOG_FILE)) break; //server logger already outputs to console

                    ConsoleHandler consoleHandler = new ConsoleHandler();
                    consoleHandler.setLevel(Level.ALL);
                    consoleHandler.setFormatter(new SimpleFormatter() {
                        @Override
                        public String format(LogRecord record) {
                            Object[] parameters = record.getParameters();
                            Date time = new Date(record.getMillis());
                            Level level = record.getLevel();
                            Action action = (Action) parameters[0];
                            return LogOutputImpl.this.format(FORMAT, time, level, action.outcome);
                        }
                    });
                    logger.addHandler(consoleHandler);
                    break;
            }
        }

        if (!logTargets.contains(LogTarget.SERVER_LOG_FILE)) {
            logger.setUseParentHandlers(false);
        }
    }

    static LogOutputImpl of(Plugin plugin, UUID spectatorId, String spectatorName, Target target, LogOptions options) {
        return new LogOutputImpl(plugin, spectatorId, spectatorName, target, options.getTargets());
    }

    private final String format(String format, Date date, Level level, Difference difference) {
        assert format == FORMAT || format == FORMAT_WITHOUT_SPECTATOR : "invalid format";
        return String.format(format, date, level.getLocalizedName(), spectatorId, spectatorName, Taken.from(difference), Given.from(difference), target);
    }

    @Override
    public void log(Difference difference) {
        logger.log(Level.INFO, "%s", new Action(spectatorId, spectatorName, target, difference));
    }

    private static class DiffFormattable implements Formattable {

        private final List<Pair<ItemType, Integer>> items;

        protected DiffFormattable(List<Pair<ItemType, Integer>> items) {
            this.items = Objects.requireNonNull(items);
        }

        @Override
        public void formatTo(Formatter formatter, int flags, int width, int precision) {
            StringBuilder sb = new StringBuilder();

            String out = items.stream()
                    .map(pair -> {
                        ItemType type = pair.getFirst();
                        Material material = type.getMaterial();
                        ItemMeta meta = type.getItemMeta();
                        int amount = pair.getSecond();

                        if (meta == null) {
                            return material.name() + " x " + amount;
                        } else {
                            return material.name() + " & " + meta + " x " + amount;
                        }
                    })
                    .collect(Collectors.joining(", "));

            if (precision == -1 || out.length() < precision) {
                sb.append(out);
            } else {
                sb.append(out, 0, precision - 4).append("...]");
            }

            int len = sb.length();
            if (len < width)
                for (int i = 0; i < width - len; i++)
                    if ((flags | FormattableFlags.LEFT_JUSTIFY) == FormattableFlags.LEFT_JUSTIFY)
                        sb.append(' ');
                    else
                        sb.insert(0, ' ');

            formatter.format(sb.toString());
        }

        @Override
        public String toString() {
            return items.toString();
        }
    }

    static class Given extends DiffFormattable {

        private Given(List<Pair<ItemType, Integer>> items) {
            super(items);
        }

        static Given from(Difference difference) {
            var diff = difference.getDifference();
            List<Pair<ItemType, Integer>> items = new ArrayList<>(diff.size());
            for (var entry : diff.entrySet()) {
                items.add(new Pair<>(entry.getKey(), entry.getValue()));
            }
            return new Given(items);
        }

        @Override
        public String toString() {
            return "Given(" + super.toString() + ")";
        }
    }

    static class Taken extends DiffFormattable {

        private Taken(List<Pair<ItemType, Integer>> items) {
            super(items);
        }

        static Taken from(Difference difference) {
            var diff = difference.getDifference();
            List<Pair<ItemType, Integer>> items = new ArrayList<>(diff.size());
            for (var entry : diff.entrySet()) {
                items.add(new Pair<>(entry.getKey(), -1 * entry.getValue()));
            }
            return new Taken(items);
        }

        @Override
        public String toString() {
            return "Taken(" + super.toString() + ")";
        }

    }

}