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

public final class JTupException extends Exception {
    /* these numbers must be synchronized with SmpError enum */
    public static final int CODE_INVALID_PARAM = -1;
    public static final int CODE_NO_MEM = -2;
    public static final int CODE_NO_DEVICE = -3;
    public static final int CODE_NOT_FOUND = -4;
    public static final int CODE_BUSY = -5;
    public static final int CODE_PERM = -6;
    public static final int CODE_BAD_FD = -7;
    public static final int CODE_NOT_SUPPORTED = -8;
    public static final int CODE_WOULD_BLOCK = -9;
    public static final int CODE_IO = -10;
    public static final int CODE_EXIST = -11;
    public static final int CODE_TOO_BIG = -12;
    public static final int CODE_TIMEDOUT = -13;
    public static final int CODE_OVERFLOW = -14;
    public static final int CODE_BAD_MESSAGE = -15;
    public static final int CODE_BAD_TYPE = -16;
    public static final int CODE_BAD_OTHER = -100;


    public JTupException(int code, String message) {
        super(message + ": " + codeToString(code) + " (" + code + ")");
        m_code = code;
    }

    public int getCode() {
        return m_code;
    }

    private static String codeToString(int code) {
        switch (code) {
            case CODE_INVALID_PARAM:
                return "invalid argument";
            case CODE_NO_MEM:
                return "not enough space";
            case CODE_NO_DEVICE:
                return "no such device";
            case CODE_NOT_FOUND:
                return "no such file or directory";
            case CODE_BUSY:
                return "device or resource busy";
            case CODE_PERM:
                return "bad permission";
            case CODE_BAD_FD:
                return "bad file descriptor";
            case CODE_NOT_SUPPORTED:
                return "operation not supported";
            case CODE_WOULD_BLOCK:
                return "resource temporarily unavailable";
            case CODE_IO:
                return "io error";
            case CODE_EXIST:
                return "already exist";
            case CODE_TOO_BIG:
                return "too long";
            case CODE_TIMEDOUT:
                return "timeout";
            case CODE_OVERFLOW:
                return "overflow";
            case CODE_BAD_MESSAGE:
                return "bad message";
            case CODE_BAD_TYPE:
                return "bad type";
            case CODE_BAD_OTHER:
                return "other";
            default:
                return "unknown";
        }
    }

    private int m_code;
}
