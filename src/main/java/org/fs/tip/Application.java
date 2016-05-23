package org.fs.tip;

import java.io.IOException;

import org.fs.tip.server.ServerBuilder;

public class Application {
	public static void main(String[] args) {
		try {
			new Thread(ServerBuilder.build()).start();
		} catch(IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
