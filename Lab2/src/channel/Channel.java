package channel;

import command.Command;
import model.DistributedMap;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.protocols.*;
import org.jgroups.protocols.pbcast.*;
import org.jgroups.stack.ProtocolStack;

import java.net.InetAddress;

public class Channel extends JChannel {

    public Channel(String hostName, String clusterName, DistributedMap distributedMap) throws Exception {
        super(false);

        initProtocolStack();

        setReceiver(new Receiver(hostName, this, distributedMap));
        connect(clusterName);
        getState(null, 60000);
    }

    private void initProtocolStack() throws Exception {
        ProtocolStack stack = new ProtocolStack();
        this.setProtocolStack(stack);
        stack.addProtocol(new UDP().setValue("mcast_group_addr", InetAddress.getByName("230.100.200.100")))
                .addProtocol(new PING())
                .addProtocol(new MERGE3())
                .addProtocol(new FD_SOCK())
                .addProtocol(new FD_ALL()
                        .setValue("timeout", 12000)
                        .setValue("interval", 3000))
                .addProtocol(new VERIFY_SUSPECT())
                .addProtocol(new BARRIER())
                .addProtocol(new NAKACK2())
                .addProtocol(new UNICAST3())
                .addProtocol(new STABLE())
                .addProtocol(new GMS())
                .addProtocol(new UFC())
                .addProtocol(new MFC())
                .addProtocol(new FRAG2())
                .addProtocol(new STATE())
                .addProtocol(new SEQUENCER())
                .addProtocol(new FLUSH());

        stack.init();
    }

    public void sendCommand(Command command) throws Exception {
        Message message = new Message(null, null, command);

        send(message);
    }
}
