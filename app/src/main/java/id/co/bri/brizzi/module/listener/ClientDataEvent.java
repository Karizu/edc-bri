/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package id.co.bri.brizzi.module.listener;

import java.nio.channels.SocketChannel;

/**
 *
 * @author Ahmad
 */
public class ClientDataEvent {
	public ChannelClient server;
	public SocketChannel socket;
	public byte[] data;
	
	public ClientDataEvent(ChannelClient server, SocketChannel socket, byte[] data) {
		this.server = server;
		this.socket = socket;
		this.data = data;
	}
}
