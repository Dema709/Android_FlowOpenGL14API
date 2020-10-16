package com.chakapon.flowopengl;

import android.util.Log;

public class Boss {
    final String LOG_TAG = "Boss1";

    private int bossType;

    private float currentX, currentY;//Положение на карте
    private float orientation;//Ориентация, в радианах
    private float orientationAim;
    private float currentSpeed, maxSpeed, maxBoostSpeed;//В пикселях в секунду
    private float turnSpeed;//,maxTurnSpeed;//В радианах в секунду

    private float canvasSize;
    private float canvasSnake;

    Segment[] segments;
    int Nsegm;

    boolean isEatingRightNow = true;//Чтобы не съел сразу!
    private float canvasEat;//Для отображения анимации поедания
    private float canvasSegmentMovement;

    boolean isPanic = false;
    float panicTimer = 0, panicMaxTime = 2;

    boolean hasTarget = false;
    boolean hasPlayerInTarget=false;
    boolean isEaten = false;
    float aimX, aimY;


    boolean isInDivision = false;
    float divisionTimer = 0;
    boolean isDivisionWrittenInFood = false;


    float agroTimer = 8, agroMaxTimeWithoutAim = 2, agroAngle = (float) (60f / 180f * Math.PI), agroRadius, agroQuietRadius;// = 600;//Угол в каждую сторону
    boolean isAgro = false;

    Food [] angryFood;

    Boss(int bossType){
        this.bossType=bossType;
        Log.wtf(LOG_TAG,"Bosstype="+this.bossType);

        currentX = (float) (Math.random()-0.5)*2*1000;
        currentY = (float) (Math.random()-0.5)*2*1000;

        aimX = (float) (Math.random() - 0.5) * 2 * 1000;
        aimY = (float) (Math.random() - 0.5) * 2 * 1000;

        orientationAim = 0;
        turnSpeed = (float) ((8f + (float) Math.random() * 3f) * 5 / 180f * Math.PI);//*20

        //Log.wtf(LOG_TAG,"turnspeed "+turnSpeed*180/3.1415);

        maxSpeed = 140;
        maxBoostSpeed = maxSpeed*3;
        //screenSizeX = screenSizeX_;
        //screenSizeY = screenSizeY_;


        agroRadius=maxBoostSpeed/turnSpeed*1.5f;
        agroQuietRadius=agroRadius*1.2f;

        orientation = (float) (Math.random() * Math.PI * 2f);
        currentSpeed = (float) (Math.random() * maxSpeed);


        canvasSize = canvasSnake = canvasEat = canvasSegmentMovement = 0;

        Nsegm = 12;


        segments = new Segment[Nsegm];

        angryFood = new Food[Nsegm-3];//Столько пустых точечных сегментов

        for (int i=0;i<Nsegm;i++){
            if (i<3){
                segments[i] = new Segment(3,1);//Три летающие точки
            }
            else{
                segments[i] = new Segment(4,1);//Мелкие сегменты
                angryFood[i-3] = new Food("angryBoss1Food");
            }
        }
    }

    public float getOrientationInDegrees(){ return orientation*180/(float)Math.PI;}

    public void updateMapPosition(float dt,Protagonist protagonist) {

        if (isInDivision){
            if (divisionTimer>(Nsegm+1)*0.1+1) isInDivision=false;//С запасом. А так NsegmMax*0.1+1
            //Log.wtf(LOG_TAG, "Разделение на части в процессе! "+divisionTimer);
            divisionTimer=divisionTimer+dt;


        }



        if (!isEaten) {
            if (isPanic) {
                if (panicTimer > panicMaxTime) {
                    isPanic = false;
                    aimX = (float) (Math.random() - 0.5) * 2 * 1000;
                    aimY = (float) (Math.random() - 0.5) * 2 * 1000;
                }
                panicTimer = panicTimer + dt;
            }//Обновление таймера паники

            if (isPanic) {
                currentSpeed = Math.min(maxBoostSpeed, currentSpeed + dt * 800);
            }
            else{//Паники нет
                if (hasPlayerInTarget){
                    orientationAim = (float) Math.atan2(aimY - currentY, aimX - currentX);
                    float orientationDelta = (orientationAim - orientation) % ((float) Math.PI * 2);
                    //turnSpeed = maxTurnSpeed;
                    if (Math.abs(orientationDelta) > turnSpeed * dt) {//Если изменение угла не слишком маленькое
                        if ((orientationDelta <= -Math.PI) || ((orientationDelta > 0) && (orientationDelta <= Math.PI))) {
                            orientation = orientation + turnSpeed * dt;
                        } else {
                            orientation = orientation - turnSpeed * dt;
                        }
                    } else {
                        orientation = orientationAim;
                    }
                    currentSpeed=maxBoostSpeed;
                }//Видна цель - гг. В агре движется на неё
                else{
                    if (isAgro) {
                        if (agroTimer > agroMaxTimeWithoutAim) {
                            isAgro = false;

                            //Log.wtf(LOG_TAG, "Рандомный путь. Cброс агры по времени");
                            aimX = (float) (Math.random() - 0.5) * 2 * 1000;
                            aimY = (float) (Math.random() - 0.5) * 2 * 1000;
                        }
                        agroTimer = agroTimer + dt;
                    }//Обновление таймера агрессии
                    if (isAgro){
                        currentSpeed=maxBoostSpeed;//Лишнее действие. И можно замедлять чуток, если нет цели в виде гг
                    }//Агрессия осталась, но пролетел мимо гг
                    else{
                        if (!hasTarget){
                            if ((Math.abs(aimX - currentX) < (200)) & (Math.abs(aimY - currentY) < (200))) {
                                //Log.wtf(LOG_TAG, "Рандомный путь при приближении");
                                aimX = (float) (Math.random() - 0.5) * 2 * 1000;
                                aimY = (float) (Math.random() - 0.5) * 2 * 1000;
                            }
                        }//Обновление точки маршрута при приближении

                        orientationAim = (float) Math.atan2(aimY - currentY, aimX - currentX);
                        float orientationDelta = (orientationAim - orientation) % ((float) Math.PI * 2);
                        //turnSpeed = maxTurnSpeed;
                        if (Math.abs(orientationDelta) > turnSpeed * dt) {//Если изменение угла не слишком маленькое
                            if ((orientationDelta <= -Math.PI) || ((orientationDelta > 0) && (orientationDelta <= Math.PI))) {
                                orientation = orientation + turnSpeed * dt;
                            } else {
                                orientation = orientation - turnSpeed * dt;
                            }
                        } else {
                            orientation = orientationAim;
                        }
                        if (currentSpeed > maxSpeed) {//Теряем скорость после ускорения
                            currentSpeed = Math.max(currentSpeed * (float) Math.pow(0.98d, dt * 30), maxSpeed);//FPS=30
                        } else {
                            currentSpeed = Math.min(maxSpeed, currentSpeed + dt * 400f * 0.5f);
                        }
                    }//Не агрессия
                }
            }

            //Позиция
            currentX = currentX + currentSpeed * (float) Math.cos(orientation) * dt;
            currentY = currentY + currentSpeed * (float) Math.sin(orientation) * dt;
            //Обновление для канвы - изменение размера
            canvasSize = (canvasSize + dt * 0.4f) % 1;
            canvasSnake = (canvasSnake + dt * currentSpeed / 100f * 0.5f) % 1;
            if (isEatingRightNow) {
                canvasEat = (canvasEat + dt * 0.2f * 2);
                if (canvasEat >= 2) {
                    canvasEat = 0;
                    isEatingRightNow = false;
                }
            }
            canvasSegmentMovement = (canvasSegmentMovement + dt * 0.4f) % 3;
            //Log.wtf(LOG_TAG,"canvasSM ");
            float time=canvasSegmentMovement%1;
            time=0.5f-0.5f*(float)Math.cos(Math.PI*time);
            for (int i=0;i<3;i++) {
                if ((canvasSegmentMovement + 3 - i )%3 < 1) {
                    //Первое движение
                    segments[i].updateMapPosition(currentX + (294f*0.97f*(1-time)+80f*time) * (float) Math.cos(orientation) - (206*time) * (float) Math.sin(orientation),
                            currentY + (294f*0.97f*(1-time)+80f*time) * (float) Math.sin(orientation) + (206*time) * (float) Math.cos(orientation),
                            orientation + (float) Math.PI);
                    //Log.wtf(LOG_TAG,"canvasSM "+i+" "+time);
                } else {
                    if ((canvasSegmentMovement + 3 - i )%3 < 2) {
                        //Второе движение
                        segments[i].updateMapPosition(currentX + (80f) * (float) Math.cos(orientation) - (206*(1-time)+(-206)*time) * (float) Math.sin(orientation),
                                currentY + (80f) * (float) Math.sin(orientation) + (206*(1-time)+(-206)*time) * (float) Math.cos(orientation),
                                orientation + (float) Math.PI);
                    } else {
                        //Третье движение
                        segments[i].updateMapPosition(currentX + (294f*0.97f*time+80f*(1-time)) * (float) Math.cos(orientation) - (-206*(1-time)) * (float) Math.sin(orientation),
                                currentY + (294f*0.97f*time+80f*(1-time)) * (float) Math.sin(orientation) + (-206*(1-time)) * (float) Math.cos(orientation),
                                orientation + (float) Math.PI);
                    }
                }
            }



            //Обновление сегментов
            /*
            segments[0].updateMapPosition(currentX + 206 * (float) Math.sin(orientation) + 80f * (float) Math.cos(orientation),
                    currentY - 206 * (float) Math.cos(orientation)+ 80f * (float) Math.sin(orientation), orientation + (float) Math.PI);
            segments[1].updateMapPosition(currentX - 206 * (float) Math.sin(orientation) + 80f * (float) Math.cos(orientation),
                    currentY + 206 * (float) Math.cos(orientation)+ 80f * (float) Math.sin(orientation), orientation + (float) Math.PI);
            segments[2].updateMapPosition(currentX + 294f*0.97f * (float) Math.cos(orientation),
                    currentY + 294f*0.97f * (float) Math.sin(orientation), orientation + (float) Math.PI);
*/


            segments[3].updateMapPosition(currentX + 246 * (float) Math.cos(orientation),
                    currentY + 246 * (float) Math.sin(orientation), orientation + (float) Math.PI);
            segments[4].updateMapPosition(currentX + (-40) * (float) Math.cos(orientation)- (16) * (float) Math.sin(orientation),
                    currentY + (-40) * (float) Math.sin(orientation) + (16) * (float) Math.cos(orientation),
                    orientation + (float) Math.PI);
            segments[5].updateMapPosition(currentX + (52) * (float) Math.cos(orientation)- (68) * (float) Math.sin(orientation),
                    currentY + (52) * (float) Math.sin(orientation) + (68) * (float) Math.cos(orientation),
                    orientation + (float) Math.PI);
            segments[6].updateMapPosition(currentX + (118) * (float) Math.cos(orientation)- (121) * (float) Math.sin(orientation),
                    currentY + (118) * (float) Math.sin(orientation) + (121) * (float) Math.cos(orientation),
                    orientation + (float) Math.PI);
            segments[7].updateMapPosition(currentX + (174) * (float) Math.cos(orientation)- (62) * (float) Math.sin(orientation),
                    currentY + (174) * (float) Math.sin(orientation) + (62) * (float) Math.cos(orientation),
                    orientation + (float) Math.PI);
            segments[8].updateMapPosition(currentX + (15) * (float) Math.cos(orientation)- (-75) * (float) Math.sin(orientation),
                    currentY + (15) * (float) Math.sin(orientation) + (-75) * (float) Math.cos(orientation),
                    orientation + (float) Math.PI);
            segments[9].updateMapPosition(currentX + (82) * (float) Math.cos(orientation)- (-49) * (float) Math.sin(orientation),
                    currentY + (82) * (float) Math.sin(orientation) + (-49) * (float) Math.cos(orientation),
                    orientation + (float) Math.PI);
            segments[10].updateMapPosition(currentX + (148) * (float) Math.cos(orientation)- (-96) * (float) Math.sin(orientation),
                    currentY + (148) * (float) Math.sin(orientation) + (-96) * (float) Math.cos(orientation),
                    orientation + (float) Math.PI);
            segments[11].updateMapPosition(currentX + (184) * (float) Math.cos(orientation)- (-44) * (float) Math.sin(orientation),
                    currentY + (184) * (float) Math.sin(orientation) + (-44) * (float) Math.cos(orientation),
                    orientation + (float) Math.PI);
            /*
            segments[3].updateMapPosition(currentX - 119 * scaleForLittleOrBigFish * (float) Math.cos(orientation),
                    currentY - 119 * scaleForLittleOrBigFish * (float) Math.sin(orientation), orientation + (float) Math.PI);
            segments[4].updateMapPosition(currentX - 516 * scaleForLittleOrBigFish * (float) Math.cos(orientation),
                    currentY - 516 * scaleForLittleOrBigFish * (float) Math.sin(orientation), orientation + (float) Math.PI);
*/
        }

        for (int i=0;i<Nsegm-3;i++){
            angryFood[i].updateMapPositionAngryBoss(dt,protagonist);
        }
    }


    public void findNearFood(Food [] foods_array, Protagonist protagonist){


        if (!isEaten) {
            hasPlayerInTarget=false;
            hasTarget=false;
            if (!isPanic) {
                if (!isEatingRightNow) {
                    float mouthDist = 294f*0.97f;
                    float curCheckX, curCheckY;//Положение текущей цели

                    //Попытка съесть протагониста или сагриться на него
                    for (int i = 0; i < protagonist.getNsegm(); i++) {
                        if (protagonist.isSegmentWeakPointAndUndamaged(i)) {
                            curCheckX=protagonist.getCurrentSegX(i);
                            curCheckY=protagonist.getCurrentSegY(i);


                            if (Math.pow(currentX + mouthDist * Math.cos(orientation) - curCheckX, 2) +
                                    Math.pow(currentY + mouthDist * Math.sin(orientation) - curCheckY, 2) <
                                    Math.pow(38 + protagonist.getCurrentSegRadius(i), 2)) {
                                protagonist.setDamaged(i);
                                isEatingRightNow = true;
                                return;
                            }//Если попало в рот

                            if (!isAgro){
                                if (!hasTarget) {//Первый раз за цикл
                                    if (Math.pow(currentX - curCheckX, 2) + Math.pow(currentY - curCheckY, 2) < Math.pow(agroQuietRadius, 2)) {
                                        //Если в большом агрорадиусе
                                        aimX = curCheckX;
                                        aimY = curCheckY;
                                        hasTarget = true;
                                    }
                                } else {
                                    if (Math.pow(currentX - curCheckX, 2) + Math.pow(currentY - curCheckY, 2) <
                                            Math.pow(currentX - aimX, 2) + Math.pow(currentY - aimY, 2)) {
                                        //Новое расстояние меньше
                                        aimX = curCheckX;
                                        aimY = curCheckY;
                                    }
                                }
                            }//Если не в агре - проверка на ближайшую цель в большом агрорадиусе


                            if (Math.pow(currentX - curCheckX, 2) + Math.pow(currentY - curCheckY, 2) < Math.pow(agroRadius, 2)){
                                //Если попало в малый агрорадиус
                                if (Math.abs(((float) Math.atan2(curCheckY - currentY, curCheckX - currentX) - orientation) % ((float) Math.PI * 2)) < agroAngle){
                                    //Если попало в агроугол
                                    if (!hasPlayerInTarget) {//Первый раз за цикл
                                        aimX = curCheckX;
                                        aimY = curCheckY;
                                        isAgro = true;
                                        hasPlayerInTarget = true;
                                        agroTimer = 0;
                                    } else {
                                        if (Math.pow(currentX - curCheckX, 2) + Math.pow(currentY - curCheckY, 2) < Math.pow(currentX - aimX, 2) + Math.pow(currentY - aimY, 2)) {
                                            //Новое расстояние меньше
                                            aimX = curCheckX;
                                            aimY = curCheckY;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (isAgro){
                        return;
                    }//Если агры, нет смысла проверять еду

                }
            }
        }
        else{

            if (!isDivisionWrittenInFood){
                Log.wtf(LOG_TAG,"Разваливается на еду");
                int k=0;
                for (int i=0;i<Nsegm;i++){
                    //if (segments[i].hasSaturationOrIsWeakPoint()){
                        while (k<foods_array.length-1){
                            if (foods_array[k].isEatenAndNotInvisible()){
                                foods_array[k].setInvisible((float)i*0.1f+0.1f,segments[i].getCurrentX(),segments[i].getCurrentY());
                                k++;
                                break;
                            }
                            k++;
                        }
                    //}
                    isDivisionWrittenInFood=true;
                }
            }
        }
    }





    void draw(OpenGLRenderer openGLRenderer, Camera camera){
        if (isInDivision){
            Log.wtf(LOG_TAG,"inDivision Nsegm="+Nsegm+" divTimer"+divisionTimer);
            for (int k = 0; k < Nsegm; k++) {
                if (divisionTimer-k*0.1f<0){
                    segments[k].draw(openGLRenderer);
                }
                else{
                    segments[k].drawDivision(openGLRenderer,divisionTimer-k*0.1f);
                }
            }
        }

        if (!isEaten) {

            if (isAgro) openGLRenderer.setColor(7);//Red

            openGLRenderer.drawBossBody(currentX,currentY,this.getOrientationInDegrees()+180,Math.abs(canvasSnake - 0.5f) * 2f,1);

            openGLRenderer.drawSharkmouthTransfered(currentX,currentY,1*0.3f,this.getOrientationInDegrees(),294f*0.97f,0,(canvasSize+canvasEat)*360+this.getOrientationInDegrees());

            openGLRenderer.drawRing3Transfered(currentX,currentY,103*0.3f,this.getOrientationInDegrees(),294f*0.97f,0);
            openGLRenderer.drawRing3Transfered(currentX,currentY,153*0.3f,this.getOrientationInDegrees(),270,0);
            /*
            openGLRenderer.drawSharkBody(currentX,currentY,this.getOrientationInDegrees(),Math.abs(canvasSnake - 0.5f) * 2f,0.1f);
            openGLRenderer.drawRing3Transfered(currentX,currentY,103*0.1f,this.getOrientationInDegrees(),774*0.1f,0);
            openGLRenderer.drawRing3Transfered(currentX,currentY,153*0.1f,this.getOrientationInDegrees(),724*0.1f,0);
            openGLRenderer.drawSharkmouthTransfered(currentX,currentY,0.1f,this.getOrientationInDegrees(),774*0.1f,0,(canvasSize+canvasEat)*360+this.getOrientationInDegrees());
            */
            if (isAgro) openGLRenderer.setColor(0);//White

            for (int k = 0; k < Nsegm; k++) {
                segments[k].draw(openGLRenderer);
            }


        }

        for (int i=0;i<Nsegm-3;i++){
            angryFood[i].drawAngryBossFood(openGLRenderer, camera);
        }
                        //Log.wtf(LOG_TAG,"isssss"+angryFood[0].isEaten());

    }

    public boolean isEaten() {
        return isEaten;
    }

    public int getNsegm(){
        if (bossType==1) return 3;
        Log.wtf(LOG_TAG,"error bossType: Nsegm to eat");return 0;}
    public float getCurrentSegX(int nSegm){return segments[nSegm].getCurrentX();}
    public float getCurrentSegY(int nSegm){return segments[nSegm].getCurrentY();}
    public float getCurrentSegRadius(int nSegm){return segments[nSegm].getCurrentRadius();}
    public boolean isSegmentWeakPointAndUndamaged(int nSegm){return segments[nSegm].isSegmentWeakPointAndUndamaged();}

    public void setDamaged(int nSegm){
        segments[nSegm].setWeakPointDamaged();///////////////////////////////////////////////Можно будет объединить


        int weakUndamagedSum=0;
        if (bossType==1){
            for (int k=0;k<Nsegm-1;k++) {
                if (segments[k].isSegmentWeakPointAndUndamaged())///////////////////////////////Тут должна быть проверка на слабые точки аля weakpoints
                    weakUndamagedSum++;
            }
        }
        else Log.wtf(LOG_TAG,"Error bosstype: setdamaged undefined type");

        if (weakUndamagedSum==0){
            isInDivision=true;
            divisionTimer=0;
            isEaten=true;/////////////////////////////////////////////////////////////////////////////И вызов ф-ии распада
        }//Вас сожрали нафиг ;р
        else{
            isPanic=true;
            isAgro=false;
            panicTimer=0;

            for (int i=0;i<Nsegm-3;i++){//Столько точечных сегментов
                angryFood[i].setInvisibleAngryBossfood((float)Math.random()*3+5,segments[i+3].getCurrentX(),segments[i+3].getCurrentY(),currentSpeed,orientation);
            }



            //Log.wtf(LOG_TAG, "Паника!");
        }//Больно! Надо ускориться! Паника!
    }
/*




    public float getOrientationInDegrees(){ return orientation*180/(float)Math.PI;}





    public void updateMapPosition(float dt) {

        if (isInDivision){
            if (divisionTimer>(Nsegm+1)*0.1+1) isInDivision=false;//С запасом. А так NsegmMax*0.1+1
            //Log.wtf(LOG_TAG, "Разделение на части в процессе! "+divisionTimer);


            divisionTimer=divisionTimer+dt;
        }

        //Log.wtf(LOG_TAG,"panic "+isPanic+" agro "+isAgro);
        if (!isEaten) {
            if (isPanic) {
                if (panicTimer > panicMaxTime) {
                    isPanic = false;
                    aimX = (float) (Math.random() - 0.5) * 2 * 1000;
                    aimY = (float) (Math.random() - 0.5) * 2 * 1000;
                }
                panicTimer = panicTimer + dt;
            }//Обновление таймера паники

            if (isPanic) {
                currentSpeed = Math.min(maxBoostSpeed, currentSpeed + dt * 800);
            }
            else{//Паники нет
                if (hasPlayerInTarget){
                    orientationAim = (float) Math.atan2(aimY - currentY, aimX - currentX);
                    float orientationDelta = (orientationAim - orientation) % ((float) Math.PI * 2);
                    //turnSpeed = maxTurnSpeed;
                    if (Math.abs(orientationDelta) > turnSpeed * dt) {//Если изменение угла не слишком маленькое
                        if ((orientationDelta <= -Math.PI) || ((orientationDelta > 0) && (orientationDelta <= Math.PI))) {
                            orientation = orientation + turnSpeed * dt;
                        } else {
                            orientation = orientation - turnSpeed * dt;
                        }
                    } else {
                        orientation = orientationAim;
                    }
                    currentSpeed=maxBoostSpeed;
                }//Видна цель - гг. В агре движется на неё
                else{
                    if (isAgro) {
                        if (agroTimer > agroMaxTimeWithoutAim) {
                            isAgro = false;

                            //Log.wtf(LOG_TAG, "Рандомный путь. Cброс агры по времени");
                            aimX = (float) (Math.random() - 0.5) * 2 * 1000;
                            aimY = (float) (Math.random() - 0.5) * 2 * 1000;
                        }
                        agroTimer = agroTimer + dt;
                    }//Обновление таймера агрессии
                    if (isAgro){
                        currentSpeed=maxBoostSpeed;//Лишнее действие. И можно замедлять чуток, если нет цели в виде гг
                    }//Агрессия осталась, но пролетел мимо гг
                    else{
                        if (!hasTarget){
                            if ((Math.abs(aimX - currentX) < (200)) & (Math.abs(aimY - currentY) < (200))) {
                                //Log.wtf(LOG_TAG, "Рандомный путь при приближении");
                                aimX = (float) (Math.random() - 0.5) * 2 * 1000;
                                aimY = (float) (Math.random() - 0.5) * 2 * 1000;
                            }
                        }//Обновление точки маршрута при приближении

                        orientationAim = (float) Math.atan2(aimY - currentY, aimX - currentX);
                        float orientationDelta = (orientationAim - orientation) % ((float) Math.PI * 2);
                        //turnSpeed = maxTurnSpeed;
                        if (Math.abs(orientationDelta) > turnSpeed * dt) {//Если изменение угла не слишком маленькое
                            if ((orientationDelta <= -Math.PI) || ((orientationDelta > 0) && (orientationDelta <= Math.PI))) {
                                orientation = orientation + turnSpeed * dt;
                            } else {
                                orientation = orientation - turnSpeed * dt;
                            }
                        } else {
                            orientation = orientationAim;
                        }
                        if (currentSpeed > maxSpeed) {//Теряем скорость после ускорения
                            currentSpeed = Math.max(currentSpeed * (float) Math.pow(0.98d, dt * 30), maxSpeed);//FPS=30
                        } else {
                            currentSpeed = Math.min(maxSpeed, currentSpeed + dt * 400f * 0.5f);
                        }
                    }//Не агрессия
                }
            }

            //Позиция
            currentX = currentX + currentSpeed * (float) Math.cos(orientation) * dt;
            currentY = currentY + currentSpeed * (float) Math.sin(orientation) * dt;
            //Обновление для канвы - изменение размера
            canvasSize = (canvasSize + dt * 0.8f) % 1;
            canvasSnake = (canvasSnake + dt * currentSpeed / 100f * 0.5f) % 1;
            if (isEatingRightNow) {
                canvasEat = (canvasEat + dt * 0.8f * 2);
                if (canvasEat >= 2) {
                    canvasEat = 0;

                    this.evolve(false);


                    isEatingRightNow = false;
                }
            }

            segments[0].updateMapPosition(currentX + 302 * scaleForLittleOrBigFish * (float) Math.sin(orientation),
                    currentY - 302 * scaleForLittleOrBigFish * (float) Math.cos(orientation), orientation + (float) Math.PI);
            segments[1].updateMapPosition(currentX - 302 * scaleForLittleOrBigFish * (float) Math.sin(orientation),
                    currentY + 302 * scaleForLittleOrBigFish * (float) Math.cos(orientation), orientation + (float) Math.PI);

            segments[2].updateMapPosition(currentX + 278 * scaleForLittleOrBigFish * (float) Math.cos(orientation),
                    currentY + 278 * scaleForLittleOrBigFish * (float) Math.sin(orientation), orientation + (float) Math.PI);
            segments[3].updateMapPosition(currentX - 119 * scaleForLittleOrBigFish * (float) Math.cos(orientation),
                    currentY - 119 * scaleForLittleOrBigFish * (float) Math.sin(orientation), orientation + (float) Math.PI);
            segments[4].updateMapPosition(currentX - 516 * scaleForLittleOrBigFish * (float) Math.cos(orientation),
                    currentY - 516 * scaleForLittleOrBigFish * (float) Math.sin(orientation), orientation + (float) Math.PI);

        }
    }

    void draw(OpenGLRenderer openGLRenderer, Camera camera){
        if (isInDivision){
            for (int k = 0; k < Nsegm; k++) {
                if (divisionTimer-k*0.1f<0){
                    segments[k].draw(openGLRenderer);
                }
                else{
                    segments[k].drawDivision(openGLRenderer,divisionTimer-k*0.1f);
                }
            }
        }

        if (!isEaten) {

                if (isAgro) openGLRenderer.setColor(7);//Red
                openGLRenderer.drawSharkBody(currentX,currentY,this.getOrientationInDegrees(),Math.abs(canvasSnake - 0.5f) * 2f,0.1f);
                openGLRenderer.drawRing3Transfered(currentX,currentY,103*0.1f,this.getOrientationInDegrees(),774*0.1f,0);
                openGLRenderer.drawRing3Transfered(currentX,currentY,153*0.1f,this.getOrientationInDegrees(),724*0.1f,0);
                openGLRenderer.drawSharkmouthTransfered(currentX,currentY,0.1f,this.getOrientationInDegrees(),774*0.1f,0,(canvasSize+canvasEat)*360+this.getOrientationInDegrees());
                if (isAgro) openGLRenderer.setColor(0);//White

                for (int k = 0; k < Nsegm; k++) {
                    segments[k].draw(openGLRenderer);
                }


        }
    }


    public void findNearFood(Food [] foods_array, Protagonist protagonist){


        if (!isEaten) {
            hasPlayerInTarget=false;
            hasTarget=false;
            if (!isPanic) {
                if (!isEatingRightNow) {
                    float mouthDist = 294;
                    float curCheckX, curCheckY;//Положение текущей цели

                    //Попытка съесть протагониста или сагриться на него
                    for (int i = 0; i < protagonist.getNsegm(); i++) {
                        if (protagonist.isSegmentWeakPointAndUndamaged(i)) {
                            curCheckX=protagonist.getCurrentSegX(i);
                            curCheckY=protagonist.getCurrentSegY(i);


                            if (Math.pow(currentX + mouthDist * Math.cos(orientation) - curCheckX, 2) +
                                    Math.pow(currentY + mouthDist * Math.sin(orientation) - curCheckY, 2) <
                                    Math.pow(38 + protagonist.getCurrentSegRadius(i), 2)) {
                                protagonist.setDamaged(i);
                                isEatingRightNow = true;this.evolve(true);
                                return;
                            }//Если попало в рот

                            if (!isAgro){
                                if (!hasTarget) {//Первый раз за цикл
                                    if (Math.pow(currentX - curCheckX, 2) + Math.pow(currentY - curCheckY, 2) < Math.pow(agroQuietRadius, 2)) {
                                        //Если в большом агрорадиусе
                                        aimX = curCheckX;
                                        aimY = curCheckY;
                                        hasTarget = true;
                                    }
                                } else {
                                    if (Math.pow(currentX - curCheckX, 2) + Math.pow(currentY - curCheckY, 2) <
                                            Math.pow(currentX - aimX, 2) + Math.pow(currentY - aimY, 2)) {
                                        //Новое расстояние меньше
                                        aimX = curCheckX;
                                        aimY = curCheckY;
                                    }
                                }
                            }//Если не в агре - проверка на ближайшую цель в большом агрорадиусе


                            if (Math.pow(currentX - curCheckX, 2) + Math.pow(currentY - curCheckY, 2) < Math.pow(agroRadius, 2)){
                                //Если попало в малый агрорадиус
                                if (Math.abs(((float) Math.atan2(curCheckY - currentY, curCheckX - currentX) - orientation) % ((float) Math.PI * 2)) < agroAngle){
                                    //Если попало в агроугол
                                    if (!hasPlayerInTarget) {//Первый раз за цикл
                                        aimX = curCheckX;
                                        aimY = curCheckY;
                                        isAgro = true;
                                        hasPlayerInTarget = true;
                                        agroTimer = 0;
                                    } else {
                                        if (Math.pow(currentX - curCheckX, 2) + Math.pow(currentY - curCheckY, 2) < Math.pow(currentX - aimX, 2) + Math.pow(currentY - aimY, 2)) {
                                            //Новое расстояние меньше
                                            aimX = curCheckX;
                                            aimY = curCheckY;
                                        }
                                    }
                                }
                            }








                        }
                    }

                    if (isAgro){
                        return;
                    }//Если агры, нет смысла проверять еду

                    for (int i = 0; i < foods_array.length; i++){
                        if (!foods_array[i].isEaten()){
                            curCheckX=foods_array[i].getCurrentX();
                            curCheckY=foods_array[i].getCurrentY();

                            if (Math.pow(currentX + mouthDist * Math.cos(orientation) - curCheckX, 2) +
                                    Math.pow(currentY + mouthDist * Math.sin(orientation) - curCheckY, 2) <
                                    Math.pow(38 + foods_array[i].getCurrentRadius(), 2)) {
                                foods_array[i].setEaten();
                                isEatingRightNow = true;this.evolve(true);
                                return;
                            }//Если попало в рот

                            if ((Math.pow(currentX - curCheckX, 2) + Math.pow(currentY - curCheckY, 2) < Math.pow(agroRadius, 2)) &&
                                    (Math.abs(((float) Math.atan2(curCheckY - currentY, curCheckX - currentX) - orientation) % ((float) Math.PI * 2)) < agroAngle)) {
                                //Если попало в агрорадиус и агроугол
                                if (!hasTarget) {//Первый раз за цикл
                                    aimX = curCheckX;
                                    aimY = curCheckY;
                                    hasTarget = true;
                                } else {
                                    if (Math.pow(currentX - curCheckX, 2) + Math.pow(currentY - curCheckY, 2) < Math.pow(currentX - aimX, 2) + Math.pow(currentY - aimY, 2)) {
                                        //Новое расстояние меньше
                                        aimX = curCheckX;
                                        aimY = curCheckY;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        else{
            if (!isDivisionWrittenInFood){
                Log.wtf(LOG_TAG,"Разваливается на еду");
                int k=0;
                for (int i=0;i<Nsegm;i++){
                    if (segments[i].hasSaturationOrIsWeakPoint()){
                        while (k<foods_array.length-1){
                            if (foods_array[k].isEatenAndNotInvisible()){
                                foods_array[k].setInvisible((float)i*0.1f+0.1f,segments[i].getCurrentX(),segments[i].getCurrentY());
                                k++;
                                break;
                            }
                            k++;
                        }
                    }
                    isDivisionWrittenInFood=true;
                }
            }
        }
    }

    public void evolve(Boolean firstTimeCall){
        //Первым делом необходимо восстановить "глаза" как слабые точки

        if (firstTimeCall) {
            int k = 0;
            while (k < Nsegm) {
                if (segments[k].isWeakPointDamaged()) {//сработает только на weakPoint
                    segments[k].restoreWeakPoint();
                    evolvedAlready = true;
                    break;
                }
                k++;
            }
            evolvedAlready = false;
        }
        else{
            if (!evolvedAlready) {
                int k = 0;
                while (k < Nsegm) {
                    if (segments[k].isWeakPointDamaged()) {//сработает только на weakPoint
                        segments[k].restoreWeakPoint();
                        evolvedAlready = true;
                        break;
                    }//Восстановление weakPoint
                    if (!segments[k].getSaturation()) {
                        segments[k].setSaturation(true);
                        evolvedAlready = true;
                        break;
                    }//Обычный нажор

                    k++;
                }
            }
        }
    }

*/
}
