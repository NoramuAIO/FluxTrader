package noramu.fluxtrader.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FluxTraderTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // First argument - subcommands
            List<String> subcommands = Arrays.asList(
                    "gui",
                    "info",
                    "next",
                    "reload"
            );
            
            String input = args[0].toLowerCase();
            for (String subcommand : subcommands) {
                if (subcommand.startsWith(input)) {
                    completions.add(subcommand);
                }
            }
        }

        return completions;
    }
}
