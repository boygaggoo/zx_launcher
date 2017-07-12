package com.ds05.launcher.net.response;

import java.io.IOException;

/**
 * Created by Vincent on 2017/6/22.
 */

public class HeartBeatMsg extends ServerResponse {

    /**
     * 必须调用此方法设置消息号
     *
     * @param msgCode
     */
    public HeartBeatMsg(String msgCode) {
        super(msgCode);
        try {
            output.writeUTF(msgCode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
