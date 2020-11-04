package com.example.pepper4;


import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.GoToBuilder;
import com.aldebaran.qi.sdk.builder.LocalizeAndMapBuilder;
import com.aldebaran.qi.sdk.builder.LookAtBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.builder.TransformBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayStrategy;
import com.aldebaran.qi.sdk.object.actuation.Actuation;
import com.aldebaran.qi.sdk.object.actuation.ExplorationMap;
import com.aldebaran.qi.sdk.object.actuation.Frame;
import com.aldebaran.qi.sdk.object.actuation.FreeFrame;
import com.aldebaran.qi.sdk.object.actuation.GoTo;
import com.aldebaran.qi.sdk.object.actuation.LocalizationStatus;
import com.aldebaran.qi.sdk.object.actuation.LocalizeAndMap;
import com.aldebaran.qi.sdk.object.actuation.LookAt;
import com.aldebaran.qi.sdk.object.actuation.LookAtMovementPolicy;
import com.aldebaran.qi.sdk.object.actuation.MapTopGraphicalRepresentation;
import com.aldebaran.qi.sdk.object.actuation.Mapping;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.geometry.Transform;
import com.softbankrobotics.pepperpointat.PointAtAnimator;

public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks {

    //hulpvariabele
    private QiContext qiContext1;
    private ImageView image;
    private GoTo goTo;
    private LookAt lookAt;
    private static final String TAG = "MainActivity";
    private Actuation actuation;
    private Frame robotFrame;
    private Future<Void> lookAtFuture;
    private PointAtAnimator pointAtAnimator;
    private LocalizeAndMap localizeAndMap;
    private Future localizingAndMapping;
    private ExplorationMap explorationMap;

    //Dit wordt geladen als de app wordt gecreeerd
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QiSDK.register(this, this);
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.OVERLAY);
        //dit koppelt de ui
        setContentView(R.layout.activity_main);
    }

    //Dit wordt geladen als de app is afgesloten
    @Override
    protected void onDestroy(){
        QiSDK.unregister(this, this);
        super.onDestroy();
    }

    //Dit wordt geladen als de app wordt opgestart
    @Override
    public void onRobotFocusGained(QiContext qiContext){
        //Dit vult de hulp variabele in die we later nodig hebben
        qiContext1 = qiContext;
        actuation = qiContext.getActuation();
        //Dit geeft de locatie van de robot door
        robotFrame = actuation.robotFrame();
        //pointAtAnimator = PointAtAnimator(qiContext);
        mapStart();
    }


    @Override
    //Als de app wordt afgelosten worden ook alle listerens weggedaan
    public void onRobotFocusLost(){
        if (goTo != null) {
            goTo.removeAllOnStartedListeners();
        }
        if (lookAt != null) {
            lookAt.removeAllOnStartedListeners();
        }
    }

    @Override
    public void onRobotFocusRefused(String reason){

    }

    //Dit is een functie om de robot te bewegen
    private void move(double distanceX, double distanceY){
        //De coordinaten doorgeven
        Transform transform = TransformBuilder.create().from2DTranslation(distanceX, distanceY);
        //de ruimte bekijken
        Mapping mapping = qiContext1.getMapping();
        //een frame maken
        FreeFrame targetFrame = mapping.makeFreeFrame();
        //deze frame updaten met de transformatie en de huidige positie van de robot
        targetFrame.update(robotFrame, transform, 0L);
        // Create a GoTo action.
        goTo = GoToBuilder.with(qiContext1) // Create the builder with the QiContext.
                .withFrame(targetFrame.frame()) // Set the target frame.
                .build(); // Build the GoTo action.
        goTo.addOnStartedListener(() -> Log.i(TAG, "GoTo action started."));
        Future<Void> goToFuture = goTo.async().run();
        goToFuture.thenConsume(future -> {
            if (future.isSuccess()) {
                Log.i(TAG, "GoTo action finished with success.");
            } else if (future.hasError()) {
                Log.e(TAG, "GoTo action finished with error.", future.getError());
            }
        });
    }

    //Hetzelfde als de move functie, maar dan om rond te kijken
    private void lookAt(double distanceX, double distanceY){
        Transform transform = TransformBuilder.create().from2DTranslation(distanceX, distanceY);
        Mapping mapping = qiContext1.getMapping();
        FreeFrame targetFrame = mapping.makeFreeFrame();
        targetFrame.update(robotFrame, transform, 0L);
        // Create a GoTo action.
        lookAt = LookAtBuilder.with(qiContext1) // Create the builder with the QiContext.
                .withFrame(targetFrame.frame()) // Set the target frame.
                .build(); // Build the GoTo action.
        lookAt.setPolicy(LookAtMovementPolicy.HEAD_ONLY);
        lookAt.addOnStartedListener(() -> Log.i(TAG, "LookAt action started."));
        lookAtFuture = lookAt.async().run();
        lookAtFuture.thenConsume(future -> {
            if (future.isSuccess()) {
                Log.i(TAG, "LookAt action finished with success.");
            } else if (future.hasError()) {
                Log.e(TAG, "LookAt action finished with error.", future.getError());
            }
        });
    }

    //zorgen dat de robot niet meer rond kijkt
    private void stopLookAt(){
        lookAtFuture.requestCancellation();
    }

    //de presentatie, die verder moet worden gebouwd
    private void presentation(){
        sayText("I'll now present the deepsea room, follow me!");
        pause(1000);
        move(2,2);
        pause(2000);
        lookAt(0.5,0.5);
        pause(3000);
        stopLookAt();
    }


    //de robot laten praten
    private void sayText(String text){
        Say say = SayBuilder.with(qiContext1)
                .withText(text)
                .build();
        say.run();
    }

    //de robot laten wachten
    private void pause(int time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //een beeld neer zetten
    private void setImage(int resource) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                image.setImageResource(resource);
                image.setVisibility(View.VISIBLE);
            }
        };
        runOnUiThread(runnable);
    }

    //een beeld weg laten halen
    private void clearImage(){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                image.setVisibility(View.INVISIBLE);
            }
        };
        runOnUiThread(runnable);
    }

    private void mapStart(){
        localizeAndMap = LocalizeAndMapBuilder.with(qiContext1).build();
        localizeAndMap.async().run();
        localizeAndMap.addOnStatusChangedListener(localizationStatus -> {
            if (localizationStatus == LocalizationStatus.LOCALIZED) {
                // Stop the action.
                localizingAndMapping.requestCancellation();
                // Dump the map for future use by a Localize action.
                explorationMap = localizeAndMap.dumpMap();
            }
        });
        localizingAndMapping = localizeAndMap.async().run();
    }

    private void mapPrinter(){
        
    }


}