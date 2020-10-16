package com.chakapon.flowopengl;


import android.graphics.Canvas;
import android.util.Log;

public class FlockieBird {
    //Стайка птиц (клеток)

    final String LOG_TAG = "FlockieBird";
    float currentX, currentY, speed;
    float aimX, aimY;
    Food [] foods_array;
    int Nbirds, NbirdsNow;
    boolean areEaten=false;//Съедены ли все?
    boolean isPanic=false;//Паника для всех
    float panicCenterX, panicCenterY;

    float panicTimer = 0, panicMaxTime = 2;

    FlockieBird(int Nbirds_, int birdType_){
        Nbirds=NbirdsNow=Nbirds_;
        currentX = (float) (Math.random() - 0.5) * 2 * 1000;
        currentY = (float) (Math.random() - 0.5) * 2 * 1000;
        speed=30;
        foods_array = new Food[Nbirds];
        for (int j=0;j<Nbirds;j++){
            foods_array[j] = new Food(currentX,currentY,birdType_);
        }
    }

    public boolean areEaten(){return areEaten;}

    public int getNbirds(){return Nbirds;}
    public boolean isEaten(int N_){
        return foods_array[N_].isEaten;
    }
    public float getCurrentX(int N_){
        return foods_array[N_].getCurrentX();
    }
    public float getCurrentY(int N_){
        return foods_array[N_].getCurrentY();
    }
    public float getCurrentRadius(int N_){
        return foods_array[N_].getCurrentRadius();
    }

    public void updateMapPosition(float dt) {
        if (!areEaten) {
            if (isPanic) {
                if (panicTimer > panicMaxTime) {
                    isPanic = false;
                    currentX=0;currentY=0;
                    for (int j=0;j<Nbirds;j++){
                        if (!foods_array[j].isEaten()) {
                            currentX = currentX + foods_array[j].getCurrentX();
                            currentY = currentY + foods_array[j].getCurrentY();
                        }
                    }
                    currentX=currentX/NbirdsNow;
                    currentY=currentY/NbirdsNow;
                    aimX = (float) (Math.random() - 0.5) * 2 * 1000;
                    aimY = (float) (Math.random() - 0.5) * 2 * 1000;
                }
                panicTimer = panicTimer + dt;
            }//Обновление таймера паники
            if (!isPanic){
                if (Math.abs(currentX - aimX)<10&&Math.abs(currentY - aimY)<10){
                    aimX = (float) (Math.random() - 0.5) * 2 * 1000;
                    aimY = (float) (Math.random() - 0.5) * 2 * 1000;
                }

                //Обновление передвижения центра стаи
                float orientationAim = (float) Math.atan2(aimY - currentY, aimX - currentX);
                currentX = currentX + speed * (float) Math.cos(orientationAim) * dt;
                currentY = currentY + speed * (float) Math.sin(orientationAim) * dt;
            }
            else{
                for (int j=0;j<Nbirds;j++){
                    if (!foods_array[j].isEaten())
                        foods_array[j].setOrientationAim((float) (Math.atan2(foods_array[j].getCurrentY() - panicCenterY, foods_array[j].getCurrentX() - panicCenterX)+(Math.random()-0.5)*2*0.17));
                }//+-10 градусов
            }

            for (int j=0;j<Nbirds;j++){
                foods_array[j].updateMapPositionBird(dt,isPanic,currentX,currentY);
            }
        }
    }

    public void setDamaged(int nBird){
        foods_array[nBird].setEaten();
        NbirdsNow--;
        if (NbirdsNow==0){
            areEaten=true;
            return;
        }
/////////Паника

        isPanic=true;
        panicTimer=0;
        panicCenterX=foods_array[nBird].getCurrentX();
        panicCenterY=foods_array[nBird].getCurrentY();
    }

    public void draw(OpenGLRenderer openGLRenderer, Camera camera) {
        float camCurX=camera.getCurrentX();
        float camCurY=camera.getCurrentY();
        for (int j=0;j<Nbirds;j++){
            if (!foods_array[j].isEaten) {
                if (Math.abs(foods_array[j].getCurrentX()-camCurX)<camera.getGamefieldHalfX()&&Math.abs(foods_array[j].getCurrentY()-camCurY)<camera.getGamefieldHalfY()) {
                    foods_array[j].draw(openGLRenderer,camera);
                }
            }
        }
    }

}
