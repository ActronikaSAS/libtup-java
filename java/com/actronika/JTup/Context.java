/* libtup-java
 * Copyright (C) 2018 Actronika SAS
 *     Author: Aurélien Zanelli <aurelien.zanelli@actronika.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.actronika.JTup;

import java.util.concurrent.TimeoutException;

public final class Context {
    public static final int BAUDRATE_1200 = 0;
    public static final int BAUDRATE_2400 = 1;
    public static final int BAUDRATE_4800 = 2;
    public static final int BAUDRATE_9600 = 3;
    public static final int BAUDRATE_19200 = 4;
    public static final int BAUDRATE_38400 = 5;
    public static final int BAUDRATE_57600 = 6;
    public static final int BAUDRATE_115200 = 7;

    public static final int PARITY_NONE = 0;
    public static final int PARITY_ODD = 0;
    public static final int PARITY_EVEN = 0;

    static {
        System.loadLibrary("tup-jni");
    }

    public interface Listener {
        public void onNewMessage(Context ctx, Message msg);
    }

    public Context() {
        m_ctx = create();
        if (m_ctx == 0)
            throw new RuntimeException("failed to create tup context");
    }

    @Override
    protected void finalize() throws Throwable {
        destroy(m_ctx);
        super.finalize();
    }

    public void setListener(Listener listener) {
        m_listener = listener;
    }

    public void open(String devpath) throws JTupException {
        int ret;

        ret = open(m_ctx, devpath);
        if (ret != 0)
            throw new JTupException(ret, "failed to open device");
    }

    public void close() {
        close(m_ctx);
    }

    public void setConfig(int baudrate, int parity, boolean flow_control)
            throws JTupException {
        int ret;

        ret = setConfig(m_ctx, baudrate, parity, flow_control);
        if (ret != 0)
            throw new JTupException(ret, "failed to send config");
    }

    public void send(Message msg) throws JTupException {
        int ret;

        ret = send(m_ctx, msg.opaquePtr());
        if (ret != 0)
            throw new JTupException(ret, "failed to send message");
    }

    public void processFd() throws JTupException {
        int ret;

        ret = processFd(m_ctx);
        if (ret != 0)
            throw new JTupException(ret, "failed to process fd");
    }

    public void waitAndProcess(int timeout_ms)
            throws JTupException, TimeoutException {
        int ret;

        ret = waitAndProcess(m_ctx, timeout_ms);
        if (ret != 0) {
            if (ret == JTupException.CODE_TIMEDOUT)
                throw new TimeoutException("timeout while waiting");
            else
                throw new JTupException(ret, "failed to wait and process");
        }
    }

    private void on_new_native_message(long ctx, long imsg) {
        Message msg = new Message(imsg);

        if (m_listener != null)
            m_listener.onNewMessage(this, msg);
    }

    private native long create();
    private native void destroy(long ctx);
    private native int open(long ctx, String path);
    private native void close(long ctx);
    private native int setConfig(long ctx, int baudrate, int parity,
            boolean flow_control);
    private native int send(long ctx, long msg);
    private native int processFd(long ctx);
    private native int waitAndProcess(long ctx, int timeout_ms);

    private long m_ctx;
    private Listener m_listener;
}
