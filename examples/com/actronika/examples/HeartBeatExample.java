package com.actronika.examples;

import com.actronika.JTup.Context;
import com.actronika.JTup.Message;
import com.actronika.JTup.JTupException;

import java.util.concurrent.TimeoutException;

public final class HeartBeatExample {
    static int HEARTBEAT_EFFECT_ID = 4;

    public static void usage() {
        System.out.println("Usage: <device>");
    }

    static public class TupListener implements Context.Listener {
        public void onNewMessage(Context ctx, Message msg) {
            /* wait for ack for play */
            if (msg.type() == Message.TYPE_ERROR) {
                System.out.println("Got error for cmd " + msg.getCmd());
                m_ack_received = true;
            } else if (msg.type() == Message.TYPE_ACK
                    && msg.getCmd() == Message.TYPE_PLAY) {
                System.out.println("Effect playing");
                m_ack_received = true;
            }
        }

        public static boolean m_ack_received = false;
    }

    public static void main(String[] args) {
        Context ctx = new Context();
        Message msg = new Message();
        TupListener listener = new TupListener();

        if (args.length != 1) {
            usage();
            return;
        }

        ctx.setListener(listener);

        try {
            ctx.open(args[0]);

            msg.initLoad(0, HEARTBEAT_EFFECT_ID);
            ctx.send(msg);

            msg.initBindEffect(0, 3);
            ctx.send(msg);

            msg.initPlay(0);
            ctx.send(msg);

            while (!TupListener.m_ack_received) {
                ctx.waitAndProcess(-1);
            }

            ctx.close();
        } catch (JTupException e) {
            System.out.println(e);
        } catch (TimeoutException e) {
            System.out.println(e);
        }
    }
}
