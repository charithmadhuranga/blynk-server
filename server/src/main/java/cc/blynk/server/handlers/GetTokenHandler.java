package cc.blynk.server.handlers;

import cc.blynk.common.model.messages.protocol.GetTokenMessage;
import cc.blynk.server.auth.User;
import cc.blynk.server.auth.UserRegistry;
import cc.blynk.server.exceptions.IllegalCommandException;
import cc.blynk.server.group.SessionsHolder;
import cc.blynk.server.utils.FileManager;
import io.netty.channel.ChannelHandlerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static cc.blynk.common.model.messages.MessageFactory.produce;

/**
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 2/1/2015.
 *
 */
public class GetTokenHandler extends BaseSimpleChannelInboundHandler<GetTokenMessage> {

    private static final Logger log = LogManager.getLogger(GetTokenHandler.class);

    public GetTokenHandler(FileManager fileManager, UserRegistry userRegistry, SessionsHolder sessionsHolder) {
        super(fileManager, userRegistry, sessionsHolder);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GetTokenMessage message) throws Exception {
        String dashBoardIdString = message.body;

        long dashBoardId;
        try {
            dashBoardId = Long.parseLong(dashBoardIdString);
        } catch (NumberFormatException ex) {
            throw new IllegalCommandException(String.format("Dash board id '%s' not valid.", dashBoardIdString), message.id);
        }

        if (dashBoardId < 0 || dashBoardId > 100) {
            throw new IllegalCommandException(String.format("Token '%s' should ne in range [0..100].", dashBoardIdString), message.id);
        }

        User user = sessionsHolder.findUserByChannel(ctx.channel(), message.id);
        String token = userRegistry.getToken(user, dashBoardId);

        ctx.writeAndFlush(produce(message.id, message.command, token));
    }
}
