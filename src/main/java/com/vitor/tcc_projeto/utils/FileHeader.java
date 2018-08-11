package com.vitor.tcc_projeto.utils;

import org.jgroups.Global;
import org.jgroups.Header;
import org.jgroups.util.Util;

import java.io.DataInput;
import java.io.DataOutput;

public class FileHeader extends Header {
    private String fileName;
    private boolean eof;

    public FileHeader() {
    } // for de-serialization

    public FileHeader(String fileName, boolean eof) {
        this.fileName = fileName;
        this.eof = eof;
    }

    @Override
    public int size() {
        return Util.size(fileName) + Global.BYTE_SIZE;
    }

    //TODO: que tal usar estes métodos al invés dos Streams?

    @Override
    public void writeTo(DataOutput out) throws Exception {
        Util.writeObject(fileName, out);
        out.writeBoolean(eof);
    }

    @Override
    public void readFrom(DataInput in) throws Exception {
        fileName = (String) Util.readObject(in);
        eof = in.readBoolean();
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isEof() {
        return eof;
    }

    public void setEof(boolean eof) {
        this.eof = eof;
    }
}
