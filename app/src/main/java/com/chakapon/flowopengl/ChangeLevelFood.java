package com.chakapon.flowopengl;

import android.util.Log;

public class ChangeLevelFood {

    final String LOG_TAG = "ChangeLevelFood";
    private float currentX,currentY;//Положение на карте
    private float orientation;//Ориентация, в радианах
    private float orientationAim;
    private float currentSpeed;//, maxSpeed;//В пикселях в секунду
    private float turnSpeed;//В радианах в секунду
    private float aimX,aimY;

    private float screenSizeX, screenSizeY;

    private float canvasSize, canvasSnake;
    float multiplSize, multiplSize2;


    int type;

    float currentRadius;

    float lastPingTime;
    final float pingPeriod=2f, pingDuration=1f;
    boolean shouldIPing;

    float pingSideCriticalAngle;//Для определения, пинг на какой из стенок
    float alpha, deltaX, deltaY;

    ChangeLevelFood(int type_){
        currentX=(float) (Math.random()-0.5)*2*1000;
        currentY=(float) (Math.random()-0.5)*2*1000;
        orientation= (float) (Math.random()*Math.PI*2f);
        currentSpeed=100;
        turnSpeed=(float)(20f / 180f * Math.PI);

        this.goToRandomLocation();

        canvasSize=(float)Math.random();
        canvasSnake=(float)Math.random();

        type=type_;
        currentRadius=20;

        lastPingTime=0;//-10;//Начальное время от прихода на уровень до первого пинга
        shouldIPing=false;
    }

    public float getCurrentX(){
        return currentX;
    }
    public float getCurrentY(){
        return currentY;
    }

    public float getCurrentRadius() { return currentRadius; }
    public int getType(){
        if (type==0) return 1;
        else         return -1;
    }

    public void setScreen(float screenWidth, float screenHeight){
        screenSizeX=screenWidth;
        screenSizeY=screenHeight;
        pingSideCriticalAngle=(float) Math.atan2(screenSizeY,screenSizeX);
    }

    void draw(OpenGLRenderer openGLRenderer, Camera camera){
        switch (type){
            case 0://Переход вниз
                openGLRenderer.setColor(5);
                openGLRenderer.drawLowpolyRound(currentX, currentY,5f);
                openGLRenderer.setColor(0);
                openGLRenderer.drawRing3(currentX,currentY,28*multiplSize);

                openGLRenderer.drawRoundedTriangleInCenterTransfered(currentX,currentY,multiplSize2,45+orientation * 180 / (float) Math.PI);
                openGLRenderer.drawRoundedTriangleInCenterTransfered(currentX,currentY,multiplSize2,45+90+orientation * 180 / (float) Math.PI);
                openGLRenderer.drawRoundedTriangleInCenterTransfered(currentX,currentY,multiplSize2,45+180+orientation * 180 / (float) Math.PI);
                openGLRenderer.drawRoundedTriangleInCenterTransfered(currentX,currentY,multiplSize2,45+270+orientation * 180 / (float) Math.PI);
                openGLRenderer.drawBezier5(currentX,currentY,1.5f,orientation * 180 / (float) Math.PI,Math.abs(canvasSnake-0.5f)*2f,21*multiplSize-10);
                break;
            case 1://Переход наверх
                openGLRenderer.setColor(6);
                openGLRenderer.drawLowpolyRound(currentX, currentY,3f);
                openGLRenderer.setColor(0);
                openGLRenderer.drawRing3(currentX,currentY,28*multiplSize);
                openGLRenderer.drawRoundedTriangleOutCenterTransfered(currentX,currentY,multiplSize2,45+orientation * 180 / (float) Math.PI);
                openGLRenderer.drawRoundedTriangleOutCenterTransfered(currentX,currentY,multiplSize2,45+90+orientation * 180 / (float) Math.PI);
                openGLRenderer.drawRoundedTriangleOutCenterTransfered(currentX,currentY,multiplSize2,45+180+orientation * 180 / (float) Math.PI);
                openGLRenderer.drawRoundedTriangleOutCenterTransfered(currentX,currentY,multiplSize2,45+270+orientation * 180 / (float) Math.PI);
                openGLRenderer.drawBezier5(currentX,currentY,1.5f,orientation * 180 / (float) Math.PI,Math.abs(canvasSnake-0.5f)*2f,21*multiplSize-10);
                break;
            default:
                Log.wtf(LOG_TAG,"Тип еды задан неправильно. Обновление картинки");
                break;
        }



        if (lastPingTime>pingPeriod+pingDuration) {lastPingTime=0;shouldIPing=false;}//Может рассинхронизироваться у разных клеток
        if (lastPingTime>=pingPeriod&&lastPingTime<=pingPeriod+pingDuration){
            if (lastPingTime>=pingPeriod&&lastPingTime<pingPeriod+0.1) {
                if ((Math.abs(camera.getCurrentX()-currentX)>screenSizeX/2)||(Math.abs(camera.getCurrentY()-currentY)>screenSizeY/2))//Чтобы не пинговать впритык
                    shouldIPing=true;

            }


            if (shouldIPing) {
                //ping

                if ((Math.abs(camera.getCurrentX()-currentX)>screenSizeX/2)||(Math.abs(camera.getCurrentY()-currentY)>screenSizeY/2)){
                    //За границей
                    //Log.wtf(LOG_TAG,"Ping "+type);

                    alpha=(float) Math.atan2(currentY-camera.getCurrentY(),currentX-camera.getCurrentX());
                    if ((Math.abs(alpha)<pingSideCriticalAngle)||(Math.PI-Math.abs(alpha)<pingSideCriticalAngle)){
                        //Боковая стенка
                        if (currentX-camera.getCurrentX()>0) {//Правая стенка
                            deltaX=camera.getCurrentX()+screenSizeX/2;
                            deltaY=camera.getCurrentY() + screenSizeX/2*(float)Math.tan(alpha);
                        }
                        else {
                            deltaX=camera.getCurrentX()-screenSizeX/2;
                            deltaY=camera.getCurrentY() - screenSizeX/2*(float)Math.tan(alpha);
                        }
                    }
                    else{
                        //Верх или низ
                        if (currentY-camera.getCurrentY()>0) {//Низ
                            deltaX=camera.getCurrentX() + screenSizeY/2/(float)Math.tan(alpha);
                            deltaY=camera.getCurrentY()+screenSizeY/2;
                        }
                        else {//Верх
                            deltaX=camera.getCurrentX() - screenSizeY/2/(float)Math.tan(alpha);
                            deltaY=camera.getCurrentY()-screenSizeY/2;
                        }
                    }
                }
                else{
                    //В экран попадает
                    deltaX=currentX;
                    deltaY=currentY;
                }
                if (type==0){
                    openGLRenderer.setAlphaCyan(1-(lastPingTime - pingPeriod) / pingDuration);
                }
                else{
                    openGLRenderer.setAlphaRed(1-(lastPingTime - pingPeriod) / pingDuration);
                }
                openGLRenderer.drawRing3(deltaX,deltaY,(lastPingTime - pingPeriod) / pingDuration * 40);
                openGLRenderer.setColor(0);
            }//ping
        }
    }


    public void updateMapPosition(float dt){
        lastPingTime=lastPingTime+dt;
                if (Math.sqrt(Math.pow((currentX - aimX), 2) + Math.pow((currentX - aimX), 2)) < 5f) {
                    goToRandomLocation();
                }

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

                //Позиция
                currentX = currentX + currentSpeed * (float) Math.cos(orientation) * dt;
                currentY = currentY + currentSpeed * (float) Math.sin(orientation) * dt;

        //Обновление для канвы - изменение размера
        switch (type) {
            case 0://Переход вниз
                canvasSize = (canvasSize + dt * 0.8f) % 1;
                canvasSnake = (canvasSnake + dt * currentSpeed / 100f) % 1;
                multiplSize=Math.abs(canvasSize-0.5f)*2f*0.3f+0.7f;
                multiplSize2=(Math.abs(canvasSize-0.5f)*(-2f)*0.3f+0.7f)*40;//вместо мулт сайз 2
                break;
            case 1://Переход наверх
                canvasSize = (canvasSize + dt * 0.8f) % 1;
                canvasSnake = (canvasSnake + dt * currentSpeed / 100f) % 1;
                multiplSize=Math.abs(canvasSize-0.5f)*2f*0.3f+0.7f;
                multiplSize2=(Math.abs(canvasSize-0.5f)*(-2f)*0.3f+0.7f)*38;//35;//вместо мулт сайз 2
                break;
            default:
                Log.wtf(LOG_TAG,"Тип еды задан неправильно. Обновление размеров "+type);
                break;
        }
    }

    public void goToRandomLocation(){
        aimX = (float) (Math.random()-0.5)*2*1000;
        aimY = (float) (Math.random()-0.5)*2*1000;
    }
}
