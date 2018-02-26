import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.*;
import sx.blah.discord.handle.impl.events.*;
import sx.blah.discord.handle.impl.events.guild.GuildCreateEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelEvent;
import sx.blah.discord.handle.obj.*;
import util.Config;
import util.Message;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Listener class for events
 */
public class Listener
{

    private IDiscordClient client;

    private static String guildCfg = "";

    private String owner;

    private Map<IGuild, CommandHandler> guildMap;

    private Config cfg;

    /**
     * Constructor for the listener
     * @param client IDiscordClient for doing things
     */
    public Listener ( IDiscordClient client, String owner, Config cfg )
    {
        Scanner scn;

        this.client = client;

        this.owner = owner;

        guildMap = new HashMap<>();
    }

    /**
     * Handles messages
     */
    @EventSubscriber
    public void onMessageReceivedEvent( MessageReceivedEvent event )
    {
        String prefix, messageStr;

        IGuild guild = event.getGuild();

        IMessage msg = event.getMessage();

        messageStr = msg.getContent();

        CommandHandler cmd = guildMap.get( guild );

        prefix = cmd.prefix;

        if( messageStr.startsWith( prefix ) )
        {
            cmd.run( messageStr.substring( prefix.length() ), msg );
        }
    }

    /**
     * handles startup
     */
    @EventSubscriber
    public void onReadyEvent( ReadyEvent event ) // This method is called when the ReadyEvent is dispatched
    {

        // TODO: 2/22/2018 Test .online to see if it is server based or not
        //client.online("Type +join to queue in proper channel");
        //foo(); // Will be called!

        System.out.println( "started successfully" );
    }

    /**
     * also handles some startup
     */
    @EventSubscriber
    public void onGuildJoin( GuildCreateEvent event )
    {
        String prefix;

        CommandHandler cmd;

        IGuild guild = event.getGuild();

        cfg = new Config( "GuildConfigs/", guild.getStringID() + ".properties");

        prefix = cfg.getProp( "prefix" );

        if( prefix == null )
        {
            prefix = "?";

            cfg.setProp( "prefix", prefix );

            cmd = new CommandHandler(prefix, guild, client, cfg, owner );

            guildMap.put( guild, cmd );

            newGuild( guild );
        }
        else
        {
            cmd = new CommandHandler(prefix, guild, client, cfg, owner );

            guildMap.put( guild, cmd );
        }

    }

    /**
     * Handles users joining a channel, mostly for sorting teams
     * @param event user join event
     */
    @EventSubscriber
    public void userJoinChannel( UserVoiceChannelEvent event )
    {
        CommandHandler cmd = guildMap.get(event.getGuild());

        cmd.channelJoin( event );
    }

    /**
     * Figures out what to do on join of a new server
     * @param guild new guild to be setup
     */
    private void newGuild( IGuild guild )
    {
        IChannel defaultChannel = guild.getDefaultChannel();

        String newMessage = "Hiya! By default the command prefix is ?, you can set that with ?setup prefix (prefix), or find out what i can do with ?help";

        Message.builder( client, defaultChannel, newMessage );
    }

}
