package com.uncle.newshanxun.bean;

public class myMessage {
	public static final int TYPE_TOAST = 1;
	public static final int TYPE_DIALOG = 2;
	public static final int TYPE_CHANGEPREF = 3;
	public static final int TYPE_SXDIALSUCC = 4;
	
	
	private int type;
	private String mess;
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getMess() {
		return mess;
	}
	public void setMess(String mess) {
		this.mess = mess;
	}
}
