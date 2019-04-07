package channel;

import command.Command;
import command.CommandParser;
import model.DistributedMap;
import org.jgroups.*;
import org.jgroups.util.Util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalTime;
import java.util.Map;

public class Receiver extends ReceiverAdapter {

    private final DistributedMap distributedMap;

    private String hostName;
    private Channel channel;

    Receiver(String hostName, Channel channel, DistributedMap distributedMap) {
        this.hostName = hostName;
        this.channel = channel;
        this.distributedMap = distributedMap;
    }

    @Override
    public void receive(Message message) {
        if(channel.getAddress().equals(message.getSrc())) {
            return;
        }

        Command command = (Command) message.getObject();

        System.out.println(String.format("[%s] %s: Received message: %s from %s", LocalTime.now(), hostName, command, message.getSrc()));

        try {
            CommandParser.parseReceivedCommand(distributedMap, command);
        } catch (Exception e) {
            System.out.println("Exception during parsing command");
        }
    }

    @Override
    public void getState(OutputStream outputStream) throws Exception {
        synchronized (distributedMap) {
            Util.objectToStream(distributedMap.getState(), new DataOutputStream(outputStream));
        }
    }

    @Override
    public void setState(InputStream inputStream) throws Exception {
        Map<String, Integer> state = (Map<String, Integer>) Util.objectFromStream(new DataInputStream(inputStream));

        synchronized (distributedMap) {
            distributedMap.setState(state);
        }
    }

    @Override
    public void viewAccepted(View view) {
        System.out.println(String.format("[%s] %s: Received view: %s", LocalTime.now(), hostName, view));

        if (view instanceof MergeView) {
            ViewHandler handler = new ViewHandler(channel, (MergeView) view);
            handler.start();
        }
    }

    private static class ViewHandler extends Thread {
        JChannel channel;
        MergeView view;

        private ViewHandler(JChannel channel, MergeView view) {
            this.channel = channel;
            this.view = view;
        }

        public void run() {
            View tmpView = view.getSubgroups().get(0);
            Address localAddress = channel.getAddress();

            if (!tmpView.getMembers().contains(localAddress)) {
                System.out.println(String.format("Not member of the new primary partition (%s), will re-acquire the state", tmpView));
                try {
                    channel.getState(null, 30000);
                } catch (Exception ignored) {
                }
            } else {
                System.out.println(String.format("Not member of the new primary partition (%s), will do nothing", tmpView));
            }
        }
    }
}
