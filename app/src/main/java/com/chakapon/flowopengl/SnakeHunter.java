package com.chakapon.flowopengl;


import java.util.Random;

public class SnakeHunter {

    final String LOG_TAG = "SnakeHunter";


    private float currentX,currentY;//Положение на карте
    private float orientation;//Ориентация, в радианах
    private float orientationAim;
    private float currentSpeed, maxSpeed, maxBoostSpeed;//В пикселях в секунду
    private float maxTurnSpeed;//В радианах в секунду

    //private float screenSizeX, screenSizeY;

    private float canvasSize;
    private float canvasSnake;

    Segment [] segments;
    int Nsegm;

    boolean isEatingRightNow;
    private float canvasEat;//Для отображения анимации поедания

    final int NsegmMax=16;

    boolean itWasVoidFood=false;

    boolean isPanic=false;
    float panicTimer=0, panicMaxTime=2;

    boolean hasTarget=false;
    boolean isEaten=false;
    float aimX, aimY;

    float agroRadius=200;

    boolean isInDivision=false;
    float divisionTimer=0;
    boolean isDivisionWrittenInFood=false;

    SnakeHunter(int Nsegm_, int NsegmEvolved_) {


        currentX= (float) (Math.random()-0.5)*2*1000;
        currentY= (float) (Math.random()-0.5)*2*1000;

        aimX=(float) (Math.random()-0.5)*2*1000;
        aimY=(float) (Math.random()-0.5)*2*1000;

        orientationAim = 0;
        maxTurnSpeed = (float) ((8f + (float) Math.random() * 3f)*10 / 180f * Math.PI);//*20
        maxSpeed = 120;
        maxBoostSpeed = 250;
        //screenSizeX = screenSizeX_;
        //screenSizeY = screenSizeY_;

        orientation= (float) (Math.random()*Math.PI*2f);
        currentSpeed=(float) (Math.random()*maxSpeed);


        canvasSize = canvasSnake = 0;
        canvasEat = 0;


        isEatingRightNow = false;////////////////////////////////////////////////////////////////////////////////////////////////////Почему тут?


        if (Nsegm_>NsegmMax){//Нельзя, чтобы превышало, иначе будет краш
            Nsegm=NsegmMax;
        }
        else{
            Nsegm=Nsegm_;
        }


        segments = new Segment[NsegmMax];
        for (int k = 0; k < Nsegm; k++) {
            if (k!=Nsegm-1){
                if (k==0)
                {
                    segments[k] = new Segment(currentX, currentY, orientation+(float)(Math.random()-0.5)*2*(30f/180f*(float)Math.PI), 0);
                    segments[0].setFirst();

                }
                else {
                    segments[k] = new Segment(segments[k-1].getCurrentX(), segments[k-1].getCurrentY(), segments[k-1].getOrientation()+(float)(Math.random()-0.5)*2*(30f/180f*(float)Math.PI), 0);
                }
            }
            else {
                segments[k] = new Segment(segments[k-1].getCurrentX(), segments[k-1].getCurrentY(), segments[k-1].getOrientation()+(float)(Math.random()-0.5)*2*(30f/180f*(float)Math.PI), 1);//Хвостик
            }


        }

        segments[0].setSaturation(true);
        segments[0].setWeakPoint();


        //segments[0].changeType(2);
        Random random = new Random();
        int currentRandom;
        //int rand=(int)(Math.random() * Nsegm - 1);
        for (int k = 0; k < NsegmEvolved_-1; k++) {
            currentRandom=random.nextInt(Nsegm-1);
            if (!segments[currentRandom].isWeakPoint()) {
                segments[currentRandom].setWeakPoint();
                //segments[currentRandom].changeType(2);
            }
            else{
                for (int j = 1; j < Nsegm-1; j++) {//C 1, т.к. нулевой всегда weakpoint
                    if (!segments[j].isWeakPoint()){
                        segments[j].setWeakPoint();
                        //segments[j].changeType(2);
                        break;
                    }
                }
            }
        }



    }

    public boolean isEaten() {
        return isEaten;
    }
    public int getNsegm(){return Nsegm;}
    public boolean isSegmentWeakPointAndUndamaged(int nSegm){return segments[nSegm].isSegmentWeakPointAndUndamaged();};

    public float getCurrentSegX(int nSegm){return segments[nSegm].getCurrentX();}
    public float getCurrentSegY(int nSegm){return segments[nSegm].getCurrentY();}
    public float getOrientationInDegrees(){ return orientation*180/(float)Math.PI;}
    public float getCurrentSegRadius(int nSegm){return segments[nSegm].getCurrentRadius();}

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
            panicTimer=0;
        }//Больно! Надо ускориться! Паника!
    }




    public void findNearFood(Food [] foods_array){


        if (!isEaten){
            if (!isPanic) {
                if (!isEatingRightNow) {
                    float mouthDist = 30;
                    float mouthRadius = 20;
                    hasTarget = false;

                    //Поедание мелких
                    for (int i = 0; i < foods_array.length; i++) {
                        if (!foods_array[i].isEaten()) {
                            if (Math.pow(currentX + mouthDist * Math.cos(orientation) - foods_array[i].getCurrentX(), 2) +
                                    Math.pow(currentY + mouthDist * Math.sin(orientation) - foods_array[i].getCurrentY(), 2) <
                                    Math.pow(20 + foods_array[i].getCurrentRadius(), 2)) {
                                isEatingRightNow = true;
                                foods_array[i].setEaten();
                                return;
                            }//Съедена еда

                            if (Math.pow(currentX - foods_array[i].getCurrentX(), 2) + Math.pow(currentY - foods_array[i].getCurrentY(), 2) < Math.pow(agroRadius, 2)) {
                                if (hasTarget) {
                                    if (Math.pow(currentX - foods_array[i].getCurrentX(), 2) + Math.pow(currentY - foods_array[i].getCurrentY(), 2) < Math.pow(currentX - aimX, 2) + Math.pow(currentY - aimY, 2)) {
                                        aimX = foods_array[i].getCurrentX();
                                        aimY = foods_array[i].getCurrentY();
                                    }//Проверка, что ближе: старая цель или новая
                                } else {
                                    aimX = foods_array[i].getCurrentX();
                                    aimY = foods_array[i].getCurrentY();
                                    hasTarget = true;
                                }
                            }//Еда попала в агрорадиус
                        }
                    }
                }
            }
        }
        else {
            if (!isDivisionWrittenInFood){
                //Log.wtf(LOG_TAG,"Разваливается на еду");
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

    public void updateMapPosition(float dt){

        if (isInDivision){
            if (divisionTimer>(NsegmMax+1)*0.1+1) isInDivision=false;//С запасом. А так NsegmMax*0.1+1
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
            }


            if (!isPanic) {
                if (!hasTarget) {
                    if ((Math.abs(aimX - currentX) < (200)) & (Math.abs(aimY - currentY) < (200))) {
                        aimX = (float) (Math.random() - 0.5) * 2 * 1000;
                        aimY = (float) (Math.random() - 0.5) * 2 * 1000;
                        //return;
                    }
                    //Если цель близко
                    //Рандомить цель (и при задании!)
                    //Если приближается к цели или время прошло - менять (время - постоянная неспокойного поиска))
                } else {
                    //Уменьшать скорость при приближении?
                }


                //Угол
                orientationAim = (float) Math.atan2(aimY - currentY, aimX - currentX);

                float orientationDelta = (orientationAim - orientation) % ((float) Math.PI * 2);

                if (Math.abs(orientationDelta) > maxTurnSpeed * dt) {//Если изменение угла не слишком маленькое
                    if ((orientationDelta <= -Math.PI) || ((orientationDelta > 0) && (orientationDelta <= Math.PI))) {
                        orientation = orientation + maxTurnSpeed * dt;
                    } else {
                        orientation = orientation - maxTurnSpeed * dt;
                    }
                } else {
                    orientation = orientationAim;
                }

            }
            //Скорость

            //if (isPressed) {
            if (isPanic) {
                currentSpeed = Math.min(maxBoostSpeed, currentSpeed + dt * 400);//400 - прирост скорости?
            } else {
                if (currentSpeed > maxSpeed) {//Теряем скорость после ускорения
                    //currentSpeed = Math.max(currentSpeed*0.98f*dt*30,maxSpeed);//FPS=30
                    currentSpeed = Math.max(currentSpeed * (float) Math.pow(0.98d, dt * 30), maxSpeed);//FPS=30//////////////////////////////////////////////////////Зависимость от фпс! (?)
                } else {
                    currentSpeed = Math.min(maxSpeed, currentSpeed + dt * 400f * 0.5f);
                }
            }

            //} else {
            //    currentSpeed = currentSpeed * (float) Math.pow(0.95d, dt * 30);//FPS=30
            //}


            // currentSpeed=Math.max(maxSpeed*0.1f,currentSpeed);Ненулевая минимальная скорость

            //Позиция
            currentX = currentX + currentSpeed * (float) Math.cos(orientation) * dt;
            currentY = currentY + currentSpeed * (float) Math.sin(orientation) * dt;

            //Log.wtf(LOG_TAG, "touch ("+touchX+","+touchY+") curX=("+currentX+","+currentY+")");
            //Log.wtf(LOG_TAG, "addX="+currentSpeed * (float) Math.cos(orientation) * dt+" addY="+currentSpeed * (float) Math.sin(orientation) * dt);
            //return "touchX="+(int)touchX+" touchY="+(int)touchY;


            //Обновление для канвы - изменение размера
            canvasSize = (canvasSize + dt * 0.8f) % 1;
            canvasSnake = (canvasSnake + dt * currentSpeed / maxSpeed) % 1;
            if (isEatingRightNow) {
                canvasEat = (canvasEat + dt * 0.8f * 2);
                if (canvasEat >= 2) {
                    canvasEat = 0;
                    if (itWasVoidFood) {//Не насыщать клетки, если перешёл на другой уровень
                        itWasVoidFood = false;
                    } else {
                        this.evolveLittle();
                    }
                    isEatingRightNow = false;
                }
            }


            for (int k = 0; k < Nsegm; k++) {
                if (k == 0) {
                    segments[k].updateMapPosition(currentX, currentY, dt, currentSpeed);
                } else {
                    segments[k].updateMapPosition(segments[k - 1].getCurrentX(), segments[k - 1].getCurrentY(), dt, currentSpeed);
                }
            }

            //hasTarget = false;
        }
    }




    public void draw(OpenGLRenderer openGLRenderer, Camera camera){
        if (isInDivision){
            for (int k = 0; k < Nsegm; k++) {
                if (divisionTimer-k*0.1f<0){
                    segments[k].drawWithScale(openGLRenderer,k,Nsegm);
                }
                else{
                    segments[k].drawDivision(openGLRenderer,divisionTimer-k*0.1f);////////////Фуньк-пуньк с задержкой
                }
            }
        }

        if (!isEaten) {


            openGLRenderer.drawRing2(currentX,currentY,2*0.7f*5.3f);
            openGLRenderer.drawMouth(currentX,currentY,this.getOrientationInDegrees(),1-Math.abs(canvasEat%1-0.5f)*2f);
            openGLRenderer.drawSquareTransfered(currentX,currentY,3.36f,this.getOrientationInDegrees(),-21,0);

            for (int k=0;k<Nsegm;k++){
                segments[k].drawWithScale(openGLRenderer,k,Nsegm);
            }
        }
    }




    public void evolveLittle(){//Нажрать кружочки
        int saturationSum=0;
        for (int k=0;k<Nsegm-1;k++) {
            if (segments[k].getSaturation())
                saturationSum++;
        }
        if (saturationSum==Nsegm-1){
            this.evolveBig();
        }
        else{
            //Ищи наименьшую пустую и наполняй
            int k=0;
            while (k<Nsegm-1){
                if (!segments[k].getSaturation()){
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
