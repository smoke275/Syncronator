package com.syncro.encryption;

/**
 * Exception class for Crypto Exception
 */
public class CryptoException extends Exception {

    public CryptoException() {
    }

    public CryptoException(String message, Throwable throwable) {
        super(message, throwable);
    }
}