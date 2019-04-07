package model;

import channel.Channel;
import command.CommandType;
import command.PutCommand;
import command.StandardCommand;

import java.util.HashMap;
import java.util.Map;

public class DistributedMap implements SimpleStringMap {

    private HashMap<String, Integer> state = new HashMap<>();
    private Channel channel;

    public DistributedMap(String hostName, String clusterName) throws Exception {
        channel = new Channel(hostName, clusterName, this);
    }

    @Override
    public boolean containsKey(String key) {
        return state.containsKey(key);
    }

    @Override
    public Integer get(String key) {
        return state.get(key);
    }

    @Override
    public void put(String key, Integer value) throws Exception {
        channel.sendCommand(new PutCommand(CommandType.PUT, key, value));

        putLocal(key, value);
    }

    public void putLocal(String key, Integer value) {
        state.put(key, value);
    }

    @Override
    public Integer remove(String key) throws Exception {
        channel.sendCommand(new StandardCommand(CommandType.REMOVE, key));

        return removeLocal(key);
    }

    public Integer removeLocal(String key) {
        return state.remove(key);
    }

    public void setState(Map<String, Integer> state) {
        this.state.clear();
        this.state.putAll(state);
    }

    public HashMap<String, Integer> getState() {
        return state;
    }

}
