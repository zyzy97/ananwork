package com.lvshandian.partylive.wangyiyunxin.chatroom.viewholder;

import android.graphics.Color;
import android.text.method.LinkMovementMethod;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.TextView;

import com.lvshandian.partylive.utils.LogUtils;
import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;
import com.netease.nim.uikit.session.emoji.MoonUtil;
import com.netease.nim.uikit.session.viewholder.MsgViewHolderBase;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMessage;

import java.util.Map;

/**
 * Created by sll on 2016/11/22.
 */

/**
 * 直播室聊天的ViewHolder，用于填充直播室聊天数据，根据扩展消息的type不同展示不同数据
 * 观众想看表演
 * 愿意支付你的表演，要加油哦！
 *
 * @author sll
 * @time 2016/11/22 17:07
 */
public class ChatRoomViewHolderShowUser extends MsgViewHolderBase {
    @Override
    protected boolean isChatRoom() {
        return true;
    }

    @Override
    protected boolean isShowBubble() {
        return false;
    }

    @Override
    protected boolean isShowHeadImage() {
        return true;
    }

    @Override
    public void setNameTextView() {
        nameContainer.setPadding(ScreenUtil.dip2px(6), 0, 0, 0);
        ChatRoomViewHolderHelper.setNameTextView((ChatRoomMessage) message, nameTextView, nameIconView, context);
        LogUtils.i("WangYi", "ChatRoomViewHolderGift:" + message.getRemoteExtension());
        nameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClick();
            }
        });
        nameIconView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClick();
            }
        });
    }

    @Override
    protected int getContentResId() {
        return com.netease.nim.uikit.R.layout.nim_message_item_gift;
    }

    @Override
    protected void inflateContentView() {

    }

    @Override
    protected void bindContentView() {
        TextView bodyTextView = findViewById(com.netease.nim.uikit.R.id.nim_message_item_text_body_gift);
        bodyTextView.setTextColor(Color.WHITE);
        bodyTextView.setPadding(ScreenUtil.dip2px(6), 0, 0, 0);
//        layoutDirection();
//        bodyTextView.setTextColor(context.getResources().getColor(R.color.tv_main));
        bodyTextView.setMovementMethod(LinkMovementMethod.getInstance());
        bodyTextView.setOnLongClickListener(longClickListener);
        bodyTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClick();
            }
        });
//        LogUtils.i("WangYi", "ChatRoomViewHolderZBLive:" + message.getRemoteExtension());
        //愿意支付你的表演，要加油哦！
//        String costText = ((ChatRoomMessage)message).getChatRoomMessageExtension().getSenderNick() + "愿意支付你的表演，要加油哦！";
        String costText = "愿意支付你的表演，要加油哦！";
        MoonUtil.identifyFaceExpression(NimUIKit.getContext(), bodyTextView, costText, ImageSpan.ALIGN_BOTTOM);
    }

//    private void layoutDirection() {
//        TextView bodyTextView = findViewById(com.netease.nim.uikit.R.id.nim_message_item_text_body);
//        bodyTextView.setPadding(ScreenUtil.dip2px(6), 0, 0, 0);
//    }
}
