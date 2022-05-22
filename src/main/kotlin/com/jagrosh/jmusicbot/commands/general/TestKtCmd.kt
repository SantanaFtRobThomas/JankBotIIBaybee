package com.jagrosh.jmusicbot.commands.general

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

class TestKtCmd() : Command() {
    init {
        this.name = "testkt"
        this.help = "test kotlin"
        this.arguments = "<text>"
        this.guildOnly = true
        print("test kotlin cmd")
    }

    override fun execute(event: CommandEvent) {
        val args = event.args
        event.reply("test kotlin: ${args}")
    }
}