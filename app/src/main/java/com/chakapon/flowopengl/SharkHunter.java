package com.chakapon.flowopengl;


import android.util.Log;

public class SharkHunter {
    final String LOG_TAG = "SharkHunter";


    private float currentX, currentY;//Положение на карте
    private float orientation;//Ориентация, в радианах
    private float orientationAim;
    private float currentSpeed, maxSpeed, maxBoostSpeed;//В пикселях в секунду
    private float turnSpeed;//,maxTurnSpeed;//В радианах в секунду

    //private float screenSizeX, screenSizeY;

    private float canvasSize;
    private float canvasSnake;

    Segment[] segments;
    int Nsegm;

    boolean isEatingRightNow = false;;
    private float canvasEat;//Для отображения анимации поедания

    boolean isPanic = false;
    float panicTimer = 0, panicMaxTime = 2;

    boolean hasTarget = false; boolean hasPlayerInTarget=false;
    boolean isEaten = false;
    float aimX, aimY;


    boolean isInDivision = false;
    float divisionTimer = 0;
    boolean isDivisionWrittenInFood = false;

    float scaleForLittleOrBigFish = 0.1f;

    float agroTimer = 8, agroMaxTimeWithoutAim = 2, agroAngle = (float) (60f / 180f * Math.PI), agroRadius, agroQuietRadius;// = 600;//Угол в каждую сторону
    boolean isAgro = false;//, shouldILoseAgro = false;
    boolean evolvedAlready=false;

    SharkHunter() {
        currentX = (float) (Math.random()-0.5)*2*1000;
        currentY = (float) (Math.random()-0.5)*2*1000;

        aimX = (float) (Math.random() - 0.5) * 2 * 1000;
        aimY = (float) (Math.random() - 0.5) * 2 * 1000;

        orientationAim = 0;
        turnSpeed = (float) ((8f + (float) Math.random() * 3f) * 5 / 180f * Math.PI);//*20

        //Log.wtf(LOG_TAG,"turnspeed "+turnSpeed*180/3.1415);

        maxSpeed = 100*(scaleForLittleOrBigFish*10);
        maxBoostSpeed = maxSpeed*3;
        //screenSizeX = screenSizeX_;
        //screenSizeY = screenSizeY_;


        agroRadius=maxBoostSpeed/turnSpeed*1.3f;
        agroQuietRadius=agroRadius*1.2f;

        orientation = (float) (Math.random() * Math.PI * 2f);
        currentSpeed = (float) (Math.random() * maxSpeed);


        canvasSize = canvasSnake = canvasEat = 0;

        Nsegm = 5;


        segments = new Segment[Nsegm];

        segments[0] = new Segment(3,scaleForLittleOrBigFish*5);//Глаза
        segments[1] = new Segment(3,scaleForLittleOrBigFish*5);//Автоматом с насыщением и weakpoint
        segments[2] = new Segment(0,scaleForLittleOrBigFish*7);
        segments[3] = new Segment(0,scaleForLittleOrBigFish*6);//Ячейки по центру
        segments[4] = new Segment(0,scaleForLittleOrBigFish*5);
    }

    public boolean isEaten() {
        return isEaten;
    }
    public int getNsegm(){return Nsegm;}
    public float getCurrentSegX(int nSegm){return segments[nSegm].getCurrentX();}
    public float getCurrentSegY(int nSegm){return segments[nSegm].getCurrentY();}
    public float getCurrentSegRadius(int nSegm){return segments[nSegm].getCurrentRadius();}

    public float getOrientationInDegrees(){ return orientation*180/(float)Math.PI;}
    public boolean isSegmentWeakPointAndUndamaged(int nSegm){return segments[nSegm].isSegmentWeakPointAndUndamaged();};

    public void setDamaged(int nSegm){
        segments[nSegm].setWeakPointDamaged();///////////////////////////////////////////////Можно будет объединить


        int weakUndamagedSum=0;
        for (int k=0;k<Nsegm-1;k++) {
            if (segments[k].isSegmentWeakPointAndUndamaged())///////////////////////////////Тут должна быть проверка на слабые точки аля weakpoints
                weakUndamagedSum++;
        }

        if (weakUndamagedSum==0){
            isInDivision=true;
            divisionTimer=0;
            isEaten=true;/////////////////////////////////////////////////////////////////////////////И вызов ф-ии распада
        }//Вас сожрали нафиг ;р
        else{
            isPanic=true;
            isAgro=false;
            panicTimer=0;
            //Log.wtf(LOG_TAG, "Паника!");
        }//Больно! Надо ускориться! Паника!
    }


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
            if ((Math.abs(currentX - camera.getCurrentX()) < camera.getGamefieldHalfX() + 560 * scaleForLittleOrBigFish) && (Math.abs(currentY - camera.getCurrentY()) < camera.getGamefieldHalfY() + 560 * scaleForLittleOrBigFish)){
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
    }


    public void findNearFood(Food [] foods_array, Protagonist protagonist){


        if (!isEaten) {
            hasPlayerInTarget=false;
            hasTarget=false;
            if (!isPanic) {
                if (!isEatingRightNow) {
                    float mouthDist = 774 * scaleForLittleOrBigFish;
                    float curCheckX, curCheckY;//Положение текущей цели

                    //Попытка съесть протагониста или сагриться на него
                    for (int i = 0; i < protagonist.getNsegm(); i++) {
                        if (protagonist.isSegmentWeakPointAndUndamaged(i)) {
                            curCheckX=protagonist.getCurrentSegX(i);
                            curCheckY=protagonist.getCurrentSegY(i);


                            if (Math.pow(currentX + mouthDist * Math.cos(orientation) - curCheckX, 2) +
                                    Math.pow(currentY + mouthDist * Math.sin(orientation) - curCheckY, 2) <
                                    Math.pow(103 * scaleForLittleOrBigFish + protagonist.getCurrentSegRadius(i), 2)) {
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
                                    Math.pow(103 * scaleForLittleOrBigFish + foods_array[i].getCurrentRadius(), 2)) {
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

}