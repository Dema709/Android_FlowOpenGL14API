package com.chakapon.flowopengl;

import android.util.Log;

public class Protagonist {

    final String LOG_TAG = "Protagonist";

    private float currentX,currentY;//Положение на карте
    private float orientation;//Ориентация, в радианах
    private float orientationAim;
    private float currentSpeed, maxSpeed, maxBoostSpeed;//В пикселях в секунду
    private float turnSpeed;//В радианах в секунду
    private float touchX,touchY;
    private float screenSizeX, screenSizeY;
    private float canvasSize;
    float canvasSnake;

    Segment [] segments;
    int Nsegm;

    boolean isEatingRightNow;
    private float canvasEat;//Для отображения анимации поедания

    final int NsegmMax=8;

    boolean itWasVoidFood=false;

    boolean levelDownCosDamaged=false;



    Protagonist(){
        currentX=200;
        currentY=00;
        orientation=orientationAim=(float)(0f / 180f * Math.PI);
        currentSpeed=0;
        maxSpeed=200;
        maxBoostSpeed=700;//400
        turnSpeed=(float)(240f / 180f * Math.PI);

        touchX=touchY=0;

        canvasSize=canvasSnake=0;

        Nsegm = 2;
        segments = new Segment[NsegmMax];
            /*for (int k = 0; k < Nsegm; k++) {
                segments[k] = new Segment(currentX, currentY, orientation, k);
            }*/
        segments[0] = new Segment(currentX, currentY, orientation, 0);
        segments[0].setFirst();
        segments[0].setWeakPoint();
        segments[1] = new Segment(segments[0].getCurrentX(), segments[0].getCurrentY(), segments[0].getOrientation(), 1);


        isEatingRightNow=false;
        canvasEat=0;
    }

    public void setScreen(float screenWidth, float screenHeight){
            screenSizeX=screenWidth;screenSizeY=screenHeight;
        }

    public float getMaxSpeed() {return  maxSpeed;}
    public float getCurrentSpeed(){
        return currentSpeed;
    }
    public float getCurrentX(){
        return currentX;
    }//Где используется?
    public float getCurrentY(){
        return currentY;
    }
    public float getOrientationInDegrees(){ return orientation*180/(float)Math.PI;}
    public int getNsegm() {
        return Nsegm;
    }
    public float getCurrentSegX(int num){return segments[num].getCurrentX();}
    public float getCurrentSegY(int num){return segments[num].getCurrentY();}
    public float getCurrentSegRadius(int num){return segments[num].getCurrentRadius();}
    public boolean isSegmentWeakPointAndUndamaged(int nSegm){return segments[nSegm].isSegmentWeakPointAndUndamaged();};
    public void setDamaged(int nSegm){
        segments[nSegm].setWeakPointDamaged();
        int healthRemain=0;
        for (int i=0;i<Nsegm;i++){
            if (segments[i].isSegmentWeakPointAndUndamaged()){
                healthRemain++;
            }
        }
        if (healthRemain==0){
            levelDownCosDamaged=true;
        }
    }

    public void updateMapPosition(float dt, boolean isPressed, boolean isDoubleTapped, float touchX_screen, float touchY_screen, Camera camera) {

        //TouchX/Y - координаты на карте. Преобразуются из координат касания экрана

        //Если подняли палец, оставить старое значение касания
        if (isPressed) {
            touchX = camera.getCurrentX() + (touchX_screen - screenSizeX / 2f);
            touchY = camera.getCurrentY() - (touchY_screen - screenSizeY / 2f);
        }

        //currentX=touchX;currentY=touchY;

        //Угол
        if (isPressed) {//Если нет нажатия, не менять направление (целевое)
            orientationAim = (float) Math.atan2(touchY - currentY, touchX - currentX);
        }
        float orientationDelta = (orientationAim - orientation) % ((float) Math.PI * 2);
        if (Math.abs(orientationDelta) > turnSpeed * dt) {//Если изменение угла не слишком маленькое
            if ((orientationDelta <= -Math.PI) || ((orientationDelta > 0) && (orientationDelta <= Math.PI))) {
                orientation = orientation + turnSpeed * dt;
            } else {
                orientation = orientation - turnSpeed * dt;
            }
        } else {
            orientation = orientationAim;
        }

        //Скорость

        if (isPressed) {
            if (isDoubleTapped) {
                currentSpeed = Math.min(maxBoostSpeed, currentSpeed + dt * 400);//400 - прирост скорости?
                //Log.wtf(LOG_TAG,"DoubleTapped=boosted");
            } else {
                if (currentSpeed > maxSpeed) {//Теряем скорость после ускорения
                    //currentSpeed = Math.max(currentSpeed*0.98f*dt*30,maxSpeed);//FPS=30
                    currentSpeed = Math.max(currentSpeed * (float) Math.pow(0.98d, dt * 30), maxSpeed);//FPS=30
                } else {
                    currentSpeed = Math.min(maxSpeed, currentSpeed + dt * 400f * 0.5f);
                }
            }

        } else {
            //currentSpeed = currentSpeed*0.95f*dt*30;//FPS=30
            currentSpeed = currentSpeed * (float) Math.pow(0.95d, dt * 30);//FPS=30
        }

        //Позиция
        currentX = currentX + currentSpeed * (float) Math.cos(orientation) * dt;
        currentY = currentY + currentSpeed * (float) Math.sin(orientation) * dt;


        canvasSize=(canvasSize+dt*0.8f)%1;
        canvasSnake=(canvasSnake+dt*currentSpeed/maxSpeed)%1;

        if (isEatingRightNow){
            canvasEat=(canvasEat+dt*0.8f*2);
            if (canvasEat>=2) {
                canvasEat = 0;
                if (itWasVoidFood) {//Не насыщать клетки, если перешёл на другой уровень
                    itWasVoidFood=false;
                }
                else{
                    this.evolveLittle();
                }
                isEatingRightNow = false;
            }
        }

        for (int k=0;k<Nsegm;k++){
            if (k==0) {
                segments[k].updateMapPosition(currentX, currentY, dt, currentSpeed);
            }
            else {
                segments[k].updateMapPosition(segments[k-1].getCurrentX(), segments[k-1].getCurrentY(), dt, currentSpeed);
            }
        }
    }
    public void updateMapPosition(float dt, float acceletometerRadius, float acceletometerOrientation) {
        //Log.wtf(LOG_TAG,"accR "+acceletometerRadius+" accO "+acceletometerOrientation);
        //Угол

        //Log.wtf(LOG_TAG,"updated mapP "+acceletometerRadius+" "+acceletometerOrientation);
        orientationAim = acceletometerOrientation;//-acceletometerOrientation+(float)Math.PI/2;//(float) Math.atan2(touchY - currentY, touchX - currentX);
        //Log.wtf(LOG_TAG,"accelerometr rad "+acceletometerRadius);

        float orientationDelta = (orientationAim - orientation) % ((float) Math.PI * 2);
        float acceletometerRadius_=Math.min(20,acceletometerRadius);
        float neededSpeed=acceletometerRadius_*maxBoostSpeed/20;

        Log.wtf(LOG_TAG,"accR "+acceletometerRadius);
        if (acceletometerRadius>1) {
            if (Math.abs(orientationDelta) > turnSpeed * dt) {//Если изменение угла не слишком маленькое
                if ((orientationDelta <= -Math.PI) || ((orientationDelta > 0) && (orientationDelta <= Math.PI))) {
                    orientation = orientation + turnSpeed * dt;
                } else {
                    orientation = orientation - turnSpeed * dt;
                }
            } else {
                orientation = orientationAim;
            }
        }
        //Скорость

        //Log.wtf(LOG_TAG,"oriDELTA "+ Math.abs(orientationAim - orientation) % ((float) Math.PI * 2));


        if (currentSpeed<neededSpeed) {
            currentSpeed = Math.min(neededSpeed, currentSpeed + dt * 400);//400 - прирост скорости?
        }
        else{
            currentSpeed = Math.max(neededSpeed,currentSpeed * (float) Math.pow(0.98d, dt * 30));//FPS=30
        }

        //Позиция
        currentX = currentX + currentSpeed * (float) Math.cos(orientation) * dt;
        currentY = currentY + currentSpeed * (float) Math.sin(orientation) * dt;


        canvasSize=(canvasSize+dt*0.8f)%1;
        canvasSnake=(canvasSnake+dt*currentSpeed/maxSpeed)%1;

        if (isEatingRightNow){
            canvasEat=(canvasEat+dt*0.8f*2);
            if (canvasEat>=2) {
                canvasEat = 0;
                if (itWasVoidFood) {//Не насыщать клетки, если перешёл на другой уровень
                    itWasVoidFood=false;
                }
                else{
                    this.evolveLittle();
                }
                isEatingRightNow = false;
            }
        }

        for (int k=0;k<Nsegm;k++){
            if (k==0) {
                segments[k].updateMapPosition(currentX, currentY, dt, currentSpeed);
            }
            else {
                segments[k].updateMapPosition(segments[k-1].getCurrentX(), segments[k-1].getCurrentY(), dt, currentSpeed);
            }
        }
        //Log.wtf(LOG_TAG,"updated");
    }

    public void draw(OpenGLRenderer openGLRenderer){

        openGLRenderer.drawRing2(currentX,currentY,2*0.7f*5.3f);
        openGLRenderer.drawMouth(currentX,currentY,this.getOrientationInDegrees(),1-Math.abs(canvasEat%1-0.5f)*2f);
        openGLRenderer.drawSquareTransfered(currentX,currentY,3.36f,this.getOrientationInDegrees(),-21,0);

        for (int k=0;k<Nsegm;k++){
            segments[k].drawWithScale(openGLRenderer,k,Nsegm);
        }

        /*for (int k=0;k<Nsegm;k++){
            segments[k].draw(openGLRenderer);
        }*/
        //Log.wtf(LOG_TAG,"updated draw "+currentX+" "+currentY);
    }//Отображение


    public int updateEat(Food [] foods_array, SnakeHunter [] snakeHunter_array, SharkHunter [] sharkHunter_array, ChangeLevelFood [] changeLevelFood_array, FlockieBird [] flockieBird_array, Boss [] boss_array){
        //minus lvl ot damaga
        if (levelDownCosDamaged){
            segments[0].restoreWeakPoint();


            levelDownCosDamaged=false;
            return -1;
        }

        if (!isEatingRightNow) {
            float mouthDist = 30;
            float mouthRadius = 20;

            //Поедание еды на смену уровня
            for (int i = 0; i < changeLevelFood_array.length; i++) {
                if (Math.pow(currentX + mouthDist * Math.cos(orientation) - changeLevelFood_array[i].getCurrentX(), 2) +
                        Math.pow(currentY + mouthDist * Math.sin(orientation) - changeLevelFood_array[i].getCurrentY(), 2) <
                        Math.pow(mouthRadius + changeLevelFood_array[i].getCurrentRadius(), 2)) {
                    isEatingRightNow = true;
                    itWasVoidFood = true;
                    return changeLevelFood_array[i].getType();
                }
            }

            //Поедание босса
            for (int i = 0; i < boss_array.length; i++) {
                if (!boss_array[i].isEaten()) {


                    for (int j = 0; j < boss_array[i].getNsegm(); j++) {
                        if (boss_array[i].isSegmentWeakPointAndUndamaged(j)) {
                            if (Math.pow(currentX + mouthDist * Math.cos(orientation) - boss_array[i].getCurrentSegX(j), 2) +
                                    Math.pow(currentY + mouthDist * Math.sin(orientation) - boss_array[i].getCurrentSegY(j), 2) <
                                    Math.pow(mouthRadius + boss_array[i].getCurrentSegRadius(j), 2)) {//Радиус
                                isEatingRightNow = true;
                                boss_array[i].setDamaged(j);
                                return 0;
                            }
                        }
                    }

                }
            }

            //Поедание мелких
            for (int i = 0; i < foods_array.length; i++) {
                if (!foods_array[i].isEaten()) {
                    if (Math.pow(currentX + mouthDist * Math.cos(orientation) - foods_array[i].getCurrentX(), 2) + Math.pow(currentY + mouthDist * Math.sin(orientation) - foods_array[i].getCurrentY(), 2) <
                            Math.pow(mouthRadius + foods_array[i].getCurrentRadius(), 2)) {
                        isEatingRightNow = true;
                        foods_array[i].setEaten();
                        return 0;
                    }
                }
            }

            //Поедание змейки
            for (int i = 0; i < snakeHunter_array.length; i++) {
                if (!snakeHunter_array[i].isEaten()) {



                    for (int j = 0; j < snakeHunter_array[i].getNsegm(); j++) {
                        if (snakeHunter_array[i].isSegmentWeakPointAndUndamaged(j)) {
                            if (Math.pow(currentX + mouthDist * Math.cos(orientation) - snakeHunter_array[i].getCurrentSegX(j), 2) +
                                    Math.pow(currentY + mouthDist * Math.sin(orientation) - snakeHunter_array[i].getCurrentSegY(j), 2) <
                                    Math.pow(mouthRadius + snakeHunter_array[i].getCurrentSegRadius(j), 2)) {//Радиус
                                isEatingRightNow = true;
                                snakeHunter_array[i].setDamaged(j);
                                return 0;
                            }
                        }
                    }


                }
            }

            //Поедание акулы
            for (int i = 0; i < sharkHunter_array.length; i++) {
                if (!sharkHunter_array[i].isEaten()) {
                    for (int j = 0; j < sharkHunter_array[i].getNsegm(); j++) {
                        if (sharkHunter_array[i].isSegmentWeakPointAndUndamaged(j)) {
                            if (Math.pow(currentX + mouthDist * Math.cos(orientation) - sharkHunter_array[i].getCurrentSegX(j), 2) +
                                    Math.pow(currentY + mouthDist * Math.sin(orientation) - sharkHunter_array[i].getCurrentSegY(j), 2) <
                                    Math.pow(mouthRadius + sharkHunter_array[i].getCurrentSegRadius(j), 2)) {//Радиус
                                isEatingRightNow = true;
                                sharkHunter_array[i].setDamaged(j);
                                return 0;
                            }
                        }
                    }
                }
            }

            //Поедание стайки
            for (int i = 0; i < flockieBird_array.length; i++) {
                if (!flockieBird_array[i].areEaten()) {
                    //Log.wtf(LOG_TAG,"dd");
                    for (int j=0;j<flockieBird_array[i].getNbirds();j++){
                        if (!flockieBird_array[i].isEaten(j)){
                            if (Math.pow(currentX + mouthDist * Math.cos(orientation) - flockieBird_array[i].getCurrentX(j), 2) +
                                    Math.pow(currentY + mouthDist * Math.sin(orientation) - flockieBird_array[i].getCurrentY(j), 2) <
                                    Math.pow(mouthRadius + flockieBird_array[i].getCurrentRadius(j), 2)) {
                                isEatingRightNow = true;
                                flockieBird_array[i].setDamaged(j);
                                return 0;
                            }

                        }




                    }






                }
            }


        }
        return 0;
    }


    public void evolveLittle(){


        //Нажрать кружочки
        //Попробовать восстановить weakPoint
        //Если удалось - return
        int kT = 0;
        while (kT < Nsegm) {
            if (segments[kT].isWeakPointDamaged()) {//сработает только на weakPoint
                segments[kT].restoreWeakPoint();
                return;//break;
            }//Восстановление weakPoint
            kT++;
        }
        ////////////////////////////////////////////////Можно сделать проще - как выше
        int saturationSum = 0;
        for (int k = 0; k < Nsegm - 1; k++) {
            if (segments[k].getSaturation())
                saturationSum++;
        }
        if (saturationSum == Nsegm - 1) {
            this.evolveBig();
        } else {
            //Ищи наименьшую пустую и наполняй
            int k = 0;
            while (k < Nsegm - 1) {
                if (!segments[k].getSaturation()) {
                    segments[k].setSaturation(true);
                    break;
                }
                k++;
            }
        }

    }

    public void evolveBig(){//А вот здесь пошла реальная эволюция xD

        int [] k_types;
        k_types=new int[3];//Сколько сегментов какого типа, кроме хвостика(?)
        int k2=0;//Счётчик перебора
        while (k2<Nsegm-1){
            k_types[segments[k2].getType()]++;
            k2++;
        }


        if (Nsegm<NsegmMax){

            if (Nsegm-k_types[2]>3){
                //добавить лапку
                int k0=0;
                while (k0<Nsegm-1){
                    if (segments[k0].getType()==0){
                        //segments[k0].setSaturation(false);
                        segments[k0].changeType(2);//
                        break;
                    }
                    k0++;
                }

                for (int k = 0; k < Nsegm - 1; k++) {//Убрать насыщение
                    //segments[k].changeType(0);//
                    segments[k].setSaturation(false);
                }
            }
            else {

                Nsegm++;
                //segments[Nsegm - 1] = new Segment(currentX, currentY, segments[Nsegm - 2].getOrientation(), 1);//Хвост
                segments[Nsegm - 1] = new Segment(segments[Nsegm - 2].getCurrentX(), segments[Nsegm - 2].getCurrentY(), segments[Nsegm - 2].getOrientation(), 1);//Хвост
                segments[Nsegm - 2].changeType(0);//
                segments[Nsegm - 2].setWeakPoint();
/////////////////////////NET!
                for (int k = 0; k < Nsegm - 1; k++) {
                    //segments[k].changeType(0);//
                    segments[k].setSaturation(false);
                }

            }
        }


        else{
            int k0=0;
            while (k0<Nsegm-1){
                if (segments[k0].getType()==0){
                    //segments[k0].setSaturation(false);
                    segments[k0].changeType(2);//
                    break;
                }
                k0++;
            }


            int segmTypeSum=0;
            for (int k=0;k<Nsegm-1;k++) {
                if (segments[k].getType()==2)
                    segmTypeSum++;
            }
            if (segmTypeSum!=Nsegm-1) {

                for (int k = 0; k < Nsegm - 1; k++) {
                    //segments[k].changeType(0);//
                    segments[k].setSaturation(false);
                }
            }
        }

        //Log.wtf(LOG_TAG, "N_seg "+k_types[0]+"/"+k_types[1]+"/"+k_types[2]);//Было
    }




}
