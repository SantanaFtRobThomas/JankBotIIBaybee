package com.jagrosh.jmusicbot.commands.general

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import com.jagrosh.jmusicbot.commands.AdminCommand
import com.jagrosh.jmusicbot.commands.OwnerCommand
import com.jagrosh.jmusicbot.commands.TantamodCommand
import java.util.function.Consumer

class HelpManager {
    private var commands: List<Command> = mutableListOf()
    private var categories = mutableMapOf<String, MutableList<Command>>()

    fun showHelp(event: CommandEvent) {
        if(event.args != "") {
            val cmds = event.client.commands.filter {
                it.name.equals(event.args, true) || it.aliases.contains(event.args) }
            if(cmds.isNotEmpty()) {
                if(cmds[0] is TantamodCommand){
                    if(cmds[0].isHidden and (736622853797052519L !in event.member.roles.map { it.idLong })) {
                        event.reply("That command is hidden. Naughty naughty.")
                        return
                    }
                }
                event.reply(cmds[0].help)
                return
            } else {
                event.reply("I couldn't find that command! Jank it up!")
                return
            }
        } else {
            var reply = ""
            val is_mod = 736622853797052519L in event.member.roles.map { it.idLong }
            this.categories.forEach { cat ->
                var cat_str = "__**${cat.key}**__\n"
                for (cmd in cat.value) {
                    if(cmd is AdminCommand && !is_mod)
                        continue
                    if(cmd is TantamodCommand && !is_mod)
                        continue
                    cat_str += "`${cmd.name}` - ${cmd.help}\n\n"
                }
                reply += cat_str + "\n"
            }
            if(reply.isNotEmpty()){
                event.replyInDm(reply, { _ -> event.message.addReaction("✅").queue()},
                    {_ -> event.replyWarning("Help cannot be sent because you are blocking Direct Messages.")})
            }
        }
    }

    fun setCommands(commands: List<Command>) {
        this.commands = commands
        commands.forEach {
            val category: String = it.category?.name ?: "General"
            if (!categories.containsKey(category))
                categories[category] = mutableListOf()
            categories[category]!!.add(it)
        }
        categories = categories.toSortedMap()

        categories.forEach { cat ->
            cat.value.sortBy { it.name }
        }
    }

}
