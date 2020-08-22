// SPDX-License-Identifier: Apache-2.0
// YAPION
// Copyright (C) 2019,2020 yoyosource

package yapion.packet;

import yapion.annotations.deserialize.YAPIONLoadExclude;
import yapion.annotations.serialize.YAPIONSaveExclude;
import yapion.hierarchy.YAPIONAny;
import yapion.hierarchy.YAPIONVariable;
import yapion.hierarchy.types.YAPIONObject;
import yapion.hierarchy.types.YAPIONValue;
import yapion.parser.YAPIONParser;
import yapion.serializing.YAPIONDeserializer;
import yapion.utils.YAPIONLogger;

import java.io.IOException;
import java.io.InputStream;

@YAPIONSaveExclude(context = "*")
@YAPIONLoadExclude(context = "*")
public class YAPIONInputStream {

    private final InputStream inputStream;
    private YAPIONPacketReceiver yapionPacketReceiver = null;

    private YAPIONPacketIdentifier staticIdentifier = null;
    private YAPIONPacketIdentifierCreator dynamicIdentifier = null;

    private Thread yapionInputStreamHandler = null;
    private boolean running = true;

    /**
     * Creates a YAPIONInputStream from an InputStream.
     *
     * @param inputStream the InputStream
     */
    public YAPIONInputStream(InputStream inputStream) {
        yapionInputStreamHandler = new Thread(() -> {
           while (running) {
               try {
                   Thread.sleep(10);
               } catch (InterruptedException e) {
                   Thread.currentThread().interrupt();
               }
               if (handleAvailable() == 0) continue;
               try {
                   handle();
               } catch (Exception e) {
                   YAPIONLogger.warn(YAPIONLogger.LoggingType.PACKET, "Something went wrong while handling the read object.", e.getCause());
               }
           }
        });
        yapionInputStreamHandler.setDaemon(true);
        yapionInputStreamHandler.start();
        this.inputStream = inputStream;
    }

    /**
     * Set a direct receiver to the data from the InputStream.
     *
     * @param yapionPacketReceiver the receiver
     */
    public void setYAPIONPacketReceiver(YAPIONPacketReceiver yapionPacketReceiver) {
        this.yapionPacketReceiver = yapionPacketReceiver;
    }

    /**
     * Returns an estimate of bytes to be able to read.
     *
     * @return the estimated byte count
     * @throws IOException
     */
    public synchronized int available() throws IOException {
        if (yapionPacketReceiver != null) throw new IOException();
        return inputStream.available();
    }

    /**
     * Read and parses the next YAPIONObject.
     *
     * @return the next YAPIONObject
     * @throws IOException
     */
    public synchronized YAPIONObject read() throws IOException {
        if (yapionPacketReceiver != null) throw new IOException();
        return YAPIONParser.parse(inputStream);
    }

    /**
     * Read, parses and deserialized the next YAPIONObject.
     *
     * @return the next Object
     * @throws IOException
     */
    public synchronized Object readObject() throws IOException {
        if (yapionPacketReceiver != null) throw new IOException();
        return YAPIONDeserializer.deserialize(read());
    }

    /**
     * Closes this InputStream and tries to close the handler Thread
     *
     * @throws IOException
     */
    public synchronized void close() throws IOException {
        running = false;
        inputStream.close();
    }

    public synchronized void identifier(YAPIONPacketIdentifier yapionPacketIdentifier) {
        this.dynamicIdentifier = null;
        this.staticIdentifier = yapionPacketIdentifier;
    }

    public synchronized void identifier(YAPIONPacketIdentifierCreator yapionPacketIdentifierCreator) {
        this.staticIdentifier = null;
        this.dynamicIdentifier = yapionPacketIdentifierCreator;
    }

    private synchronized int handleAvailable() {
        if (yapionPacketReceiver == null) return 0;
        try {
            return inputStream.available();
        } catch (IOException e) {
            return 0;
        }
    }

    private synchronized void handle() {
        if (yapionPacketReceiver == null) return;
        YAPIONObject yapionObject = YAPIONParser.parse(inputStream);
        YAPIONVariable variable = yapionObject.getVariable("@type");
        if (variable == null) return;
        YAPIONAny yapionAny = variable.getValue();
        if (!(yapionAny instanceof YAPIONValue)) return;
        Object object = ((YAPIONValue)yapionAny).get();
        if (!(object instanceof String)) return;
        if (!object.equals(YAPIONPacket.class.getTypeName())) return;
        YAPIONPacket yapionPacket = (YAPIONPacket) YAPIONDeserializer.deserialize(yapionObject);
        if (staticIdentifier != null) yapionPacket.setYapionPacketIdentifier(staticIdentifier);
        if (dynamicIdentifier != null) yapionPacket.setYapionPacketIdentifier(dynamicIdentifier.identifier());
        yapionPacketReceiver.handle(yapionPacket);
    }

}