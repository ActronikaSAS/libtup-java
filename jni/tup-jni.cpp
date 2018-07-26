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

#include <jni.h>
#include <string>

#include <libtup.h>
#include <errno.h>

extern "C" {

enum JTupExceptionErrorCode
{
    JTUP_EXCEPTION_ERROR_CODE_ENOENT = 1,
    JTUP_EXCEPTION_ERROR_CODE_E2BIG = 2,
    JTUP_EXCEPTION_ERROR_CODE_EBADF = 3,
    JTUP_EXCEPTION_ERROR_CODE_EAGAIN = 4,
    JTUP_EXCEPTION_ERROR_CODE_ENOMEM = 5,
    JTUP_EXCEPTION_ERROR_CODE_EFAULT = 6,
    JTUP_EXCEPTION_ERROR_CODE_EBUSY = 7,
    JTUP_EXCEPTION_ERROR_CODE_EINVAL = 8,
    JTUP_EXCEPTION_ERROR_CODE_ENOSYS = 9,
    JTUP_EXCEPTION_ERROR_CODE_EBADMSG = 10,
    JTUP_EXCEPTION_ERROR_CODE_ETIMEDOUT = 11,
};

static JavaVM *jvm;

static int convert_error(int error)
{
    switch (error) {
        case 0:
            return 0;
        case -ENOENT:
            return -JTUP_EXCEPTION_ERROR_CODE_ENOENT;
        case -E2BIG:
            return -JTUP_EXCEPTION_ERROR_CODE_E2BIG;
        case -EBADF:
            return -JTUP_EXCEPTION_ERROR_CODE_EBADF;
        case -EAGAIN:
            return -JTUP_EXCEPTION_ERROR_CODE_EAGAIN;
        case -ENOMEM:
            return -JTUP_EXCEPTION_ERROR_CODE_ENOMEM;
        case -EFAULT:
            return -JTUP_EXCEPTION_ERROR_CODE_EFAULT;
        case -EBUSY:
            return -JTUP_EXCEPTION_ERROR_CODE_EBUSY;
        case -EINVAL:
            return -JTUP_EXCEPTION_ERROR_CODE_EINVAL;
        case -ENOSYS:
            return -JTUP_EXCEPTION_ERROR_CODE_ENOSYS;
        case -EBADMSG:
            return -JTUP_EXCEPTION_ERROR_CODE_EBADMSG;
        case -ETIMEDOUT:
            return -JTUP_EXCEPTION_ERROR_CODE_ETIMEDOUT;
        default:
            return -JTUP_EXCEPTION_ERROR_CODE_EFAULT;
    }
}

static jobject new_integer(JNIEnv *env, int value)
{
    static jclass cls = NULL;
    static jmethodID mid = NULL;
    static JNIEnv *prev_env = NULL;

    if (prev_env != env) {
        cls = NULL;
        mid = NULL;
    }

    if (cls == NULL) {
        cls = env->FindClass("java/lang/Integer");
        if (cls == NULL)
            return NULL;
    }

    if (mid == NULL) {
        mid = env->GetMethodID(cls, "<init>", "(I)V");
        if (mid == NULL)
            return NULL;
    }

    return env->NewObject(cls, mid, value);
}

static void on_new_message(TupContext *ctx, TupMessage *msg, void *userdata)
{
    jobject obj = (jobject) userdata;
    JNIEnv *env;
    jint ret;

    ret = jvm->GetEnv((void **) &env, JNI_VERSION_1_6);
    if (ret == JNI_OK) {
        jclass cls = env->GetObjectClass(obj);
        jmethodID mid = env->GetMethodID(cls, "on_new_native_message", "(JJ)V");
        env->CallVoidMethod(obj, mid, (jlong) ctx, (jlong) msg);
    }
}

jint JNI_OnLoad(JavaVM *vm, void *reserved)
{
    jvm = vm;

    return JNI_VERSION_1_6;
}

JNIEXPORT jlong JNICALL
Java_com_actronika_JTup_Context_create(JNIEnv *env, jobject obj, jstring jpath)
{
    TupContext *ctx;
    const char *path = env->GetStringUTFChars(jpath, NULL);
    TupCallbacks cbs = {
            .new_message = on_new_message,
    };
    jobject ref;

    ref = env->NewGlobalRef(obj);

    ctx = tup_context_new(path, &cbs, ref);
    env->ReleaseStringUTFChars(jpath, path);

    if (ctx == NULL) {
        env->DeleteGlobalRef(ref);
        return 0;
    }

    return (jlong) ctx;
}

JNIEXPORT void JNICALL
Java_com_actronika_JTup_Context_destroy(JNIEnv *env, jobject obj, jlong jctx)
{
    TupContext *ctx = (TupContext *) jctx;

    env->DeleteGlobalRef((jobject) ctx->userdata);
    tup_context_free(ctx);
}

JNIEXPORT jint JNICALL
Java_com_actronika_JTup_Context_setConfig(JNIEnv *env, jobject obj,
        jlong jctx, jint baudrate, jint parity, jboolean flow_control)
{
    int ret;

    ret = tup_context_set_config((TupContext *) jctx,
            (SmpSerialFrameBaudrate) baudrate, (SmpSerialFrameParity) parity,
            (flow_control == JNI_TRUE) ? 1 : 0);
    return (ret == 0) ? 0 : convert_error(ret);
}

JNIEXPORT jint JNICALL
Java_com_actronika_JTup_Context_send(JNIEnv *env, jobject obj, jlong jctx,
        jlong jmsg)
{
    int ret;

    ret = tup_context_send((TupContext *) jctx, (TupMessage *) jmsg);
    return (ret == 0) ? 0 : convert_error(ret);
}

JNIEXPORT jint JNICALL
Java_com_actronika_JTup_Context_processFd(JNIEnv *env, jobject obj, jlong jctx)
{
    int ret;

    ret = tup_context_process_fd((TupContext *) jctx);
    return (ret == 0) ? 0 : convert_error(ret);
}

JNIEXPORT jint JNICALL
Java_com_actronika_JTup_Context_waitAndProcess(JNIEnv *env, jobject obj,
        jlong jctx, jint timeout_ms)
{
    int ret;

    ret = tup_context_wait_and_process((TupContext *) jctx, timeout_ms);
    return (ret == 0) ? 0 : convert_error(ret);
}

JNIEXPORT jlong JNICALL
Java_com_actronika_JTup_Message_create(JNIEnv *env, jobject obj)
{
    return (jlong) tup_message_new();
}

JNIEXPORT void JNICALL
Java_com_actronika_JTup_Message_destroy(JNIEnv *env, jobject obj, jlong jmsg)
{
    tup_message_free((TupMessage *) jmsg);
}

JNIEXPORT void JNICALL
Java_com_actronika_JTup_Message_clear(JNIEnv *env, jobject obj, jlong jmsg)
{
    tup_message_clear((TupMessage *) jmsg);
}

JNIEXPORT jint JNICALL
Java_com_actronika_JTup_Message_getType(JNIEnv *env, jobject obj, jlong jmsg)
{
    return tup_message_get_type((TupMessage *) jmsg);
}

JNIEXPORT jint JNICALL
Java_com_actronika_JTup_Message_parseAck(JNIEnv *env, jobject obj, jlong jmsg)
{
    TupMessageType type;
    int ret;

    ret = tup_message_parse_ack((TupMessage *) jmsg, &type);
    if (ret < 0)
        return convert_error(ret);

    jfieldID fid = env->GetFieldID(env->GetObjectClass(obj), "m_res_cmd", "I");
    env->SetIntField(obj, fid, type);
    return 0;
}

JNIEXPORT jint JNICALL
Java_com_actronika_JTup_Message_parseError(JNIEnv *env, jobject obj, jlong jmsg)
{
    TupMessageType type;
    uint32_t error;
    int ret;
    jfieldID fid;
    jclass cls;

    ret = tup_message_parse_error((TupMessage *) jmsg, &type, &error);
    if (ret < 0)
        return convert_error(ret);

    cls = env->GetObjectClass(obj);

    fid = env->GetFieldID(cls, "m_res_cmd", "I");
    env->SetIntField(obj, fid, type);

    fid = env->GetFieldID(cls, "m_res_error_code", "I");
    env->SetIntField(obj, fid, error);
    return 0;
}

JNIEXPORT void JNICALL
Java_com_actronika_JTup_Message_initGetVersion(JNIEnv *env, jobject obj,
        jlong jmsg)
{
    tup_message_init_get_version((TupMessage *) jmsg);
}

JNIEXPORT void JNICALL
Java_com_actronika_JTup_Message_initLoad(JNIEnv *env, jobject obj, jlong jmsg,
        jint slot_id, jint effect_id)
{
    tup_message_init_load((TupMessage *) jmsg, slot_id, effect_id);
}

JNIEXPORT void JNICALL
Java_com_actronika_JTup_Message_initPlay(JNIEnv *env, jobject obj, jlong jmsg,
        jint slot_id)
{
    tup_message_init_play((TupMessage *) jmsg, slot_id);
}

JNIEXPORT void JNICALL
Java_com_actronika_JTup_Message_initStop(JNIEnv *env, jobject obj, jlong jmsg,
        jint slot_id)
{
    tup_message_init_stop((TupMessage *) jmsg, slot_id);
}

JNIEXPORT void JNICALL
Java_com_actronika_JTup_Message_initBindEffect(JNIEnv *env, jobject obj,
        jlong jmsg, jint slot_id, jint binding_flags)
{
    tup_message_init_bind_effect((TupMessage *) jmsg, slot_id, binding_flags);
}

JNIEXPORT void JNICALL
Java_com_actronika_JTup_Message_initGetBuildInfo(JNIEnv *env, jobject obj,
        jlong jmsg)
{
    tup_message_init_get_buildinfo((TupMessage *) jmsg);
}

JNIEXPORT jint JNICALL
Java_com_actronika_JTup_Message_initGetParameters(JNIEnv *env, jobject obj,
        jlong jmsg, jint effect_id, jintArray jparam_ids)
{
    jint *data;
    uint8_t *param_ids;
    jsize len;
    int ret;

    len = env->GetArrayLength(jparam_ids);
    data = env->GetIntArrayElements(jparam_ids, NULL);
    param_ids = new uint8_t[len];

    for (jsize i = 0; i < len; i++)
        param_ids[i] = data[i];

    ret = tup_message_init_get_parameter_array((TupMessage *) jmsg, effect_id,
        param_ids, len);

    delete[] param_ids;
    env->ReleaseIntArrayElements(jparam_ids, data, JNI_ABORT);

    return (ret == 0) ? 0 : convert_error(ret);
}

/*
 * jparams is an array of int containing pair (id,value)
 */
JNIEXPORT jint JNICALL
Java_com_actronika_JTup_Message_initSetParameters(JNIEnv *env, jobject obj,
        jlong jmsg, jint effect_id, jintArray jparams)
{
    jint *params;
    jsize len;
    TupParameterArgs *args;
    size_t n_params;
    int ret;

    len = env->GetArrayLength(jparams);
    n_params = len / 2;
    if (n_params == 0)
        return convert_error(-EINVAL);

    params = env->GetIntArrayElements(jparams, NULL);
    args = new TupParameterArgs[n_params];

    for (size_t i = 0; i < n_params; i++) {
        args[i].parameter_id = params[i];
        args[i].parameter_value = params[i + 1];
    }

    ret = tup_message_init_set_parameter_array((TupMessage *) jmsg, effect_id,
            args, n_params);

    delete[] args;
    env->ReleaseIntArrayElements(jparams, params, JNI_ABORT);

    return (ret == 0) ? 0 : convert_error(ret);
}

JNIEXPORT jint JNICALL
Java_com_actronika_JTup_Message_initGetInputs(JNIEnv *env, jobject obj,
        jlong jmsg, jint effect_id, jintArray jinput_ids)
{
    jint *data;
    uint8_t *input_ids;
    jsize len;
    int ret;

    len = env->GetArrayLength(jinput_ids);
    data = env->GetIntArrayElements(jinput_ids, NULL);
    input_ids = new uint8_t[len];

    for (jsize i = 0; i < len; i++)
        input_ids[i] = data[i];

    ret = tup_message_init_get_input_value_array((TupMessage *) jmsg, effect_id,
        input_ids, len);

    delete[] input_ids;
    env->ReleaseIntArrayElements(jinput_ids, data, JNI_ABORT);

    return (ret == 0) ? 0 : convert_error(ret);
}

/*
 * jparams is an array of int containing pair (id,value)
 */
JNIEXPORT jint JNICALL
Java_com_actronika_JTup_Message_initSetInputs(JNIEnv *env, jobject obj,
        jlong jmsg, jint effect_id, jintArray jinputs)
{
    jint *inputs;
    jsize len;
    TupInputValueArgs *args;
    size_t n_inputs;
    int ret;

    len = env->GetArrayLength(jinputs);
    n_inputs = len / 2;
    if (n_inputs == 0)
        return convert_error(-EINVAL);

    inputs = env->GetIntArrayElements(jinputs, NULL);
    args = new TupInputValueArgs[n_inputs];

    for (size_t i = 0; i < n_inputs; i++) {
        args[i].input_id = inputs[2 * i];
        args[i].input_value = inputs[2 * i + 1];
    }

    ret = tup_message_init_set_input_value_array((TupMessage *) jmsg,
            effect_id, args, n_inputs);

    delete[] args;
    env->ReleaseIntArrayElements(jinputs, inputs, JNI_ABORT);

    return (ret == 0) ? 0 : convert_error(ret);
}

JNIEXPORT jint JNICALL
Java_com_actronika_JTup_Message_parseRespVersion(JNIEnv *env, jobject obj,
        jlong jmsg)
{
    const char *version;
    int ret;

    ret = tup_message_parse_resp_version((TupMessage *) jmsg, &version);
    if (ret < 0)
        return convert_error(ret);

    jfieldID fid = env->GetFieldID(env->GetObjectClass(obj), "m_res_version",
            "Ljava/lang/String;");
    env->SetObjectField(obj, fid, env->NewStringUTF(version));
    return 0;
}

JNIEXPORT jint JNICALL
Java_com_actronika_JTup_Message_parseRespBuildInfo(JNIEnv *env, jobject obj,
        jlong jmsg)
{
    const char *binfo;
    int ret;

    ret = tup_message_parse_resp_buildinfo((TupMessage *) jmsg, &binfo);
    if (ret < 0)
        return convert_error(ret);

    jfieldID fid = env->GetFieldID(env->GetObjectClass(obj), "m_res_buildinfo",
            "Ljava/lang/String;");
    env->SetObjectField(obj, fid, env->NewStringUTF(binfo));
    return 0;
}

JNIEXPORT jint JNICALL
Java_com_actronika_JTup_Message_parseRespParameters(JNIEnv *env, jobject obj,
        jlong jmsg, jobject map)
{
    int ret;
    uint8_t effect_id;
    TupParameterArgs *args;
    size_t n_args;
    jmethodID mid;

    n_args = (smp_message_n_args((TupMessage *) jmsg) - 1) / 2;
    args = new TupParameterArgs[n_args];
    ret = tup_message_parse_resp_parameter((TupMessage *) jmsg, &effect_id,
            args, n_args);
    if (ret < 0)
        goto done;

    mid = env->GetMethodID(env->GetObjectClass(map), "put",
            "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
    for (size_t i = 0; i < (size_t) ret; i++) {
        env->CallObjectMethod(map, mid, new_integer(env, args[i].parameter_id),
                new_integer(env, args[i].parameter_value));
    }

done:
    delete[] args;

    return (ret == 0) ? 0 : convert_error(ret);
}

JNIEXPORT jint JNICALL
Java_com_actronika_JTup_Message_parseRespInputs(JNIEnv *env, jobject obj,
        jlong jmsg, jobject map)
{
    int ret;
    uint8_t effect_id;
    TupInputValueArgs *args;
    size_t n_args;
    jmethodID mid;

    n_args = (smp_message_n_args((TupMessage *) jmsg) - 1) / 2;
    args = new TupInputValueArgs[n_args];
    ret = tup_message_parse_resp_input((TupMessage *) jmsg, &effect_id,
            args, n_args);
    if (ret < 0)
        goto done;

    mid = env->GetMethodID(env->GetObjectClass(map), "put",
            "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
    for (size_t i = 0; i < (size_t) ret; i++) {
        env->CallObjectMethod(map, mid, new_integer(env, args[i].input_id),
                new_integer(env, args[i].input_value));
    }

done:
    delete[] args;

    return (ret == 0) ? 0 : convert_error(ret);
}

}
