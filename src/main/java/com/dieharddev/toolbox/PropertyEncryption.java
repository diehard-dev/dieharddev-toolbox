package com.dieharddev.toolbox;

public interface PropertyEncryption {
	String encryptProperty(String property);
	String decryptProperty(String property);
	String encryptHexProperty(String property);
	String decryptHexProperty(String property);
}
