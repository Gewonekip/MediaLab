package com.example.interactionmenu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import com.aldebaran.qi.Consumer;
import com.aldebaran.qi.Future;
import com.aldebaran.qi.Promise;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.LocalizeAndMapBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.object.actuation.ExplorationMap;
import com.aldebaran.qi.sdk.object.actuation.LocalizationStatus;
import com.aldebaran.qi.sdk.object.actuation.LocalizeAndMap;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.util.FutureUtils;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks {

    //Global variables
    private QiContext qiContext;
    private ExplorationMap initialExplorationMap = null;
    private ImageView imageView1;
    private Button leftButton;
    private Button rightButton;
    private Say welcomeSay = null;
    private Say byeSay = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this);
        //connecting the UI
        setContentView(R.layout.activity_main);
        imageView1 = findViewById(R.id.imageView1);
        leftButton =  findViewById(R.id.button1);
        rightButton =  findViewById(R.id.button2);
        //LeftButton action
        leftButton.setOnClickListener(ignored -> {
            buttonSwitcher();
            welcomeSay.async().run();

        });
        //rightButton action
        rightButton.setOnClickListener(ignored -> {
            buttonSwitcher();
            byeSay.async().run();
        });
    }

    @Override
    protected void onDestroy() {
        // Unregister the RobotLifecycleCallbacks for this Activity.
        QiSDK.unregister(this, this);
        super.onDestroy();
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        // The robot focus is gained.
        this.qiContext = qiContext;
        runOnUiThread(() -> leftButton.setEnabled(true));
        runOnUiThread(() -> rightButton.setEnabled(false));
        welcomeSay = talk("Welcome!");
        byeSay = talk("Bye!");
    }

    @Override
    public void onRobotFocusLost() {
        // The robot focus is lost.
        this.qiContext = null;
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        // The robot focus is refused.
    }

    private void buttonSwitcher(){
        leftButton.setEnabled(!leftButton.isEnabled());
        rightButton.setEnabled(!rightButton.isEnabled());
    }

    private Say talk(String text){
        return SayBuilder.with(qiContext).withText(text).build();
    }
}