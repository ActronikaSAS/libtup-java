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
    public static final int CODE_ENOENT = 1;
    public static final int CODE_E2BIG = 2;
    public static final int CODE_EBADF = 3;
    public static final int CODE_EAGAIN = 4;
    public static final int CODE_ENOMEM = 5;
    public static final int CODE_EFAULT = 6;
    public static final int CODE_EBUSY = 7;
    public static final int CODE_EINVAL = 8;
    public static final int CODE_ENOSYS = 9;
    public static final int CODE_EBADMSG = 10;
    public static final int CODE_ETIMEDOUT = 11;

    public JTupException(int code, String message) {
        super(message + ": " + codeToString(code) + " (" + code + ")");
        m_code = code;
    }

    public int getCode() {
        return m_code;
    }

    private static String codeToString(int code) {
        switch (code) {
            case CODE_ENOENT:
                return "no such file or directory";
            case CODE_E2BIG:
                return "too long";
            case CODE_EBADF:
                return "bad file descriptor";
            case CODE_EAGAIN:
                return "resource temporarily unavailable";
            case CODE_ENOMEM:
                return "not enough space";
            case CODE_EFAULT:
                return "fault";
            case CODE_EBUSY:
                return "device or resource busy";
            case CODE_EINVAL:
                return "invalid argument";
            case CODE_ENOSYS:
                return "operation not supported";
            case CODE_EBADMSG:
                return "bad message";
            case CODE_ETIMEDOUT:
                return "timeout";
            default:
                return "unknown";
        }
    }

    private int m_code;
}
