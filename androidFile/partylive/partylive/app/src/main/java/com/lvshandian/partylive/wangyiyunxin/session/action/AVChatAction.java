package com.lvshandian.partylive.wangyiyunxin.session.action;

import android.widget.Toast;

import com.lvshandian.partylive.R;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.netease.nim.uikit.session.actions.BaseAction;
import com.netease.nimlib.sdk.avchat.constant.AVChatType;

/**
 * Created by hzxuwen on 2015/6/12.
 */
public class AVChatAction extends BaseAction {
    private AVChatType avChatType;

    /**
     * 构造函数
     *
     * @param iconResId 图标 res id
     * @param titleId   图标标题的string res id
     */
    protected AVChatAction(int iconResId, int titleId) {
        super(iconResId, titleId);
    }

//    public AVChatAction(AVChatType avChatType) {
//        super(avChatType == AVChatType.AUDIO ? R.drawable.message_plus_audio_chat_selector : R.drawable.message_plus_video_chat_selector,
//                avChatType == AVChatType.AUDIO ? R.string.input_panel_audio_call : R.string.input_panel_video_call);
//        this.avChatType = avChatType;
//    }

    @Override
    public void onClick() {
        if (NetworkUtil.isNetAvailable(getActivity())) {
            startAudioVideoCall(avChatType);
        } else {
            Toast.makeText(getActivity(), R.string.network_is_not_available, Toast.LENGTH_SHORT).show();
        }
    }

    /************************
     * 音视频通话
     ***********************/

    public void startAudioVideoCall(AVChatType avChatType) {
//        AVChatActivity.start(getActivity(), getAccount(), avChatType.getValue(), AVChatActivity.FROM_INTERNAL);
    }
}
