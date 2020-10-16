package com.chakapon.flowopengl;


import android.util.Log;

public class Food {

    final String LOG_TAG = "Food";

    private float currentX,currentY;//Положение на карте
    private float orientation;//Ориентация, в радианах
    private float orientationAim;
    private float currentSpeed, maxSpeed;//В пикселях в секунду
    private float turnSpeed;//В радианах в секунду
    private float aimX,aimY;

    //private float screenSizeX, screenSizeY;

    private float canvasSize;
    private float canvasSnake;

    int type;

    float currentRadius;

    boolean isEaten;

    boolean isInvisible=false;
    float invisibleTimer;

    float flockieBirdRadius=50;

    float multiplSize, multiplSnake;

    Food(){
        currentX=(float) (Math.random()-0.5)*2*1000;
        currentY=(float) (Math.random()-0.5)*2*1000;
        orientation= (float) (Math.random()*Math.PI*2f);
        maxSpeed=45+(float)Math.random()*10f;
        currentSpeed=(float) (Math.random()*maxSpeed);
        turnSpeed=(float)(50f / 180f * Math.PI);
        this.goToRandomLocation();
        canvasSize=(float)Math.random();
        canvasSnake=(float)Math.random();
        this.randomizeType();
        this.setRadius();
        isEaten=false;
    }
    Food(float curX_, float curY_, int birdType){
        currentX=curX_+(float) (Math.random()-0.5)*2*flockieBirdRadius;
        currentY=curY_+(float) (Math.random()-0.5)*2*flockieBirdRadius;
        orientation= (float) (Math.random()*Math.PI*2f);
        maxSpeed=45+(float)Math.random()*10f+200;
        currentSpeed=(float) (Math.random()*maxSpeed);
        turnSpeed=(float)(50f / 180f * Math.PI)+1;
        this.goToRandomLocation();
        canvasSize=(float)Math.random();
        canvasSnake=(float)Math.random();
        type=birdType;
        this.setRadius();
        isEaten=false;
    }//Для стайки
    Food(int testType){
        currentX=testType*50-300;// (float) (Math.random()-0.5)*2*1000;
        currentY=400;// (float) (Math.random()-0.5)*2*1000;
        orientation= (float) (Math.PI/2f);;//(float) (Math.random()*Math.PI*2f);
        //orientationAim=0;
        maxSpeed=45+(float)Math.random()*10f;
        currentSpeed=(float) (Math.random()*maxSpeed);
        turnSpeed=(float)(50f / 180f * Math.PI);

        canvasSize=(float)Math.random();
        canvasSnake=(float)Math.random();

        type=testType;//this.randomizeType();

        this.setRadius();
        isEaten=false;

    }//Удалить в релизе или не использовать
    Food(String s){
        if (s=="angryBoss1Food")
        {
            maxSpeed=95+(float)Math.random()*10f;
            turnSpeed=(float)(50f / 180f * Math.PI)*2;
            canvasSize=(float)Math.random();
            canvasSnake=(float)Math.random();
            type=2;
            isEaten=true;
        }
        else
        {Log.wtf(LOG_TAG,"Food init error");}
    }


    private void randomizeType(){
        float Nreverse=1/7f;//0-6
        double random=Math.random();// [0;1)
        int A=0;
        while (true){
            if (random<Nreverse){
                type=A;break;
            }
            random -= Nreverse;
            A++;
            //Log.wtf(LOG_TAG,"inf loop "+random+" "+A);
        }
    }
    private void setRadius(){
        switch (type){
            case 0:
                currentRadius=20;
                break;
            case 1:
                currentRadius=20;
                break;
            case 2:
                currentRadius=15;
                break;
            case 3:
                currentRadius=10;
                break;
            case 4:
                currentRadius=10;
                break;
            case 5:
                currentRadius=14;
                break;
            case 6:
                currentRadius=20;
                break;
            case 7:
                currentRadius=20;
                break;
            case 8:
                currentRadius=20;
                break;
            default:
                currentRadius=20;
                Log.wtf(LOG_TAG,"Тип еды задан неправильно. Радиус");
                break;
        }
    }

    public void updateMapPosition(float dt){
        if (isInvisible){
            if (invisibleTimer>0) {
                isInvisible=false;
                isEaten=false;
            }
            invisibleTimer=invisibleTimer+dt;
        }

        if (!isEaten) {

            if (Math.sqrt(Math.pow((currentX - aimX), 2) + Math.pow((currentY - aimY), 2)) < 5f) {
                goToRandomLocation();
            }

            //Угол
            orientationAim = (float) Math.atan2(aimY - currentY, aimX - currentX);
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
            currentSpeed = Math.min(maxSpeed, currentSpeed + dt * 50f);

            //Позиция
            currentX = currentX + currentSpeed * (float) Math.cos(orientation) * dt;
            currentY = currentY + currentSpeed * (float) Math.sin(orientation) * dt;

            //Обновление для канвы - изменение размера
            switch (type) {
                case 0:
                    canvasSize = (canvasSize + dt * 0.8f) % 1;
                    canvasSnake = (canvasSnake + dt * currentSpeed / 100f) % 1;
                    multiplSize = Math.abs(canvasSize - 0.5f) * 2f;
                    multiplSnake = Math.abs(canvasSnake - 0.5f) * 2f;
                    break;
                case 1:
                    canvasSize = (canvasSize + dt * 0.8f) % 1;
                    multiplSize = (Math.abs(canvasSize - 0.5f) * 2f * 0.3f + 0.7f)*20;
                    break;
                case 2:
                    canvasSize = (canvasSize + dt * 0.8f) % 1;
                    canvasSnake = (canvasSnake + 1.8f * dt * currentSpeed / 100f) % 1;//Хвостик у головастика должен двигаться быстрее
                    multiplSize = 1 - (float) Math.pow(Math.abs(Math.abs(canvasSize - 0.5f) * 2f - 0.5f) * 2f, 2);//Вся эта конструкция для увеличения частоты вдвое и быстрого захлопывания пасти
                    multiplSnake = Math.abs(canvasSnake - 0.5f) * 2f;
                    break;
                case 3:
                    canvasSnake = (canvasSnake + dt * currentSpeed / 100f) % 1;
                    multiplSnake = Math.abs(canvasSnake - 0.5f) * 2f;
                    break;
                case 4:
                    canvasSnake = (canvasSnake + 1.5f * dt * currentSpeed / 100f) % 1;//Хвостик у однохвостого должен двигаться быстрее
                    multiplSnake = Math.abs(canvasSnake - 0.5f) * 2f;
                    break;
                case 5:
                    canvasSnake = (canvasSnake + dt * currentSpeed / 100f) % 1;
                    multiplSnake = Math.abs(canvasSnake - 0.5f) * 2f;
                    break;
                case 6:
                    canvasSnake = (canvasSnake + dt * currentSpeed / 100f) % 1;
                    multiplSnake = Math.abs(canvasSnake - 0.5f) * 2f;
                    break;
                case 7://Переход
                    canvasSize = (canvasSize + dt * 0.8f) % 1;
                    //canvasSnake = (canvasSnake + dt * currentSpeed / 100f) % 1;
                    multiplSize=Math.abs(canvasSize-0.5f)*2f*0.3f+0.7f;
                    multiplSnake=(Math.abs(canvasSize-0.5f)*(-2f)*0.3f+0.7f)*40;//вместо мулт сайз 2
                    break;
                case 8://Переход
                    canvasSize = (canvasSize + dt * 0.8f) % 1;
                    //canvasSnake = (canvasSnake + dt * currentSpeed / 100f) % 1;
                    multiplSize=Math.abs(canvasSize-0.5f)*2f*0.3f+0.7f;
                    multiplSnake=(Math.abs(canvasSize-0.5f)*(-2f)*0.3f+0.7f)*33;//вместо мулт сайз 2
                    break;
                default:
                    Log.wtf(LOG_TAG,"Тип еды задан неправильно. Обновление размеров "+type);
                    break;
            }
        }
    }
    public void updateMapPositionTest(float dt){
           currentSpeed = Math.min(maxSpeed, currentSpeed + dt * 50f);

            //Обновление для канвы - изменение размера
            switch (type) {
                case 0:
                    canvasSize = (canvasSize + dt * 0.8f) % 1;
                    canvasSnake = (canvasSnake + dt * currentSpeed / 100f) % 1;
                    multiplSize = Math.abs(canvasSize - 0.5f) * 2f;
                    multiplSnake = Math.abs(canvasSnake - 0.5f) * 2f;
                    break;
                case 1:
                    canvasSize = (canvasSize + dt * 0.8f) % 1;
                    multiplSize = (Math.abs(canvasSize - 0.5f) * 2f * 0.3f + 0.7f)*20;
                    break;
                case 2:
                    canvasSize = (canvasSize + dt * 0.8f) % 1;
                    canvasSnake = (canvasSnake + 1.8f * dt * currentSpeed / 100f) % 1;//Хвостик у головастика должен двигаться быстрее
                    multiplSize = 1 - (float) Math.pow(Math.abs(Math.abs(canvasSize - 0.5f) * 2f - 0.5f) * 2f, 2);//Вся эта конструкция для увеличения частоты вдвое и быстрого захлопывания пасти
                    multiplSnake = Math.abs(canvasSnake - 0.5f) * 2f;
                    break;
                case 3:
                    canvasSnake = (canvasSnake + dt * currentSpeed / 100f) % 1;
                    multiplSnake = Math.abs(canvasSnake - 0.5f) * 2f;
                    break;
                case 4:
                    canvasSnake = (canvasSnake + 1.5f * dt * currentSpeed / 100f) % 1;//Хвостик у однохвостого должен двигаться быстрее
                    multiplSnake = Math.abs(canvasSnake - 0.5f) * 2f;
                    break;
                case 5:
                    canvasSnake = (canvasSnake + dt * currentSpeed / 100f) % 1;
                    multiplSnake = Math.abs(canvasSnake - 0.5f) * 2f;
                    break;
                case 6:
                    canvasSnake = (canvasSnake + dt * currentSpeed / 100f) % 1;
                    multiplSnake = Math.abs(canvasSnake - 0.5f) * 2f;
                    break;
                case 7://Переход
                    canvasSize = (canvasSize + dt * 0.8f) % 1;
                    //canvasSnake = (canvasSnake + dt * currentSpeed / 100f) % 1;
                    multiplSize=Math.abs(canvasSize-0.5f)*2f*0.3f+0.7f;
                    multiplSnake=(Math.abs(canvasSize-0.5f)*(-2f)*0.3f+0.7f)*40;//вместо мулт сайз 2
                    break;
                case 8://Переход
                    canvasSize = (canvasSize + dt * 0.8f) % 1;
                    //canvasSnake = (canvasSnake + dt * currentSpeed / 100f) % 1;
                    multiplSize=Math.abs(canvasSize-0.5f)*2f*0.3f+0.7f;
                    multiplSnake=(Math.abs(canvasSize-0.5f)*(-2f)*0.3f+0.7f)*33;//вместо мулт сайз 2
                    break;
                default:
                    Log.wtf(LOG_TAG,"Тип еды задан неправильно. Расчёт положения "+type);
                    break;
            }




    }
    public void updateMapPositionBird(float dt, boolean isFlockieBirdInPanic,float curFlockieBirdX, float curFlockieBirdY){
        if (!isEaten) {
            if (!isFlockieBirdInPanic){
                if (Math.sqrt(Math.pow((currentX - curFlockieBirdX), 2) + Math.pow((currentY - curFlockieBirdY), 2)) > 300) {
                    aimX = (float) (Math.random()-0.5)*2*flockieBirdRadius+curFlockieBirdX;
                    aimY = (float) (Math.random()-0.5)*2*flockieBirdRadius+curFlockieBirdY;
                }//Если улетел далеко от стайки
                else if((Math.abs(aimX - currentX) < 5) && (Math.abs(aimY - currentY) < 5)){
                    aimX = (float) (Math.random()-0.5)*2*flockieBirdRadius+curFlockieBirdX;
                    aimY = (float) (Math.random()-0.5)*2*flockieBirdRadius+curFlockieBirdY;
                }//Если достиг текущей цели
                orientationAim = (float) Math.atan2(aimY - currentY, aimX - currentX);
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


            currentSpeed = Math.min(maxSpeed, currentSpeed + dt * 50f);



            //Позиция
            currentX = currentX + currentSpeed * (float) Math.cos(orientation) * dt;
            currentY = currentY + currentSpeed * (float) Math.sin(orientation) * dt;

            switch (type) {
                case 0:
                    canvasSize = (canvasSize + dt * 0.8f) % 1;
                    canvasSnake = (canvasSnake + dt * currentSpeed / 100f) % 1;
                    multiplSize = Math.abs(canvasSize - 0.5f) * 2f;
                    multiplSnake = Math.abs(canvasSnake - 0.5f) * 2f;
                    break;
                case 1:
                    canvasSize = (canvasSize + dt * 0.8f) % 1;
                    multiplSize = (Math.abs(canvasSize - 0.5f) * 2f * 0.3f + 0.7f)*20;
                    break;
                case 2:
                    canvasSize = (canvasSize + dt * 0.8f) % 1;
                    canvasSnake = (canvasSnake + 1.8f * dt * currentSpeed / 100f) % 1;//Хвостик у головастика должен двигаться быстрее
                    multiplSize = 1 - (float) Math.pow(Math.abs(Math.abs(canvasSize - 0.5f) * 2f - 0.5f) * 2f, 2);//Вся эта конструкция для увеличения частоты вдвое и быстрого захлопывания пасти
                    multiplSnake = Math.abs(canvasSnake - 0.5f) * 2f;
                    break;
                case 3:
                    canvasSnake = (canvasSnake + dt * currentSpeed / 100f) % 1;
                    multiplSnake = Math.abs(canvasSnake - 0.5f) * 2f;
                    break;
                case 4:
                    canvasSnake = (canvasSnake + 1.5f * dt * currentSpeed / 100f) % 1;//Хвостик у однохвостого должен двигаться быстрее
                    multiplSnake = Math.abs(canvasSnake - 0.5f) * 2f;
                    break;
                case 5:
                    canvasSnake = (canvasSnake + dt * currentSpeed / 100f) % 1;
                    multiplSnake = Math.abs(canvasSnake - 0.5f) * 2f;
                    break;
                case 6:
                    canvasSnake = (canvasSnake + dt * currentSpeed / 100f) % 1;
                    multiplSnake = Math.abs(canvasSnake - 0.5f) * 2f;
                    break;
                case 7://Переход
                    canvasSize = (canvasSize + dt * 0.8f) % 1;
                    //canvasSnake = (canvasSnake + dt * currentSpeed / 100f) % 1;
                    multiplSize=Math.abs(canvasSize-0.5f)*2f*0.3f+0.7f;
                    multiplSnake=(Math.abs(canvasSize-0.5f)*(-2f)*0.3f+0.7f)*40;//вместо мулт сайз 2
                    break;
                case 8://Переход
                    canvasSize = (canvasSize + dt * 0.8f) % 1;
                    //canvasSnake = (canvasSnake + dt * currentSpeed / 100f) % 1;
                    multiplSize=Math.abs(canvasSize-0.5f)*2f*0.3f+0.7f;
                    multiplSnake=(Math.abs(canvasSize-0.5f)*(-2f)*0.3f+0.7f)*33;//вместо мулт сайз 2
                    break;
                default:
                    Log.wtf(LOG_TAG,"Тип еды задан неправильно. Обновление размеров "+type);
                    break;
            }
        }

    }
    public void updateMapPositionAngryBoss(float dt, Protagonist protagonist) {

        if (isInvisible) {//Для этой "еды" невидимость работает как видимость, т.е. наоборот
            if (invisibleTimer > 0) {
                isInvisible = false;//Т.е. для него - время исчезать
                isEaten = true;
                return;
            }
            //else isEaten=false;
            invisibleTimer = invisibleTimer + dt;//Минус потому что должен потом исчезнуть


            //Приближение к ближайшей части ГГ
            //Попытка съесть протагониста или сагриться на него
            float curCheckX, curCheckY;
            boolean firstTime = true;
            for (int i = 0; i < protagonist.getNsegm(); i++) {
                if (protagonist.isSegmentWeakPointAndUndamaged(i)) {
                    curCheckX = protagonist.getCurrentSegX(i);
                    curCheckY = protagonist.getCurrentSegY(i);


                    if (Math.pow(currentX - curCheckX, 2) +
                            Math.pow(currentY - curCheckY, 2) <
                            Math.pow(currentRadius + protagonist.getCurrentSegRadius(i), 2)) {
                        protagonist.setDamaged(i);
                        invisibleTimer = invisibleTimer + 50;//Чтобы наверняка исчезло после съедания
                        return;
                    }//Если попало в рот

                    if (firstTime) {
                        aimX = curCheckX;
                        aimY = curCheckY;
                    } else {
                        //Проверка, что ближе - новое или уже найденное
                        if (Math.pow(currentX - curCheckX, 2) + Math.pow(currentY - curCheckY, 2) <
                                Math.pow(currentX - aimX, 2) + Math.pow(currentY - aimY, 2)) {
                            //Новое расстояние меньше
                            aimX = curCheckX;
                            aimY = curCheckY;
                        }
                    }


                    //Угол
                    orientationAim = (float) Math.atan2(aimY - currentY, aimX - currentX);
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
                    currentSpeed = Math.min(maxSpeed, currentSpeed + dt * 50f);

                    //Позиция
                    currentX = currentX + currentSpeed * (float) Math.cos(orientation) * dt;
                    currentY = currentY + currentSpeed * (float) Math.sin(orientation) * dt;

                    //Обновление для канвы - изменение размера
                    switch (type) {
                        case 0:
                            canvasSize = (canvasSize + dt * 0.8f) % 1;
                            canvasSnake = (canvasSnake + dt * currentSpeed / 100f) % 1;
                            multiplSize = Math.abs(canvasSize - 0.5f) * 2f;
                            multiplSnake = Math.abs(canvasSnake - 0.5f) * 2f;
                            break;
                        case 1:
                            canvasSize = (canvasSize + dt * 0.8f) % 1;
                            multiplSize = (Math.abs(canvasSize - 0.5f) * 2f * 0.3f + 0.7f) * 20;
                            break;
                        case 2:
                            canvasSize = (canvasSize + dt * 0.8f) % 1;
                            canvasSnake = (canvasSnake + 1.8f * dt * currentSpeed / 100f) % 1;//Хвостик у головастика должен двигаться быстрее
                            multiplSize = 1 - (float) Math.pow(Math.abs(Math.abs(canvasSize - 0.5f) * 2f - 0.5f) * 2f, 2);//Вся эта конструкция для увеличения частоты вдвое и быстрого захлопывания пасти
                            multiplSnake = Math.abs(canvasSnake - 0.5f) * 2f;
                            break;
                        case 3:
                            canvasSnake = (canvasSnake + dt * currentSpeed / 100f) % 1;
                            multiplSnake = Math.abs(canvasSnake - 0.5f) * 2f;
                            break;
                        case 4:
                            canvasSnake = (canvasSnake + 1.5f * dt * currentSpeed / 100f) % 1;//Хвостик у однохвостого должен двигаться быстрее
                            multiplSnake = Math.abs(canvasSnake - 0.5f) * 2f;
                            break;
                        case 5:
                            canvasSnake = (canvasSnake + dt * currentSpeed / 100f) % 1;
                            multiplSnake = Math.abs(canvasSnake - 0.5f) * 2f;
                            break;
                        case 6:
                            canvasSnake = (canvasSnake + dt * currentSpeed / 100f) % 1;
                            multiplSnake = Math.abs(canvasSnake - 0.5f) * 2f;
                            break;
                        case 7://Переход
                            canvasSize = (canvasSize + dt * 0.8f) % 1;
                            //canvasSnake = (canvasSnake + dt * currentSpeed / 100f) % 1;
                            multiplSize = Math.abs(canvasSize - 0.5f) * 2f * 0.3f + 0.7f;
                            multiplSnake = (Math.abs(canvasSize - 0.5f) * (-2f) * 0.3f + 0.7f) * 40;//вместо мулт сайз 2
                            break;
                        case 8://Переход
                            canvasSize = (canvasSize + dt * 0.8f) % 1;
                            //canvasSnake = (canvasSnake + dt * currentSpeed / 100f) % 1;
                            multiplSize = Math.abs(canvasSize - 0.5f) * 2f * 0.3f + 0.7f;
                            multiplSnake = (Math.abs(canvasSize - 0.5f) * (-2f) * 0.3f + 0.7f) * 33;//вместо мулт сайз 2
                            break;
                        default:
                            Log.wtf(LOG_TAG, "Тип еды задан неправильно. Обновление размеров " + type);
                            break;
                    }
                }
            }
        }
    }





    public float getMaxSpeed() {return  maxSpeed;}
    public float getCurrentSpeed(){ return currentSpeed; }
    public float getCurrentX(){
        return currentX;
    }
    public float getCurrentY(){
        return currentY;
    }
    public float getOrientationInDegrees(){ return orientation*180/(float)Math.PI;}
    public float getMultiplSize(){ return multiplSize;}

    public float getMultiplSnake() {
        return multiplSnake;
    }

    public float getCurrentRadius() { return currentRadius; }
    public boolean isEaten(){return isEaten;}

    public void setEaten(){isEaten=true;}
    public void setOrientationAim(float orientationAim_){
        orientationAim=orientationAim_;
    }

    public void setInvisible(float timer,float segCurrentX, float segCurrentY){
        invisibleTimer=-timer;
        currentX=segCurrentX;
        currentY=segCurrentY;
        isInvisible=true;
        this.goToRandomLocation();
        currentSpeed=0;
        this.randomizeType();

        isEaten=false;
    }
    public void setInvisibleAngryBossfood(float timer,float segCurrentX, float segCurrentY, float currentSpeed, float orientation){
        invisibleTimer=-timer;
        currentX=segCurrentX;
        currentY=segCurrentY;
        isInvisible=true;
        this.goToRandomLocation();
        this.currentSpeed=currentSpeed*2;
        this.orientation=orientation;

        isEaten=false;
    }

    public boolean isInvisible (){return isInvisible;}

    public boolean isEatenAndNotInvisible(){
        return isEaten&&!isInvisible;
    }

    public void draw(OpenGLRenderer openGLRenderer, Camera camera){
        if (!isEaten) {
            if (Math.abs(currentX-camera.getCurrentX())<camera.getGamefieldHalfX()&&Math.abs(currentY-camera.getCurrentY())<camera.getGamefieldHalfY()) {
                switch (type){
                    case 0:
                        openGLRenderer.drawEllipse(currentX, currentY,1+0.2f*multiplSize, orientation * 180 / (float) Math.PI);
                        openGLRenderer.drawLowpolyRound(currentX, currentY,3.5f);
                        openGLRenderer.drawLowpolyRoundTransfered(currentX, currentY,3.5f,orientation * 180 / (float) Math.PI,8.4f,0);
                        openGLRenderer.drawLowpolyRoundTransfered(currentX, currentY,3.5f,orientation * 180 / (float) Math.PI,-8.4f,0);
                        openGLRenderer.drawBezier(currentX, currentY,0.7f*1.5f,orientation * 180 / (float) Math.PI, multiplSnake);
                        break;
                    case 1:
                        openGLRenderer.drawPlus(currentX, currentY, 1.5f, orientation * 180 / (float) Math.PI);
                        openGLRenderer.drawPentagon(currentX, currentY, multiplSize, orientation * 180 / (float) Math.PI);
                        break;
                    case 2:
                        openGLRenderer.drawHalfRings(currentX, currentY,9.35f,orientation * 180 / (float) Math.PI,multiplSize * 25 + 2);
                        openGLRenderer.drawBezier3(currentX, currentY,1.05f,orientation * 180 / (float) Math.PI, multiplSnake);
                        break;
                    case 3:
                        openGLRenderer.drawLowpolyRound(currentX, currentY,3.5f);
                        openGLRenderer.drawRing(currentX, currentY,9.35f);
                        openGLRenderer.drawBezier2(currentX, currentY,1.05f,orientation * 180 / (float) Math.PI, multiplSnake);
                        break;
                    case 4:
                        openGLRenderer.drawLowpolyRound(currentX, currentY,3.5f);
                        openGLRenderer.drawRing(currentX, currentY,9.35f);
                        openGLRenderer.drawBezier3(currentX, currentY,1.05f,orientation * 180 / (float) Math.PI, multiplSnake);
                        break;
                    case 5:
                        openGLRenderer.drawTriangle(currentX, currentY,13f,orientation * 180 / (float) Math.PI);
                        openGLRenderer.drawLowpolyRound(currentX, currentY,3.5f);
                        openGLRenderer.drawBezier4(currentX, currentY,1.05f,orientation * 180 / (float) Math.PI, multiplSnake);
                        break;
                    case 6:
                        openGLRenderer.drawEllipse(currentX, currentY,1.09f,orientation * 180 / (float) Math.PI+90);
                        openGLRenderer.drawLowpolyRoundTransfered(currentX, currentY,4f,orientation * 180 / (float) Math.PI,0,6.2f);
                        openGLRenderer.drawLowpolyRoundTransfered(currentX, currentY,4f,orientation * 180 / (float) Math.PI,0,-6.2f);
                        openGLRenderer.drawBezier4(currentX, currentY,1.05f,orientation * 180 / (float) Math.PI, multiplSnake);
                        break;
                    case 7://Переход вниз
                        openGLRenderer.setColor(5);
                        openGLRenderer.drawLowpolyRound(currentX, currentY,5f);
                        openGLRenderer.setColor(0);
                        openGLRenderer.drawRing3(currentX,currentY,28*multiplSize);

                        openGLRenderer.drawRoundedTriangleInCenterTransfered(currentX,currentY,multiplSnake,45+orientation * 180 / (float) Math.PI);
                        openGLRenderer.drawRoundedTriangleInCenterTransfered(currentX,currentY,multiplSnake,45+90+orientation * 180 / (float) Math.PI);
                        openGLRenderer.drawRoundedTriangleInCenterTransfered(currentX,currentY,multiplSnake,45+180+orientation * 180 / (float) Math.PI);
                        openGLRenderer.drawRoundedTriangleInCenterTransfered(currentX,currentY,multiplSnake,45+270+orientation * 180 / (float) Math.PI);

                        break;
                    case 8://Переход наверх
                        openGLRenderer.setColor(6);
                        openGLRenderer.drawLowpolyRound(currentX, currentY,3f);
                        openGLRenderer.setColor(0);
                        openGLRenderer.drawRing3(currentX,currentY,28*multiplSize);

                        openGLRenderer.drawRoundedTriangleOutCenterTransfered(currentX,currentY,multiplSnake,45+orientation * 180 / (float) Math.PI);
                        openGLRenderer.drawRoundedTriangleOutCenterTransfered(currentX,currentY,multiplSnake,45+90+orientation * 180 / (float) Math.PI);
                        openGLRenderer.drawRoundedTriangleOutCenterTransfered(currentX,currentY,multiplSnake,45+180+orientation * 180 / (float) Math.PI);
                        openGLRenderer.drawRoundedTriangleOutCenterTransfered(currentX,currentY,multiplSnake,45+270+orientation * 180 / (float) Math.PI);

                        break;
                    default:
                        Log.wtf(LOG_TAG,"Тип еды задан неправильно. Обновление картинки");
                        break;
                }
            }
        }
    }//Отображение

    public void drawAngryBossFood(OpenGLRenderer openGLRenderer, Camera camera){
        if (!isEaten) {
            if (Math.abs(currentX-camera.getCurrentX())<camera.getGamefieldHalfX()&&Math.abs(currentY-camera.getCurrentY())<camera.getGamefieldHalfY()) {
                switch (type){

                    case 2:
                        openGLRenderer.setColor(7);
                        openGLRenderer.drawHalfRings(currentX, currentY,9.35f,orientation * 180 / (float) Math.PI,multiplSize * 25 + 2);
                        openGLRenderer.drawBezier3(currentX, currentY,1.05f,orientation * 180 / (float) Math.PI, multiplSnake);
                        openGLRenderer.setColor(0);
                        break;

                    default:
                        Log.wtf(LOG_TAG,"Тип еды задан неправильно. Обновление картинки");
                        break;
                }
            }
        }
    }//Отображение




    public void goToRandomLocation(){
        aimX = (float) (Math.random()-0.5)*2*1000;
        aimY = (float) (Math.random()-0.5)*2*1000;
    }
}
