package com.example.diepzeepresentatie;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.GoToBuilder;
import com.aldebaran.qi.sdk.builder.HolderBuilder;
import com.aldebaran.qi.sdk.builder.LookAtBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.builder.TransformBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.object.actuation.Actuation;
import com.aldebaran.qi.sdk.object.actuation.Frame;
import com.aldebaran.qi.sdk.object.actuation.FreeFrame;
import com.aldebaran.qi.sdk.object.actuation.GoTo;
import com.aldebaran.qi.sdk.object.actuation.LookAt;
import com.aldebaran.qi.sdk.object.actuation.LookAtMovementPolicy;
import com.aldebaran.qi.sdk.object.actuation.Mapping;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.geometry.Transform;
import com.aldebaran.qi.sdk.object.holder.AutonomousAbilitiesType;
import com.aldebaran.qi.sdk.object.holder.Holder;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks {

    //Globale variabele
    Button presButton;
    QiContext qiContext;
    Say welcomeSay = null;
    GoTo welcomeGoto = null;
    Say chairs = null;
    Say tables = null;
    Say doors = null;
    Say wallart = null;
    Say activefloor = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this);
        //XML file koppelen
        setContentView(R.layout.activity_main);
        //Button aanmaken
        presButton = findViewById(R.id.presButton);

        //Button actie koppelen
        presButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        presentation();
                    }
                }
        );
    }

    @Override
    protected void onDestroy() {
        // Unregister the RobotLifecycleCallbacks for this Activity.
        QiSDK.unregister(this, this);
        super.onDestroy();
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        //Disable autonomous
        Holder holder = HolderBuilder.with(qiContext)
                .withAutonomousAbilities(
                        AutonomousAbilitiesType.BASIC_AWARENESS
                )
                .build();

        // Hold the abilities asynchronously.
        Future<Void> holdFuture = holder.async().hold();
        // The robot focus is gained.
        this.qiContext = qiContext;
        welcomeSay = talk("hi, welcome to the presentation of the deapsea room. The presentation will be in English because I can not speak Dutch");
        //Naar voren
        welcomeGoto = move(2.5,0);
        //vertellen, hiertussen komt dus de trajectories
        chairs = talk("In the right corner you will see some lovely green chairs!");
        tables = talk("The wooden tables in this room can be used to do some work");
        doors = talk("This room has 3 bright yellow doors, and one glass door behind me");
        wallart = talk("On the walls you can see coral like art, it is meant to look like deapsee. Which is of course the name of this room");
        activefloor = talk("Above us you can see a beamer, this is for learning purposes.");
    }

    @Override
    public void onRobotFocusLost() {
        // The robot focus is lost.
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        // The robot focus is refused.
    }

    //De presentatie
    //De tijden in talkExe bijvoorbeeld worden niet bij elkaar opgeteld, het zijn allemaal losse timers
    //Het is allemaal hardcoded, niet heel netjes maar het werkt
    public void presentation(){
        //praten
        talkExe(welcomeSay, 0);
        //lopen
        moveExe(welcomeGoto, 2000);
        //praten, hiertussen komen dus de trajectories
        //trajectories gebrabbel
        //trajectories gebrabbel
        //trajectories gebrabbel
        //trajectories gebrabbel
        //trajectories gebrabbel
        talkExe(chairs, 8000);
        talkExe(tables, 13000);
        talkExe(doors, 20000);
        talkExe(wallart, 27000);
        talkExe(activefloor, 35000);

    }

    //Functie om een say aan te maken
    public Say talk(String text){
        return SayBuilder.with(qiContext).withText(text).build();
    }

    //Functie om een say uit te voeren, met de delay die het nodig heeft
    public void talkExe(Say say, int miliseconds){
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                say.async().run();
            }
        }, miliseconds);
    }

    //Functie om een move aan te maken
    private GoTo move(double X, double Y){
        //Coordinaten doorgeven
        Transform transform = TransformBuilder.create().from2DTranslation(X,Y);
        Mapping mapping = qiContext.getMapping();
        //Een frame maken
        FreeFrame targetFrame = mapping.makeFreeFrame();
        //De robot plaats krijgen
        Actuation actuation = qiContext.getActuation();
        Frame robotFrame = actuation.robotFrame();
        //De beweging maken
        targetFrame.update(robotFrame, transform, 0L);
        GoTo goTo = GoToBuilder.with(qiContext).withFrame(targetFrame.frame()).build();
        return goTo;
    }

    //Functie om een move uit te voeren, met de delay die het nodig heeft
    public void moveExe(GoTo goTo, int miliseconds){
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                goTo.async().run();
            }
        }, miliseconds);
    }

    //Functie om look te maken
    private LookAt look(double X, double Y){
        //Coordinaten doorgeven
        Transform transform = TransformBuilder.create().from2DTranslation(X,Y);
        Mapping mapping = qiContext.getMapping();
        //Een frame maken
        FreeFrame targetFrame = mapping.makeFreeFrame();
        //De robot plaats krijgen
        Actuation actuation = qiContext.getActuation();
        Frame robotFrame = actuation.robotFrame();
        //De look functie maken
        targetFrame.update(robotFrame, transform, 0L);
        LookAt lookAt = LookAtBuilder.with(qiContext).withFrame(targetFrame.frame()).build();
        lookAt.setPolicy(LookAtMovementPolicy.HEAD_ONLY);
        return lookAt;
    }

    //Functie om een lookAt uit te voeren, met de delay die het nodig heeft
    public void lookExe(LookAt lookAt, int miliseconds){
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                lookAt.async().run();
            }
        }, miliseconds);
    }

    private LookAt turn(double X, double Y){
        //Coordinaten doorgeven
        Transform transform = TransformBuilder.create().from2DTranslation(X,Y);
        Mapping mapping = qiContext.getMapping();
        //Een frame maken
        FreeFrame targetFrame = mapping.makeFreeFrame();
        //De robot plaats krijgen
        Actuation actuation = qiContext.getActuation();
        Frame robotFrame = actuation.robotFrame();
        //De look functie maken
        targetFrame.update(robotFrame, transform, 0L);
        LookAt lookAt = LookAtBuilder.with(qiContext).withFrame(targetFrame.frame()).build();
        lookAt.setPolicy(LookAtMovementPolicy.HEAD_AND_BASE);
        return lookAt;
    }

}