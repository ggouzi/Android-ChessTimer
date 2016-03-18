package com.mailexample.premiere_appli;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity{

    private MalibuCountDownTimer countDownTimer1, countDownTimer2;

    private boolean timer1isRunning = false;
    private boolean timer2isRunning = false;
    private boolean premierTap = true;
    private boolean vibration = true;
    private boolean sonFinPartie = true;
    private boolean sonTour = true;

    private String joueur1;
    private String joueur2;

    private int nbMove1 = 0;
    private int nbMove2 = 0;

    private View boutonTemps1, boutonTemps2;

    private Button pause_resume1, pause_resume2;
    private boolean paused = false;
    private boolean terminated = false;

    private TextView temps1, temps2, temps1bis, temps2bis, increment2, increment1, nomJoueur1, nomJoueur2, move1, move2;

    private ImageView tour;

    private long timeRemaining1, timeRemaining2;

    private final long startTime = 10000;
    private final long interval = 100;
    private final long increment = 2000;

    private final int NOIRS = 0;
    private final int BLANCS = 1;
    private int quiJoueApresPause = NOIRS;
    private int quiJoueAvantPause = BLANCS;
    private int quiAPerdu = NOIRS; // By default

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        joueur1 = getResources().getString(R.string.defaultPlayerName1);
        joueur2 = getResources().getString(R.string.defaultPlayerName2);

        boutonTemps1 = (View) this.findViewById(R.id.layoutTemps1);
        boutonTemps2 = (View) this.findViewById(R.id.layoutTemps2);

        temps1 = (TextView) this.findViewById(R.id.temps1);
        temps2 = (TextView) this.findViewById(R.id.temps2);
        temps1bis = (TextView) this.findViewById(R.id.temps1bis);
        temps2bis = (TextView) this.findViewById(R.id.temps2bis);
        increment2 = (TextView) this.findViewById(R.id.increment2);
        increment1 = (TextView) this.findViewById(R.id.increment1);
        nomJoueur1 = (TextView) this.findViewById(R.id.nomJoueur1);
        nomJoueur2 = (TextView) this.findViewById(R.id.nomJoueur2);

        move1 = (TextView) this.findViewById(R.id.nbMove1);
        move2 = (TextView) this.findViewById(R.id.nbMove2);

        pause_resume1 = (Button) this.findViewById(R.id.pause1);
        pause_resume2 = (Button) this.findViewById(R.id.pause2);
        tour = (ImageView) this.findViewById(R.id.tour);

        countDownTimer1 = new MalibuCountDownTimer(startTime, interval, temps1, temps1bis);
        countDownTimer2 = new MalibuCountDownTimer(startTime, interval, temps2, temps2bis);

        // To display the right format time by default
        countDownTimer1.displayTime(startTime);
        countDownTimer2.displayTime(startTime);

        updateMoves(nbMove1, move1);
        updateMoves(nbMove2, move2);

        displayOrGone();

        pause_resume1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pause();
            }
        });

        pause_resume2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pause();
            }
        });

        boutonTemps1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                playSoundTurn();
                if(!premierTap){
                    nbMove1++;
                    increment1.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.move_blancs));
                    timeRemaining1 = countDownTimer1.addIncAndGetTime(increment);
                    updateMoves(nbMove1, move1);
                }
                else if(premierTap){
                    premierTap=!premierTap;
                }

                playBlack();
            }
        });

        boutonTemps2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                playSoundTurn();
                if(!premierTap){
                    nbMove2++;
                    increment2.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.move_noirs));
                    timeRemaining2 = countDownTimer2.addIncAndGetTime(increment);
                    updateMoves(nbMove2, move2);
                }

                else if(premierTap){
                    premierTap=!premierTap;
                }

                playWhite();
            }
        });
    }

    public void displayOrGone(){
        if(increment==0){
            increment1.setVisibility(View.GONE);
            increment2.setVisibility(View.GONE);
        }
        else{
            increment1.setText("+"+increment/1000);
            increment2.setText("+"+increment/1000);
        }

        if(joueur1==""){
            nomJoueur1.setVisibility(View.GONE);
        }
        else{
            nomJoueur1.setText(joueur1);
        }

        if(joueur2==""){
            nomJoueur2.setVisibility(View.GONE);
        }
        else{
            nomJoueur2.setText(joueur2);
        }
    }

    public void playSoundTurn(){
        if(sonTour){
            ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                doPause();
                Intent activitySettings = new Intent(MainActivity.this, Settings.class);
                startActivity(activitySettings);

                return true;

            case R.id.action_replay:
                replay();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void pause(){
        if(terminated){
            replay();
        }
        else {
            if (!premierTap) {
                if (paused) {
                    setPauseText(getResources().getString(R.string.pause));

                    if (quiJoueApresPause == NOIRS) {
                        playBlack();
                    } else {
                        playWhite();
                    }
                } else {

                    doPause();
                }
                paused = !paused;
            }
        }
    }

    public void setPauseText(String s){
        pause_resume1.setText(s);
        pause_resume2.setText(s);
    }

    public void doPause(){
        if(!paused && !premierTap){
            setPauseText(getResources().getString(R.string.resume));
            boutonTemps2.setEnabled(false);
            boutonTemps1.setEnabled(false);

            if(quiJoueAvantPause == NOIRS){
                quiJoueApresPause = NOIRS;
            }

            else if(quiJoueAvantPause == BLANCS){
                quiJoueApresPause = BLANCS;
            }

            timeRemaining2 = countDownTimer2.getTimeRemaining();
            countDownTimer2.cancel();

            timeRemaining1 = countDownTimer1.getTimeRemaining();
            countDownTimer1.cancel();
        }
    }

    public void replay(){
        endGame();
        boutonTemps2.setEnabled(true);
        boutonTemps1.setEnabled(true);
        countDownTimer1.displayTime(startTime);
        countDownTimer2.displayTime(startTime);
        updateMoves(nbMove1, move1);
        updateMoves(nbMove2, move2);
        tour.setImageResource(R.drawable.down); // White should start at the beginning so Black should tap the first time
        terminated = false;
        setPauseText(getResources().getString(R.string.pause));
    }

    public void playWhite(){
        tour.setImageResource(R.drawable.up);
        quiJoueAvantPause = BLANCS;

        if (!timer1isRunning) {
            countDownTimer1.start();
            timeRemaining2 = countDownTimer2.getTimeRemaining();
            countDownTimer2.cancel();
            timer1isRunning = true;

        } else {
            /*increment2.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.move_blancs));
            timeRemaining2 = countDownTimer2.addIncAndGetTime(increment);*/
            timeRemaining2 = countDownTimer2.getTimeRemaining();
            countDownTimer2.cancel();
            countDownTimer1 = new MalibuCountDownTimer(timeRemaining1, interval, temps1, temps1bis);
            countDownTimer1.start();
        }

        boutonTemps2.setEnabled(false);
        boutonTemps1.setEnabled(true);
    }

    public void playBlack(){
        tour.setImageResource(R.drawable.down);
        quiJoueAvantPause = NOIRS;

        if (!timer2isRunning) {
            countDownTimer2.start();
            timeRemaining1 = countDownTimer1.getTimeRemaining();
            countDownTimer1.cancel();
            timer2isRunning = true;
        } else {
            timeRemaining1 = countDownTimer1.getTimeRemaining();
            countDownTimer1.cancel();
            countDownTimer2 = new MalibuCountDownTimer(timeRemaining2, interval, temps2, temps2bis);
            countDownTimer2.start();
        }

        boutonTemps1.setEnabled(false);
        boutonTemps2.setEnabled(true);
    }

    public void updateMoves(int nbMove, TextView move){
        if(nbMove>1) {
            move.setText(nbMove + " "+ getResources().getString(R.string.moves));
        }
        else{
            move.setText(nbMove + " "+ getResources().getString(R.string.move));
        }
    }

    public void endGame(){
        premierTap = true;
        paused = false;
        boutonTemps2.setEnabled(false);
        boutonTemps1.setEnabled(false);

        nbMove1 = 0;
        nbMove2 = 0;
        timer1isRunning = false;
        timer2isRunning = false;

        countDownTimer1.cancel();
        countDownTimer2.cancel();

        countDownTimer1 = new MalibuCountDownTimer(startTime, interval, temps1, temps1bis);
        countDownTimer2 = new MalibuCountDownTimer(startTime, interval, temps2, temps2bis);

        setPauseText(getResources().getString(R.string.replay));
        terminated = true;
    }

    public void displayMessagesEndGame(TextView t){
        if(t.equals(temps1)){
            temps1.setText(getResources().getString(R.string.youLose));
            temps1bis.setText(getResources().getString(R.string.heLoses));
            temps2.setText(getResources().getString(R.string.youWin));
            temps2bis.setText(getResources().getString(R.string.heWins));
        }
        else if(t.equals(temps2)){
            temps2.setText(getResources().getString(R.string.youLose));
            temps2bis.setText(getResources().getString(R.string.heLoses));
            temps1.setText(getResources().getString(R.string.youWin));
            temps1bis.setText(getResources().getString(R.string.heWins));
        }
    }

    public TextView getOtherTextView(TextView t) {
        if (t.equals(temps1)) {
            return temps2;
        }
        else{
            return temps1;
        }
    }

    public String whoLost(){
        if(quiAPerdu==BLANCS){
            return getResources().getString(R.string.white);
        }
        else{
            return getResources().getString(R.string.black);
        }
    }

    public void dialogEndGame(String s){
        String title = getResources().getString(R.string.endGame)+" : ";
        String message = "";
        if(quiAPerdu==NOIRS){
            title += getResources().getString(R.string.white);
            long millis = ( timeRemaining1 % 1000)/10;
            long second = ( timeRemaining1 / 1000) % 60;
            long minute = ( timeRemaining1 / (1000 * 60)) % 60;
            String time = String.format("%02d:%02d:%02d", minute, second, millis);
            message +=  getResources().getString(R.string.black)+" :\n"+
                    getResources().getString(R.string.remainingTime)+" : 00:00:00\n"+
                    getResources().getString(R.string.nbMove)+" : "+nbMove2+"\n\n"+
                    getResources().getString(R.string.white)+" : \n"+
                    getResources().getString(R.string.remainingTime)+" "+time+"\n"+
                    getResources().getString(R.string.nbMove)+" : "+nbMove1+"\n";
        }
        else{
            title += getResources().getString(R.string.black);
            long millis = ( timeRemaining2 % 1000)/10;
            long second = ( timeRemaining2 / 1000) % 60;
            long minute = ( timeRemaining2 / (1000 * 60)) % 60;
            String time = String.format("%02d:%02d:%02d", minute, second, millis);
            message +=  getResources().getString(R.string.white)+" :\n"+
                    getResources().getString(R.string.remainingTime)+" : 00:00:00\n"+
                    getResources().getString(R.string.nbMove)+" : "+nbMove1+"\n\n"+
                    getResources().getString(R.string.black)+" : \n"+
                    getResources().getString(R.string.remainingTime)+" "+time+"\n"+
                    getResources().getString(R.string.nbMove)+" : "+nbMove2+"\n";
        }
        title += " "+getResources().getString(R.string.won);
        message += "\n"+getResources().getString(R.string.whatToDo);


        new AlertDialog.Builder(MainActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(getResources().getString(R.string.no), null)
                .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        replay();
                    }
                })
                .create()
                .show();
    }

    // CountDownTimer class
    public class MalibuCountDownTimer extends CountDownTimer {

        private long interval, millis;
        private TextView textviewbis, textview;

        public MalibuCountDownTimer(long startTime, long interval, TextView textview, TextView textviewbis) {
            super(startTime, interval);
            this.interval = interval;
            this.millis = startTime;
            this.textview = textview;
            this.textviewbis = textviewbis;
        }
        @Override
        public void onFinish(){
            if(textview.equals(temps1)){
                quiAPerdu=BLANCS;
            }
            else{
                quiAPerdu=NOIRS;
            }


            dialogEndGame( (String) getOtherTextView(textview).getText());
            displayMessagesEndGame(textview);

            if(vibration){
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(500);
            }

            if(sonFinPartie){
                ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
                toneG.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 500);
            }

            endGame();
        }

        @Override
        public void onTick(long millisUntilFinished) {

            long millis, second, minute;

            this.millis = millisUntilFinished;
            displayTime(millisUntilFinished);

        }

        public void addIncrement(long increment){
            this.millis += increment;
        }

        public void displayTime(long milliseconds){
            long millis = ( milliseconds % 1000)/10;
            long second = ( milliseconds / 1000) % 60;
            long minute = ( milliseconds / (1000 * 60)) % 60;
            String time = String.format("%02d:%02d:%02d", minute, second, millis);

            textview.setText(time);
            textviewbis.setText(time);
        }

        public long getTimeRemaining(){
            return this.millis;
        }

        public long addIncAndGetTime(long increment){
            this.millis += increment;
            displayTime (this.millis);
            return this.millis;
        }
    }
}