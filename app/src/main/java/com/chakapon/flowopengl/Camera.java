package com.chakapon.flowopengl;

public class Camera {

    final String LOG_TAG = "Camera";

    private float currentX,currentY;//Положение на карте
    private float minSpeed,currentSpeed;
    private float minSpeedMultiplier;
    private float targetX,targetY;
    float screenSizeX,screenSizeY;
    float maxDistanceX,maxDistanceY;
    float gamefieldHalfX, gamefieldHalfY;


    Camera(){
        minSpeedMultiplier=0.2f;//Множитель приближения камеры при бездействии
        minSpeed=200*minSpeedMultiplier;
    }

    public void setScreen(float screenWidth, float screenHeight, float gamefieldHalfX_, float gamefieldHalfY_){
        screenSizeX=screenWidth;screenSizeY=screenHeight;
        float mult=1.4f;//Множитель на границу игровой зоны (для отображения)
        gamefieldHalfX=gamefieldHalfX_*mult;
        gamefieldHalfY=gamefieldHalfY_*mult;
        maxDistanceX=screenSizeX*0.14f;
        maxDistanceY=screenSizeY*0.14f;
    }

    public float getCurrentX(){
        return currentX;
    }
    public float getCurrentY(){
        return currentY;
    }
    public float getGamefieldHalfX() {
        return gamefieldHalfX;
    }
    public float getGamefieldHalfY() {
        return gamefieldHalfY;
    }

    public void updateMovement(float dt, Protagonist protagonist){
        currentSpeed=protagonist.getCurrentSpeed();
        targetX=protagonist.getCurrentX();
        targetY=protagonist.getCurrentY();

        //Если расстояние от центра выше чем допустимое
        float distanceEllips=(float)Math.sqrt(Math.pow((targetX-currentX)/maxDistanceX,2)+Math.pow((targetY-currentY)/maxDistanceY,2));
        float distance=(float)Math.sqrt((targetX-currentX)*(targetX-currentX)+(targetY-currentY)*(targetY-currentY));
        float angle=(float) Math.atan2(targetY - currentY, targetX - currentX);

        if (distance>minSpeedMultiplier*protagonist.getMaxSpeed()*dt*1.5) {//if Для исключения дёргания на месте
            currentSpeed = Math.max(currentSpeed * 1.0f * distanceEllips, minSpeed);///Чуток другой вариант приближения камеры, побыстрее
            //currentSpeed = Math.max(currentSpeed * 1.0f * distanceEllips, minSpeed*distanceEllips);
            currentX = currentX + currentSpeed * (float) Math.cos(angle) * dt;
            currentY = currentY + currentSpeed * (float) Math.sin(angle) * dt;
        }
    }
}
