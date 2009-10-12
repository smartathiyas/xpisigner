package org.oregan.gui.capi;

/**
 * Copyright 2007 Kevin O'Regan (http//o-regan.org)a
 */
class FileInfo
{
    private String name;
    private long length;
    private long crc;
    private byte[] md5;
    private byte[] sha1;

    public boolean isCompressed() {
        return compressed;
    }

    private boolean compressed;

    public FileInfo(String name, long length, long crc, byte[] md5, byte[] sha1, boolean compress) {
        this.name = name;
        this.length = length;
        this.crc = crc;
        this.md5 = md5;
        this.sha1 = sha1;
        this.compressed = compress;
    }

    public String getName() {
        return name;
    }

    public long getLength() {
        return length;
    }

    public long getCRC() {
        return crc;
    }

    public byte[] getMd5() {
        return md5;
    }

    public byte[] getSha1() {
        return sha1;
    }
}
