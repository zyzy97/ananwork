package com.netease.nim.uikit.session.activity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.ui.dialog.CustomAlertDialog;
import com.netease.nim.uikit.common.ui.dialog.CustomAlertDialog.onSeparateItemClickListener;
import com.netease.nim.uikit.common.ui.imageview.BaseZoomableImageView;
import com.netease.nim.uikit.common.ui.imageview.ImageGestureListener;
import com.netease.nim.uikit.common.util.C;
import com.netease.nim.uikit.common.util.file.AttachmentStore;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.media.BitmapDecoder;
import com.netease.nim.uikit.common.util.media.ImageUtil;
import com.netease.nim.uikit.common.util.storage.StorageUtil;
import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.attachment.ImageAttachment;
import com.netease.nimlib.sdk.msg.constant.AttachStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * 查看聊天消息原图
 * Created by huangjun on 2015/3/6.
 */
public class WatchMessagePictureActivity extends UI {

    private static final String INTENT_EXTRA_IMAGE = "INTENT_EXTRA_IMAGE";
    private static final String TAG = WatchMessagePictureActivity.class.getSimpleName();

    private Handler handler;
    private IMMessage message;
    private List<IMMessage> imageMsgList = new ArrayList<>();
    private int firstDisplayImageIndex = 0;

    private boolean newPageSelected = false;

    private View loadingLayout;
    private BaseZoomableImageView image;
    protected CustomAlertDialog alertDialog;
    private ViewPager imageViewPager;
    private PagerAdapter adapter;
    private AbortableFuture downloadFuture;

    public static void start(Context context, IMMessage message) {
        Intent intent = new Intent();
        intent.putExtra(INTENT_EXTRA_IMAGE, message);
        intent.setClass(context, WatchMessagePictureActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nim_watch_picture_activity);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        this.message = (IMMessage) getIntent().getSerializableExtra(INTENT_EXTRA_IMAGE);
        findViews();
        queryImageMessages();

        handler = new Handler();
        registerObservers(true);
    }

    @Override
    protected void onDestroy() {
        registerObservers(false);
        imageViewPager.setAdapter(null);
        if (downloadFuture != null) {
            downloadFuture.abort();
            downloadFuture = null;
        }
        super.onDestroy();
    }

    private void queryImageMessages() {
        IMMessage anchor = MessageBuilder.createEmptyMessage(message.getSessionId(), message.getSessionType(), 0);

        NIMClient.getService(MsgService.class).queryMessageListByType(MsgTypeEnum.image, anchor, Integer.MAX_VALUE).setCallback(new RequestCallback<List<IMMessage>>() {
            @Override
            public void onSuccess(List<IMMessage> param) {
                imageMsgList.addAll(param);
                Collections.reverse(imageMsgList);
                setDisplayIndex();
                setViewPagerAdapter();
            }

            @Override
            public void onFailed(int code) {
                Log.i(TAG, "query msg by type failed, code:" + code);
            }

            @Override
            public void onException(Throwable exception) {

            }
        });
    }

    // 设置第一个选中的图片index
    private void setDisplayIndex() {
        for (int i = 0; i < imageMsgList.size(); i++) {
            IMMessage imageObject = imageMsgList.get(i);
            if (compareObjects(message, imageObject)) {
                firstDisplayImageIndex = i;
                break;
            }
        }
    }

    protected boolean compareObjects(IMMessage t1, IMMessage t2) {
        return (t1.getUuid().equals(t2.getUuid()));
    }

    private void findViews() {
        alertDialog = new CustomAlertDialog(this);
        loadingLayout = findViewById(R.id.loading_layout);

        imageViewPager = (ViewPager) findViewById(R.id.view_pager_image);
    }

    private void setViewPagerAdapter() {
        adapter = new PagerAdapter() {
            @Override
            public int getCount() {
                return imageMsgList == null ? 0 : imageMsgList.size();
            }

            @Override
            public void notifyDataSetChanged() {
                super.notifyDataSetChanged();
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                View layout = (View) object;
                BaseZoomableImageView iv = (BaseZoomableImageView) layout.findViewById(R.id.watch_image_view);
                iv.clear();
                container.removeView(layout);
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return (view == object);
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                ViewGroup layout;
                layout = (ViewGroup) LayoutInflater.from(WatchMessagePictureActivity.this).inflate(R.layout.image_layout_multi_touch, null);
                layout.setBackgroundColor(Color.BLACK);

                container.addView(layout);
                layout.setTag(position);

                if (position == firstDisplayImageIndex) {
                    onViewPagerSelected(position);
                }

                return layout;
            }

            @Override
            public int getItemPosition(Object object) {
                return POSITION_NONE;
            }
        };

        imageViewPager.setAdapter(adapter);
        imageViewPager.setOffscreenPageLimit(2);
        imageViewPager.setCurrentItem(firstDisplayImageIndex);
        imageViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (positionOffset == 0f && newPageSelected) {
                    newPageSelected = false;
                    onViewPagerSelected(position);
                }
            }

            @Override
            public void onPageSelected(int position) {
                newPageSelected = true;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void onViewPagerSelected(int position) {
        if (downloadFuture != null) {
            downloadFuture.abort();
            downloadFuture = null;
        }
        updateCurrentImageView(position);
        onImageViewFound(image);
        requestOriImage(imageMsgList.get(position));
    }

    // 初始化每个view的image
    protected void updateCurrentImageView(final int position) {
        View currentLayout = imageViewPager.findViewWithTag(position);
        if (currentLayout == null) {
            ViewCompat.postOnAnimation(imageViewPager, new Runnable() {

                @Override
                public void run() {
                    updateCurrentImageView(position);
                }
            });
            return;
        }
        ;
        image = (BaseZoomableImageView) currentLayout.findViewById(R.id.watch_image_view);
    }

    // 若图片已下载，直接显示图片；若图片未下载，则下载图片
    private void requestOriImage(IMMessage msg) {
        if (isOriginImageHasDownloaded(msg)) {
            onDownloadSuccess(msg);
            return;
        }

        // async download original image
        onDownloadStart(msg);
        message = msg; // 下载成功之后，判断是否是同一条消息时需要使用
        downloadFuture = NIMClient.getService(MsgService.class).downloadAttachment(msg, false);
    }

    private boolean isOriginImageHasDownloaded(final IMMessage message) {
        if (message.getAttachStatus() == AttachStatusEnum.transferred &&
                !TextUtils.isEmpty(((ImageAttachment) message.getAttachment()).getPath())) {
            return true;
        }

        return false;
    }

    /**
     * ******************************** 设置图片 *********************************
     */

    private void setThumbnail(IMMessage msg) {
        String thumbPath = ((ImageAttachment) msg.getAttachment()).getThumbPath();
        String path = ((ImageAttachment) msg.getAttachment()).getPath();

        Bitmap bitmap = null;
        if (!TextUtils.isEmpty(thumbPath)) {
            bitmap = BitmapDecoder.decodeSampledForDisplay(thumbPath);
            bitmap = ImageUtil.rotateBitmapInNeeded(thumbPath, bitmap);
        } else if (!TextUtils.isEmpty(path)) {
            bitmap = BitmapDecoder.decodeSampledForDisplay(path);
            bitmap = ImageUtil.rotateBitmapInNeeded(path, bitmap);
        }

        if (bitmap != null) {
            image.setImageBitmap(bitmap);
            return;
        }

        image.setImageBitmap(ImageUtil.getBitmapFromDrawableRes(getImageResOnLoading()));
    }

    private void setImageView(final IMMessage msg) {
        String path = ((ImageAttachment) msg.getAttachment()).getPath();
        if (TextUtils.isEmpty(path)) {
            image.setImageBitmap(ImageUtil.getBitmapFromDrawableRes(getImageResOnLoading()));
            return;
        }

        Bitmap bitmap = BitmapDecoder.decodeSampledForDisplay(path, false);
        bitmap = ImageUtil.rotateBitmapInNeeded(path, bitmap);
        if (bitmap == null) {
            Toast.makeText(this, R.string.picker_image_error, Toast.LENGTH_LONG).show();
            image.setImageBitmap(ImageUtil.getBitmapFromDrawableRes(getImageResOnFailed()));
        } else {
            image.setImageBitmap(bitmap);
        }
    }

    private int getImageResOnLoading() {
        return R.drawable.nim_image_default;
    }

    private int getImageResOnFailed() {
        return R.drawable.nim_image_download_failed;
    }

    /**
     * ********************************* 下载 ****************************************
     */

    private void registerObservers(boolean register) {
        NIMClient.getService(MsgServiceObserve.class).observeMsgStatus(statusObserver, register);
    }

    private Observer<IMMessage> statusObserver = new Observer<IMMessage>() {
        @Override
        public void onEvent(IMMessage msg) {
            if (!msg.isTheSame(message) || isDestroyedCompatible()) {
                return;
            }
            if (isOriginImageHasDownloaded(msg)) {
                onDownloadSuccess(msg);
            } else if (msg.getAttachStatus() == AttachStatusEnum.fail) {
                onDownloadFailed();
            }
        }
    };

    private void onDownloadStart(final IMMessage msg) {
        setThumbnail(msg);
        if (TextUtils.isEmpty(((ImageAttachment) msg.getAttachment()).getPath())) {
            loadingLayout.setVisibility(View.VISIBLE);
        } else {
            loadingLayout.setVisibility(View.GONE);
        }
    }

    private void onDownloadSuccess(final IMMessage msg) {
        loadingLayout.setVisibility(View.GONE);
        handler.post(new Runnable() {

            @Override
            public void run() {
                setImageView(msg);
            }
        });
    }

    private void onDownloadFailed() {
        loadingLayout.setVisibility(View.GONE);
        image.setImageBitmap(ImageUtil.getBitmapFromDrawableRes(getImageResOnFailed()));
        Toast.makeText(this, R.string.download_picture_fail, Toast.LENGTH_LONG).show();
    }

    /**
     * ***********************************图片点击事件*******************************************
     */

    // 设置图片点击事件
    protected void onImageViewFound(BaseZoomableImageView imageView) {
        imageView.setImageGestureListener(new ImageGestureListener() {

            @Override
            public void onImageGestureSingleTapConfirmed() {
                onImageViewTouched();
            }

            @Override
            public void onImageGestureLongPress() {
                showWatchPictureAction();
            }

            @Override
            public void onImageGestureFlingDown() {
                finish();
            }
        });
    }

    // 图片单击
    protected void onImageViewTouched() {
        finish();
    }

    // 图片长按
    protected void showWatchPictureAction() {
        if (alertDialog.isShowing()) {
            alertDialog.dismiss();
            return;
        }
        alertDialog.clearData();
        String path = ((ImageAttachment) message.getAttachment()).getThumbPath();
        if (TextUtils.isEmpty(path)) {
            return;
        }
        String title;
        if (!TextUtils.isEmpty(((ImageAttachment) message.getAttachment()).getPath())) {
            title = getString(R.string.save_to_device);
            alertDialog.addItem(title, new onSeparateItemClickListener() {

                @Override
                public void onClick() {
                    savePicture();
                }
            });
        }
        alertDialog.show();
    }

    // 保存图片
    public void savePicture() {
        ImageAttachment attachment = (ImageAttachment) message.getAttachment();
        String path = attachment.getPath();
        if (TextUtils.isEmpty(path)) {
            return;
        }

        String srcFilename = attachment.getFileName();
        //默认jpg
        String extension = TextUtils.isEmpty(attachment.getExtension()) ? "jpg" : attachment.getExtension();
        srcFilename += ("." + extension);

        String picPath = StorageUtil.getSystemImagePath();
        String dstPath = picPath + srcFilename;
        if (AttachmentStore.copy(path, dstPath) != -1) {
            try {
                ContentValues values = new ContentValues(2);
                values.put(MediaStore.Images.Media.MIME_TYPE, C.MimeType.MIME_JPEG);
                values.put(MediaStore.Images.Media.DATA, dstPath);
                getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                Toast.makeText(WatchMessagePictureActivity.this, getString(R.string.picture_save_to), Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                // may be java.lang.UnsupportedOperationException
                Toast.makeText(WatchMessagePictureActivity.this, getString(R.string.picture_save_fail), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(WatchMessagePictureActivity.this, getString(R.string.picture_save_fail), Toast.LENGTH_LONG).show();
        }
    }
}
