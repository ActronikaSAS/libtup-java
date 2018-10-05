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

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

public final class Message {
    static {
        System.loadLibrary("tup-jni");
    }

    public static final int TYPE_NONE = 0;
    public static final int TYPE_ACK = 1;
    public static final int TYPE_ERROR = 2;
    public static final int TYPE_LOAD = 10;
    public static final int TYPE_PLAY = 11;
    public static final int TYPE_STOP = 12;
    public static final int TYPE_GET_VERSION = 13;
    public static final int TYPE_GET_PARAMETER = 14;
    public static final int TYPE_SET_PARAMETER = 15;
    public static final int TYPE_BIND = 16;
    public static final int TYPE_GET_BUILDINFO = 19;
    public static final int TYPE_GET_INPUT = 21;
    public static final int TYPE_SET_INPUT = 22;

    public static final int TYPE_BEGIN_EFFECT_UPLOAD = 50;
    public static final int TYPE_UPLOAD_EFFECT_PART = 51;
    public static final int TYPE_END_EFFECT_UPLOAD = 52;

    public static final int TYPE_RESP_VERSION = 100;
    public static final int TYPE_RESP_PARAMETER = 101;
    public static final int TYPE_RESP_BUILDINFO = 103;
    public static final int TYPE_RESP_INPUT = 104;

    public static final int BINDING_FLAGS_ACTUATOR_1 = 0x1;
    public static final int BINDING_FLAGS_ACTUATOR_2 = 0x2;

    public Message() {
        m_msg = create();
        if (m_msg == 0)
            throw new RuntimeException("failed to create tup message");

        m_msg_allocated = true;
    }

    /* shall only be use in Context */
    public Message(long msg) {
        m_msg = msg;
        m_type = getType(msg);
        parseMessage();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (m_msg_allocated)
            destroy(m_msg);
    }

    public long opaquePtr() { return m_msg; }

    public int type() { return m_type; }

    public void initGetVersion() {
        setType(TYPE_GET_VERSION);
        initGetVersion(m_msg);
    }

    public void initGetBuildInfo() {
        setType(TYPE_GET_BUILDINFO);
        initGetBuildInfo(m_msg);
    }

    public void initLoad(int slot, int effect) {
        setType(TYPE_LOAD);
        initLoad(m_msg, slot, effect);
    }

    public void initBindEffect(int slot, int binding_flags) {
        setType(TYPE_BIND);
        initBindEffect(m_msg, slot, binding_flags);
    }

    public void initPlay(int slot) {
        setType(TYPE_PLAY);
        initPlay(m_msg, slot);
    }

    public void initStop(int slot) {
        setType(TYPE_STOP);
        initStop(m_msg, slot);
    }

    public void initGetInputs(int slot, int[] ids) {
        setType(TYPE_GET_INPUT);
        initGetInputs(m_msg, slot, ids);
    }

    public void initSetInputs(int slot, Map<Integer, Integer> inputs) {
        int[] iinputs;

        setType(TYPE_SET_INPUT);
        iinputs = new int[inputs.size() * 2];

        Iterator<Map.Entry<Integer, Integer>> entries = inputs.entrySet().iterator();
        int i = 0;
        while (entries.hasNext()) {
            Map.Entry<Integer, Integer> entry = entries.next();

            iinputs[2 * i] = entry.getKey();
            iinputs[2 * i + 1 ] = entry.getValue();
        }

        initSetInputs(m_msg, slot, iinputs);
    }

    public void initGetParameters(int slot, int[] ids) {
        setType(TYPE_GET_PARAMETER);
        initGetParameters(m_msg, slot, ids);
    }

    public void initSetParameters(int slot, Map<Integer, Integer> params) {
        int[] iparams;

        setType(TYPE_SET_PARAMETER);
        iparams = new int[params.size() * 2];

        Iterator<Map.Entry<Integer, Integer>> entries = params.entrySet().iterator();
        int i = 0;
        while (entries.hasNext()) {
            Map.Entry<Integer, Integer> entry = entries.next();

            iparams[2 * i] = entry.getKey();
            iparams[2 * i + 1 ] = entry.getValue();
        }

        initSetParameters(m_msg, slot, iparams);
    }

    public void initBeginEffectUpload(int effect_id, long n_parts) {
        setType(TYPE_BEGIN_EFFECT_UPLOAD);
        initBeginEffectUpload(m_msg, effect_id, n_parts);
    }

    public void initUploadEffectPart(long part_no, byte[] data, int size) {
        setType(TYPE_UPLOAD_EFFECT_PART);
        initUploadEffectPart(m_msg, part_no, data, size);
    }

    public void initEndEffectUpload() {
        setType(TYPE_END_EFFECT_UPLOAD);
        initEndEffectUpload(m_msg);
    }

    public int getCmd() {
        if (m_type != TYPE_ACK && m_type != TYPE_ERROR)
            throw new IllegalStateException("not an ack or an error message");

        return m_res_cmd;
    }

    public int getError() {
        if (m_type != TYPE_ERROR)
            throw new IllegalStateException("not an error message");

        return m_res_error_code;
    }

    public String getVersion() {
        if (m_type != TYPE_RESP_VERSION)
            throw new IllegalStateException("not a version message");

        return m_res_version;
    }

    public String getBuildInfo() {
        if (m_type != TYPE_RESP_BUILDINFO)
            throw new IllegalStateException("not a buildinfo message");

        return m_res_buildinfo;
    }

    public Map<Integer, Integer> getParameters() {
        if (m_type != TYPE_RESP_PARAMETER)
            throw new IllegalStateException("not a parameters response message");

        return m_res_parameters;
    }

    public Map<Integer, Integer> getInputs() {
        if (m_type != TYPE_RESP_INPUT)
            throw new IllegalStateException("not an input response message");

        return m_res_inputs;
    }

    private void setType(int type) {
        if (m_type != TYPE_NONE)
            clear(m_msg);

        m_type = type;
    }

    private void parseMessage() {
        switch (m_type) {
            case TYPE_ACK:
                parseAck(m_msg);
                break;
            case TYPE_ERROR:
                parseError(m_msg);
                break;
            case TYPE_RESP_VERSION:
                parseRespVersion(m_msg);
                break;
            case TYPE_RESP_BUILDINFO:
                parseRespBuildInfo(m_msg);
                break;
            case TYPE_RESP_PARAMETER:
                m_res_parameters = new HashMap<>();
                parseRespParameters(m_msg, m_res_parameters);
                break;
            case TYPE_RESP_INPUT:
                m_res_inputs = new HashMap<>();
                parseRespInputs(m_msg, m_res_inputs);
                break;
        }
    }

    private native long create();
    private native void destroy(long msg);
    private native void clear(long msg);

    private native int getType(long msg);

    private native void parseAck(long msg);
    private native void parseError(long msg);

    private native void initGetBuildInfo(long msg);
    private native void initGetVersion(long msg);
    private native void initLoad(long msg, int slot, int effect);
    private native void initBindEffect(long msg, int slot, int binding_flags);
    private native void initPlay(long msg, int slot);
    private native void initStop(long msg, int slot);

    private native void initGetParameters(long msg, int effect_id, int[] params);
    private native void initSetParameters(long msg, int effect_id, int[] params);
    private native void initGetInputs(long msg, int effect_id, int[] inputs);
    private native void initSetInputs(long msg, int effect_id, int[] inputs);

    private native void initBeginEffectUpload(long msg, int effect_id, long n_parts);
    private native void initUploadEffectPart(long msg, long part_no, byte[] data, int size);
    private native void initEndEffectUpload(long msg);

    private native int parseRespVersion(long msg);
    private native int parseRespBuildInfo(long msg);
    private native int parseRespParameters(long msg, Map<Integer, Integer> map);
    private native int parseRespInputs(long msg, Map<Integer, Integer> map);

    private long m_msg;
    private int m_type;
    private boolean m_msg_allocated = false;

    /* attributes used to store parsing result */
    private int m_res_cmd;
    private int m_res_error_code;
    private String m_res_version = null;
    private String m_res_buildinfo = null;
    private Map<Integer, Integer> m_res_parameters = null;
    private Map<Integer, Integer> m_res_inputs = null;
}
