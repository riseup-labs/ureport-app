package io.rapidpro.surveyor.activity;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.greysonparrelli.permiso.Permiso;
import com.nyaruka.goflow.mobile.Environment;
import com.nyaruka.goflow.mobile.Event;
import com.nyaruka.goflow.mobile.Hint;
import com.nyaruka.goflow.mobile.MsgIn;
import com.nyaruka.goflow.mobile.Resume;
import com.nyaruka.goflow.mobile.SessionAssets;
import com.nyaruka.goflow.mobile.Trigger;

import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import io.rapidpro.surveyor.Logger;
import io.rapidpro.surveyor.R;
import io.rapidpro.surveyor.SurveyorIntent;
import io.rapidpro.surveyor.SurveyorPreferences;
import io.rapidpro.surveyor.data.Flow;
import io.rapidpro.surveyor.data.Org;
import io.rapidpro.surveyor.data.Submission;
import io.rapidpro.surveyor.engine.Contact;
import io.rapidpro.surveyor.engine.Engine;
import io.rapidpro.surveyor.engine.EngineException;
import io.rapidpro.surveyor.engine.Session;
import io.rapidpro.surveyor.engine.Sprint;
import io.rapidpro.surveyor.ui.IconTextView;
import io.rapidpro.surveyor.ui.ViewCache;
import io.rapidpro.surveyor.utils.ImageUtils;
import io.rapidpro.surveyor.widget.ChatBubbleView;
import io.rapidpro.surveyor.widget.IconLinkView;

import static io.rapidpro.surveyor.extend.StaticMethods.getMD5;
import static io.rapidpro.surveyor.extend.StaticMethods.playNotification;

public class RunActivity extends BaseActivity {

    // the different types of requests for media
    public static final String REQUEST_IMAGE = "image";
    public static final String REQUEST_AUDIO = "audio";
    public static final String REQUEST_VIDEO = "video";
    public static final String REQUEST_GPS = "geo";

    // custom request codes passed to media capture activities
    private static final int RESULT_IMAGE = 1;
    private static final int RESULT_VIDEO = 2;
    private static final int RESULT_AUDIO = 3;
    private static final int RESULT_GPS = 4;

    private static final int MAX_IMAGE_DIMENSION = 1024;
    private static final int MAX_THUMB_DIMENSION = 600;

    private LinearLayout chatHistory;
    private IconTextView sendButtom;
    private EditText chatCompose;
    private ScrollView scrollView;

    private Session session;
    private Submission submission;
    Org org = null;
    Flow flow = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String orgUUID = getIntent().getStringExtra(SurveyorIntent.EXTRA_ORG_UUID);
        String flowUUID = getIntent().getStringExtra(SurveyorIntent.EXTRA_FLOW_UUID);

        setContentView(R.layout.activity_run);
//        changeTitle("Survey");
        initUI();

        findViewById(R.id.back_button).setOnClickListener(view -> {
            playNotification(getSurveyor(), getApplicationContext(), R.raw.button_click_no, view);
            onBackPressed();
        });

//        menuItems.add(new PowerMenuItem(getString(R.string.action_bug_report)));
//        menuItems.add(new PowerMenuItem(getString(R.string.action_settings)));
//        menuItems.add(new PowerMenuItem(getString(R.string.action_logout)));
//        initHeaderBar();

        try {
            org = getSurveyor().getOrgService().get(orgUUID);

            // Set Language
            String pref_lang = getSurveyor().getPreferences().getString(SurveyorPreferences.LANG_CODE, "en");
            String lang_code;
            switch(pref_lang){
                case "en": lang_code = "eng";break;
                case "bn": lang_code = "ben";break;
                case "my": lang_code = "mya";break;
                default: lang_code = "eng";break;
            }

            List<String> languages = Arrays.asList(org.getLanguages());
            if(languages.contains(lang_code)){
                org.setPrimaryLanguage(lang_code);
            }else{
                org.setPrimaryLanguage(org.initialLanguage);
            }


            SessionAssets assets = Engine.createSessionAssets(Engine.loadAssets(org.getAssets()));
            Environment environment = Engine.createEnvironment(org);

            flow = org.getFlow(flowUUID);
            setTitle(flow.getName());

            changeTitle(flow.getName());

            Trigger trigger = Engine.createManualTrigger(environment, Contact.createEmpty(assets), flow.toReference());

            Pair<Session, Sprint> ss = Engine.getInstance().newSession(assets, trigger);
            session = ss.getLeft();
            submission = getSurveyor().getSubmissionService().newSubmission(org, flow);

            handleEngineSprint(ss.getRight());

        } catch (EngineException | IOException e) {
            handleProblem("Unable to start flow", e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        initUI();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks whether a hardware keyboard is available
        if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) {
            scrollView.smoothScrollTo(0, scrollView.getBottom());
        } else if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
            scrollView.smoothScrollTo(0, scrollView.getBottom());
        }
    }

    private void initUI() {
        chatHistory = findViewById(R.id.chat_history);
        chatCompose = findViewById(R.id.chat_compose);
        sendButtom = findViewById(R.id.button_send);
        scrollView = findViewById(R.id.scroll);


        // allow messages to be sent with the enter key
        chatCompose.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {

                    onActionSend(sendButtom);
                    return true;
                }
                return false;
            }
        });

        // or the send button on the keyboard
        chatCompose.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null && actionId == EditorInfo.IME_ACTION_SEND && event.getAction() == KeyEvent.ACTION_DOWN) {
                    onActionSend(sendButtom);
                    return true;
                }
                return false;
            }
        });

        // change the color of the send button when there is text in the compose box
        chatCompose.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    sendButtom.setIconColor(R.color.magenta);
                } else {
                    sendButtom.setIconColor(R.color.light_gray);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    public void onBackPressed() {
        confirmDiscardRun();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_run, menu);
        return true;
    }

    /**
     * User pressed the media request button
     */
    public void onActionMedia(View view) {
        View media = getViewCache().getView(R.id.media_icon);
        if (session.isWaiting()) {
            if (REQUEST_IMAGE.equals(media.getTag())) {
                captureImage();
            } else if (REQUEST_VIDEO.equals(media.getTag())) {
                captureVideo();
            } else if (REQUEST_AUDIO.equals(media.getTag())) {
                captureAudio();
            } else if (REQUEST_GPS.equals(media.getTag())) {
                captureLocation();
            }
        }
    }

    /**
     * Captures an image from the camera
     */
    private void captureImage() {

        Permiso.getInstance().requestPermissions(new Permiso.IOnPermissionResult() {
            @Override
            @SuppressWarnings("ResourceType")
            public void onPermissionResult(Permiso.ResultSet resultSet) {
                if (resultSet.areAllPermissionsGranted()) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    ComponentName cameraPkg = intent.resolveActivity(getPackageManager());

                    if (cameraPkg == null) {
                        handleProblem("Can't find camera device", null);
                        return;
                    }
                    Logger.d("Camera package is " + cameraPkg.toString());

                    File cameraOutput = getCameraOutput();
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, getSurveyor().getUriForFile(cameraOutput));
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    startActivityForResult(intent, RESULT_IMAGE);
                }
            }

            @Override
            public void onRationaleRequested(Permiso.IOnRationaleProvided callback, String... permissions) {
                RunActivity.this.showRationaleDialog(R.string.permission_camera, callback);
            }

        }, Manifest.permission.CAMERA);
    }

    /**
     * Captures a video from the camera
     */
    private void captureVideo() {
        Intent intent = new Intent(this, CaptureVideoActivity.class);
        intent.putExtra(SurveyorIntent.EXTRA_MEDIA_FILE, getVideoOutput().getAbsolutePath());
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        startActivityForResult(intent, RESULT_VIDEO);
    }

    /**
     * Captures an audio recording from the microphone
     */
    private void captureAudio() {
        Permiso.getInstance().requestPermissions(new Permiso.IOnPermissionResult() {
            @Override
            @SuppressWarnings("ResourceType")
            public void onPermissionResult(Permiso.ResultSet resultSet) {
                if (resultSet.areAllPermissionsGranted()) {
                    Intent intent = new Intent(RunActivity.this, CaptureAudioActivity.class);
                    intent.putExtra(SurveyorIntent.EXTRA_MEDIA_FILE, getAudioOutput().getAbsolutePath());
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    startActivityForResult(intent, RESULT_AUDIO);
                }
            }

            @Override
            public void onRationaleRequested(Permiso.IOnRationaleProvided callback, String... permissions) {
                RunActivity.this.showRationaleDialog(R.string.permission_record, callback);
            }

        }, Manifest.permission.RECORD_AUDIO);
    }

    /**
     * Captures the current location
     */
    private void captureLocation() {
        Intent intent = new Intent(this, CaptureLocationActivity.class);
        startActivityForResult(intent, RESULT_GPS);
    }

    private File getCameraOutput() {
        return new File(getSurveyor().getExternalCacheDir(), "camera.jpg");
    }

    private File getVideoOutput() {
        return new File(getSurveyor().getExternalCacheDir(), "video.mp4");
    }

    private File getAudioOutput() {
        return new File(getSurveyor().getExternalCacheDir(), "audio.m4a");
    }

    /**
     * @see android.app.Activity#onActivityResult(int, int, Intent)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        MsgIn msg = null;
        try {
            if (requestCode == RESULT_IMAGE) {
                File output = getCameraOutput();
                if (output.exists()) {
                    Bitmap full = BitmapFactory.decodeFile(output.getAbsolutePath());
                    Bitmap scaled = ImageUtils.scaleToMax(full, MAX_IMAGE_DIMENSION);
                    // correct rotation if necessary
                    int rotation = ImageUtils.getExifRotation(output.getAbsolutePath());
                    if (rotation != 0) {
                        Logger.d("Correcting EXIF rotation of " + rotation + " degrees");
                        scaled = ImageUtils.rotateImage(scaled, rotation);
                    }
                    // encode as JPEG and save to submission
                    byte[] asJpg = ImageUtils.convertToJPEG(scaled);
                    Uri uri = submission.saveMedia(asJpg, "jpg");
                    Logger.d("Saved image capture to " + uri);
                    // create thumbnail and add to chat
                    Bitmap thumb = ImageUtils.scaleToMax(scaled, MAX_THUMB_DIMENSION);
                    addMedia(thumb, uri.toString(), R.string.media_image);
                    msg = Engine.createMsgIn("", "image/jpeg:" + uri);
                    output.delete();
                }
            } else if (requestCode == RESULT_VIDEO) {
                File output = getVideoOutput();
                if (output.exists()) {
                    Bitmap thumb = ImageUtils.thumbnailFromVideo(output);
                    Uri uri = submission.saveMedia(output);
                    addMedia(thumb, uri.toString(), R.string.media_video);
                    Logger.d("Saved video capture to " + uri);
                    msg = Engine.createMsgIn("", "video/mp4:" + uri);
                    output.delete();
                }

            } else if (requestCode == RESULT_AUDIO) {
                File output = getAudioOutput();
                if (output.exists()) {
                    Uri uri = submission.saveMedia(output);
                    Logger.d("Saved audio capture to " + uri);
                    addMediaLink(getString(R.string.made_recording), uri.toString(), R.string.media_audio);
                    msg = Engine.createMsgIn("", "audio/mp4:" + uri);
                    output.delete();
                }
            } else if (requestCode == RESULT_GPS) {
                double latitude = data.getDoubleExtra("latitude", 0.0);
                double longitude = data.getDoubleExtra("longitude", 0.0);
                String coords = "geo:" + latitude + "," + longitude;
                String url = coords + "?q=" + latitude + "," + longitude + "(Location)";
                addMediaLink(latitude + "," + longitude, url, R.string.media_location);
                msg = Engine.createMsgIn("", coords);
            }
        } catch (IOException e) {
            handleProblem("Unable capture media", e);
        }
        // if we have a message we can try to resume now...
        if (msg != null) {
            resumeSession(msg);
        }
    }

    /**
     * Something has gone wrong... show the user the big report dialog
     */
    private void handleProblem(String toastMsg, Throwable e) {
        Toast.makeText(this, toastMsg, Toast.LENGTH_SHORT).show();

        if (e != null) {
            Logger.e("Error running flow", e);
            showBugReportDialog();
        }

        finish();
    }

    private void resumeSession(MsgIn msg) {
        try {
            Resume resume = Engine.createMsgResume(null, null, msg);
            Sprint sprint = session.resume(resume);

            handleEngineSprint(sprint);

        } catch (EngineException | IOException e) {
            handleProblem("Couldn't handle message", e);
        }

        // scroll us to the bottom
        scrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                scrollView.setSmoothScrollingEnabled(true);
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);

                // put the focus back on the chat box
                chatCompose.requestFocus();
            }
        }, 100);

        // refresh our menu
        invalidateOptionsMenu();
    }

    /**
     * User pressed the send button
     */
    public void onActionSend(View sendButton) {
        if (!session.getStatus().equals("waiting")) {
            return;
        }

        // Remove All Quick Response
        getViewCache().hide(R.id.quick_replies);
        ((LinearLayout) findViewById(R.id.quick_replies)).removeAllViews();
        getViewCache().show(R.id.chat_box);

        EditText chatBox = findViewById(R.id.chat_compose);
        String message = chatBox.getText().toString();
        hideKeyboard(this);

        if (message.trim().length() > 0){
            chatBox.setText("");
            final MsgIn msg = Engine.createMsgIn(message);
            addMessage(message, true);
            playNotification(getSurveyor(), getApplicationContext(), R.raw.send_message_sound);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    resumeSession(msg);
                    playNotification(getSurveyor(), getApplicationContext(), R.raw.receive_message_sound);
                }
            }, (int)(Math.random() * 1500 + 700));


        }
    }


    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * Handles new session state and events after interaction with the flow engine
     *
     * @param sprint the sprint from the engine
     */
    private void handleEngineSprint(Sprint sprint) throws IOException, EngineException {
        for (Event event : sprint.getEvents()) {
            Logger.d("Event: " + event.payload());

            JsonObject asObj = new JsonParser().parse(event.payload()).getAsJsonObject();

            if (event.type().equals("msg_created")) {
                final JsonObject msg = asObj.get("msg").getAsJsonObject();
                addMessage(msg.get("text").getAsString(), false);

                if(msg.get("quick_replies") != null){
                    JsonArray quick_replies = msg.get("quick_replies").getAsJsonArray();
                    LinearLayout quick_reply_box = findViewById(R.id.quick_replies);

                    for(int i = 0; i < quick_replies.size(); i++) {
                        String reply_data = quick_replies.get(i).getAsString();

                        View quickTemplate = LayoutInflater.from(this).inflate(R.layout.v1_quick_reply_button, null);

                        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 38, getResources().getDisplayMetrics());
                        int space = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, getResources().getDisplayMetrics());
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height);
                        layoutParams.setMargins(0, space, 0, space);

                        ((Button) quickTemplate).setText(reply_data);
                        quick_reply_box.addView(quickTemplate, layoutParams);
                    }

                    getViewCache().show(R.id.quick_replies);
                }

                if(msg.get("attachments") != null){
                    JsonArray attachments = msg.get("attachments").getAsJsonArray();
                    for(int i = 0; i < attachments.size(); i++){
                        String attachment_data = attachments.get(i).getAsString();
                        int x = attachment_data.indexOf(":");
                        String attachment_uri = attachment_data.substring(x+1);
                        String attachment_type = attachment_data.substring(0, x);
                        final String file_name = "flow_asset_" + getMD5(attachment_uri);
                        final String file_path = org.getDirectory().getPath() + "/" + file_name;

                        switch(attachment_type){
                            case "image":
                            case "image/png":
                            case "image/jpg":
                            case "image/gif":
                            case "image/webp":
                            case "image/jpeg":
                                Glide.with(this)
                                        .asBitmap()
                                        .load(new File(org.getDirectory(), file_name))
                                        .into(new CustomTarget<Bitmap>() {
                                            @Override
                                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                                addMedia(resource, file_path, R.string.media_image);
                                            }

                                            @Override
                                            public void onLoadCleared(@Nullable Drawable placeholder) {}
                                        });
                                break;
                            case "audio":
                            case "audio/mpeg":
                            case "audio/mp3":
                                Glide.with(this)
                                        .asBitmap()
                                        .load(R.drawable.v1_audio_thumbnail)
                                        .into(new CustomTarget<Bitmap>() {
                                            @Override
                                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                                addMedia(resource, file_path, R.string.media_audio);
                                            }

                                            @Override
                                            public void onLoadCleared(@Nullable Drawable placeholder) {}
                                        });
                                break;
                            case "video":
                            case "video/mp4":
                                Glide.with(this)
                                        .asBitmap()
                                        .load(R.drawable.v1_video_thumbnail)
                                        .into(new CustomTarget<Bitmap>() {
                                            @Override
                                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                                addMedia(resource, file_path, R.string.media_video);
                                            }

                                            @Override
                                            public void onLoadCleared(@Nullable Drawable placeholder) {}
                                        });
                                break;
                        }
                    }

                }

            }
        }

        if (!session.isWaiting()) {
            addLogMessage(R.string.log_flow_complete);

            ViewCache cache = getViewCache();
            cache.hide(R.id.chat_box, true);
            cache.hide(R.id.container_request_media);
            cache.show(R.id.completed_session_actions);
        } else {
            waitForInput(session.getWait().hint());
        }

        submission.saveSession(session);
        submission.saveNewModifiers(sprint.getModifiers());
        submission.saveNewEvents(sprint.getEvents());

        Logger.d("Persisted new events and modifiers after engine sprint");
    }

    private void waitForInput(Hint hint) {
        ViewCache vc = getViewCache();
        TextView mediaButton = vc.getTextView(R.id.media_icon);
        TextView mediaText = vc.getTextView(R.id.media_text);

        String mediaType = hint != null ? hint.type() : "";
        switch (mediaType) {
            case "image":
                mediaButton.setText(getString(R.string.icon_photo_camera));
                mediaButton.setTag(REQUEST_IMAGE);
                mediaText.setText(getString(R.string.request_image));
                vc.hide(R.id.chat_box, true);
                vc.show(R.id.container_request_media);
                break;
            case "video":
                mediaButton.setText(getString(R.string.icon_videocam));
                mediaButton.setTag(REQUEST_VIDEO);
                mediaText.setText(getString(R.string.request_video));
                vc.hide(R.id.chat_box, true);
                vc.show(R.id.container_request_media);
                break;
            case "audio":
                mediaButton.setText(getString(R.string.icon_mic));
                mediaButton.setTag(REQUEST_AUDIO);
                mediaText.setText(getString(R.string.request_audio));
                vc.hide(R.id.chat_box, true);
                vc.show(R.id.container_request_media);
                break;
            case "location":
                mediaButton.setText(getString(R.string.icon_place));
                mediaButton.setTag(REQUEST_GPS);
                mediaText.setText(getString(R.string.request_gps));
                vc.hide(R.id.chat_box, true);
                vc.show(R.id.container_request_media);
                break;
            default:
                vc.show(R.id.chat_box);
                vc.hide(R.id.container_request_media);
                break;
        }
    }

    private void addLogMessage(int message) {
        getLayoutInflater().inflate(R.layout.item_log_message, chatHistory);
        TextView view = (TextView) chatHistory.getChildAt(chatHistory.getChildCount() - 1);
        view.setText(getString(message));
    }

    private void addMessage(String text, boolean inbound) {
        getLayoutInflater().inflate(R.layout.item_chat_bubble, chatHistory);
        ChatBubbleView bubble = (ChatBubbleView) chatHistory.getChildAt(chatHistory.getChildCount() - 1);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bubble.setTransitionName(getString(R.string.transition_chat));
        }

        bubble.setMessage(text, inbound);
        scrollToBottom();
    }

    private void addMedia(Bitmap image, String url, int type) {
        getLayoutInflater().inflate(R.layout.item_chat_bubble, chatHistory);
        ChatBubbleView bubble = (ChatBubbleView) chatHistory.getChildAt(chatHistory.getChildCount() - 1);
        bubble.setThumbnail(image, url, type);
        scrollToBottom();
    }

    private void addMediaLink(String title, String url, int type) {
        getLayoutInflater().inflate(R.layout.item_icon_link, chatHistory);
        IconLinkView icon = (IconLinkView) chatHistory.getChildAt(chatHistory.getChildCount() - 1);
        icon.initialize(title, type, url);
        scrollToBottom();
    }

    private void scrollToBottom() {
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    /**
     * User pressed the save button
     *
     * @param view the button
     */
    public void onActionSave(View view) {
        try {
            submission.complete();
            playNotification(getSurveyor(), getApplicationContext(), R.raw.button_click_yes);
            finish();
        } catch (IOException e) {
            Logger.e("unable to complete submission", e);
        }
    }

    public void onActionQuickReply(View view) {
        // Send Quick Reply
        sendButtom = findViewById(R.id.button_send);
        EditText chatBox = findViewById(R.id.chat_compose);
        String reply_text = ((Button) view).getText().toString();

        chatBox.setText(reply_text);
        onActionSend(sendButtom);


    }

    /**
     * User pressed the discard button - prompt user to confirm if they want to lose this submission
     *
     * @param view the button
     */
    public void onActionDiscard(View view) {
        playNotification(getSurveyor(), getApplicationContext(), R.raw.button_click_no);
        confirmDiscardRun();
    }

    /**
     * User pressed the cancel menu item - prompt user to confirm if they want to lose this submission
     *
     * @param item the menu item
     */
    public void onActionCancel(MenuItem item) {
        confirmDiscardRun();
    }

    private void confirmDiscardRun() {

        final Dialog dialog6 = new Dialog(this);
        dialog6.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog6.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog6.setContentView(R.layout.v1_dialog_ui);
        dialog6.findViewById(R.id.textSubText).setVisibility(View.GONE);
        ((TextView) dialog6.findViewById(R.id.textMainText)).setText(getString(R.string.confirm_submission_discard));
        ((TextView) dialog6.findViewById(R.id.button_yes_text)).setText("Yes");
        ((TextView) dialog6.findViewById(R.id.button_no_text)).setText("No");

        dialog6.findViewById(R.id.button_yes).setOnClickListener(view -> {
            playNotification(getSurveyor(), getApplicationContext(), R.raw.button_click_yes, view);
            submission.delete();
            dialog6.dismiss();
            finish();
        });

        dialog6.findViewById(R.id.button_no).setOnClickListener(view -> {

            playNotification(getSurveyor(), getApplicationContext(), R.raw.button_click_no, view);
            dialog6.dismiss();
        });

        dialog6.show();


    }

    public void onClickMedia(View view) {

        String url = (String) view.getTag(R.string.tag_url);
        int mediaType = (int) view.getTag(R.string.tag_media_type);

        // Intercept Media to Play Locally
        if(mediaType == R.string.media_video){
            playMediaDialog(url, R.string.media_video);
            return;
        }

        if(mediaType == R.string.media_audio){
            playMediaDialog(url, R.string.media_audio);
            return;
        }

        if(mediaType == R.string.media_image){
            displayImage(url);
            return;
        }

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        switch (mediaType) {
            case R.string.media_image:
                intent.setDataAndType(Uri.parse(url), "image/*");
                break;
            case R.string.media_video:
                intent.setDataAndType(Uri.parse(url), "video/*");
                break;
            case R.string.media_audio:
                intent.setDataAndType(Uri.parse(url), "audio/*");
                break;
            case R.string.media_location:
                intent.setDataAndType(Uri.parse(url), null);
                break;
        }

        startActivity(intent);
    }

    ExoPlayer exoPlayer;

    public void playMediaDialog(String media_path, int media_type) {
        final Dialog dialog = new Dialog(this);// add here your class name
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        if(media_type == R.string.media_audio){
            dialog.setContentView(R.layout.v1_audio_player_dialog);
        }else{
            dialog.setContentView(R.layout.v1_video_player_dialog);
        }

        dialog.setOnDismissListener(dialogInterface -> exoPlayer.release());
        dialog.show();

//        if(media_type == R.string.media_audio){
//            WindowManager.LayoutParams lp = new WindowManager.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//            lp.copyFrom(dialog.getWindow().getAttributes());
//            dialog.getWindow().setAttributes(lp);
//        }

        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        Log.v("Media-Path", media_path);

        ConcatenatingMediaSource concatenatingMediaSource = new ConcatenatingMediaSource();

        PlayerView playerView = dialog.findViewById(R.id.videoPlayer);
        ((SurfaceView) playerView.getVideoSurfaceView()).setZOrderOnTop(true);
        exoPlayer = ExoPlayerFactory.newSimpleInstance(this);
        playerView.setPlayer(exoPlayer);

        Uri uri = Uri.parse(media_path);
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, getApplicationInfo().name));
        MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
        concatenatingMediaSource.addMediaSource(mediaSource);

        exoPlayer.prepare(concatenatingMediaSource);
        exoPlayer.setPlayWhenReady(false);
    }


    public void displayImage(String image_path) {

        AlertDialog.Builder builder = new AlertDialog.Builder(RunActivity.this);

        //Yes Button
        builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        LayoutInflater inflater = getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.v1_imageview_popup, null);

        ImageView dialog_image = dialoglayout.findViewById(R.id.dialogImageView);

        Glide.with(this)
                .load(image_path)
                .into(dialog_image);

        builder.setView(dialoglayout);
        builder.show();

    }
}
