package com.chakapon.flowopengl;


import android.util.Log;

public class Segment {

    final String LOG_TAG = "Segment";
    int type;
    float distance;
    float currentX,currentY;
    float orientation;
    boolean saturation;

    float canvasSnake=0;//Махать лапками

    float currentRadius=20;

    boolean isWeakPoint=false, isWeakPointDamaged=false;

    float howMuchIsTheFish=1;
    boolean isFirst=false;

    Segment(float headCurrentX, float headCurrentY, float headOrientation, int type_){
        canvasSnake=(float)Math.random();
        orientation=headOrientation;
        type = type_;
        this.setDistance();
        currentX = headCurrentX + distance * (float) Math.cos(orientation+Math.PI);// * dt;
        currentY = headCurrentY + distance * (float) Math.sin(orientation+Math.PI);// * dt;
        saturation=false;
    }
    Segment(int type_, float howMuchIsTheFish_){
        type = type_;
/////////////////////////////////////////////////////////////////////////////////добавить ещё curx, cury
        howMuchIsTheFish=howMuchIsTheFish_;
        saturation=false;
        if (type==0) {
            //currentRadius=8.4f;

            //saturation=true;
            //distance=33;
        }//Центральные три точки. Стандартные
        if (type==3){
            saturation=true;
            currentRadius=16.8f;
            isWeakPoint=true;
            //distance=21;
        }//Глаза. Кружок с ободом

        currentRadius=currentRadius*howMuchIsTheFish;///////////////////////////////////////////////////////////////////////////////////
    }//Сегмент акулы

    private void setDistance(){
        if (type==0||type==2) {
            currentRadius=8.4f;
            distance=33*0.8f;
        }
        if (type==1){
            distance=21;
        }
    }

    public void setFirst(){isFirst=true;distance=33;}
    public float getCurrentX() {
        return currentX;
    }
    public float getCurrentY() {
        return currentY;
    }
    public float getOrientation() {
        return orientation;
    }

    public float getOrientationInDegrees(){ return orientation*180/(float)Math.PI;}
    public boolean getSaturation(){return saturation;}

    public int getType() {
        return type;
    }

    public float getCurrentRadius(){return currentRadius;}

    public void setSaturation(boolean saturation_){ saturation=saturation_;}

    public void setWeakPoint(){isWeakPoint=true;}
    public void setWeakPointDamaged(){
        isWeakPointDamaged=true;
        saturation=false;
    }
    public void restoreWeakPoint(){
        isWeakPointDamaged=false;
    }

    public boolean isWeakPoint(){return isWeakPoint;}

    public boolean isWeakPointDamaged() {
        return isWeakPointDamaged;
    }

    public boolean isSegmentWeakPointAndUndamaged(){
        if (isWeakPoint&&!isWeakPointDamaged){
            return true;
        }
            return false;
    }

    public boolean hasSaturationOrIsWeakPoint(){
        return saturation||isWeakPoint;
    }



    public void draw(OpenGLRenderer openGLRenderer){
                switch (type){
                    case 0://Сегменты
                        openGLRenderer.drawSquareTransfered(currentX,currentY,3.36f*howMuchIsTheFish,this.getOrientationInDegrees(),30*0.7f*howMuchIsTheFish,0);
                        if (isWeakPoint){
                            if (isWeakPointDamaged) openGLRenderer.setColor(1);//Прозрачный цвет
                            openGLRenderer.drawRing2(currentX, currentY,12f*0.7f*howMuchIsTheFish);
                            if (saturation){
                                //saturated
                                openGLRenderer.drawRound(currentX, currentY,5.5f*0.7f*(1+0.3773584905660377f/2)*howMuchIsTheFish);
                            }
                            else{
                                //notSaturated
                                openGLRenderer.drawRing2(currentX, currentY,5.5f*0.7f*howMuchIsTheFish);
                            }
                            if (isWeakPointDamaged) openGLRenderer.setColor(0);//Вернуть цвет обратно на стандартный белый
                        }
                        else{
                            openGLRenderer.drawRing(currentX, currentY,12*0.7f*howMuchIsTheFish);
                            if (saturation){
                                //saturated
                                openGLRenderer.drawRound(currentX, currentY,5.5f*0.7f*(1+0.3773584905660377f/2)*howMuchIsTheFish);
                            }
                            else{
                                //notSaturated
                                openGLRenderer.drawRing2(currentX, currentY,5.5f*0.7f*howMuchIsTheFish);
                            }
                        }
                        break;
                    /*case 1://Хвостик
                        openGLRenderer.drawRing2(currentX, currentY,5f*0.7f);
                        openGLRenderer.drawSquareTransfered(currentX,currentY,3.36f,this.getOrientationInDegrees(),15*0.7f,0);
                        break;
                    case 2://Лапки
                        openGLRenderer.drawSquareTransfered(currentX,currentY,3.36f,this.getOrientationInDegrees(),30*0.7f,0);
                        if (isWeakPoint){
                            if (isWeakPointDamaged) openGLRenderer.setColor(1);//Прозрачный цвет
                            openGLRenderer.drawRing2(currentX, currentY,12f*0.7f);
                            if (saturation){
                                //saturated
                                openGLRenderer.drawRound(currentX, currentY,5.5f*0.7f*(1+0.3773584905660377f/2));
                            }
                            else{
                                //notSaturated
                                openGLRenderer.drawRing2(currentX, currentY,5.5f*0.7f);
                            }
                            if (isWeakPointDamaged) openGLRenderer.setColor(0);//Вернуть цвет обратно на стандартный белый
                        }
                        else{
                            openGLRenderer.drawRing(currentX, currentY,12*0.7f);
                            if (saturation){
                                //saturated
                                openGLRenderer.drawRound(currentX, currentY,5.5f*0.7f*(1+0.3773584905660377f/2));
                            }
                            else{
                                //notSaturated
                                openGLRenderer.drawRing2(currentX, currentY,5.5f*0.7f);
                            }
                        }
                        openGLRenderer.drawSquaresTransfered(currentX,currentY,this.getOrientationInDegrees(),(Math.abs(canvasSnake-0.5f)*2f*0.3f+0.7f)*30*2);
                        //Log.wtf(LOG_TAG,"canvasSnake = "+canvasSnake);
                        //Log.wtf(LOG_TAG,"angle = "+(Math.abs(canvasSnake-0.5f)*2f*0.3f+0.7f)*30*2);
                        break;*/
                    case 3://Сегменты
                        /*openGLRenderer.drawRing2(currentX, currentY,12*0.7f);
                        openGLRenderer.drawLowpolyRound(currentX, currentY,5.5f*0.7f);*/



                        if (isWeakPoint){
                            if (isWeakPointDamaged) openGLRenderer.setColor(1);//Прозрачный цвет
                            openGLRenderer.drawRing2(currentX, currentY,12f*0.7f*howMuchIsTheFish*2);
                            if (saturation){
                                //saturated
                                openGLRenderer.drawRound(currentX, currentY,5.5f*0.7f*(1+0.3773584905660377f/2)*howMuchIsTheFish*2);
                            }
                            else{
                                //notSaturated
                                openGLRenderer.drawRing2(currentX, currentY,5.5f*0.7f*howMuchIsTheFish*2);
                            }
                            if (isWeakPointDamaged) openGLRenderer.setColor(0);//Вернуть цвет обратно на стандартный белый
                        }
                        else{
                            openGLRenderer.drawRing(currentX, currentY,12*0.7f*howMuchIsTheFish*2);
                            if (saturation){
                                //saturated
                                openGLRenderer.drawRound(currentX, currentY,5.5f*0.7f*(1+0.3773584905660377f/2)*howMuchIsTheFish*2);
                            }
                            else{
                                //notSaturated
                                openGLRenderer.drawRing2(currentX, currentY,5.5f*0.7f*howMuchIsTheFish*2);
                            }
                        }
                        break;
                    case 4://Кружок и точка для босса
                        openGLRenderer.drawRing2(currentX, currentY,5.5f);
                        openGLRenderer.drawSquareTransfered(currentX,currentY,3.36f,this.getOrientationInDegrees(),14,0);
                        break;
                    default:
                        Log.wtf(LOG_TAG,"Тип еды задан неправильно. Обновление картинки draw " + type);
                        break;
                }
    }//Отображение
    public void drawWithScale(OpenGLRenderer openGLRenderer, float segNum, float NsegmMax){
        switch (type){
            case 0://Сегменты
                openGLRenderer.drawSquareTransfered(currentX,currentY,3.36f,this.getOrientationInDegrees(),30*0.7f,0);
                if (isWeakPoint){
                    if (isWeakPointDamaged) openGLRenderer.setColor(1);//Прозрачный цвет
                    openGLRenderer.drawRing2(currentX, currentY,12f*0.7f);
                    if (saturation){
                        //saturated
                        openGLRenderer.drawRound(currentX, currentY,5.5f*0.7f*(1+0.3773584905660377f/2));
                    }
                    else{
                        //notSaturated
                        openGLRenderer.drawRing2(currentX, currentY,5.5f*0.7f);
                    }
                    if (isWeakPointDamaged) openGLRenderer.setColor(0);//Вернуть цвет обратно на стандартный белый
                }
                else{
                    openGLRenderer.drawRing(currentX, currentY,12*0.7f);
                    if (saturation){
                        //saturated
                        openGLRenderer.drawRound(currentX, currentY,5.5f*0.7f*(1+0.3773584905660377f/2));
                    }
                    else{
                        //notSaturated
                        openGLRenderer.drawRing2(currentX, currentY,5.5f*0.7f);
                    }
                }
                break;
            case 1://Хвостик
                openGLRenderer.drawRing2(currentX, currentY,5f*0.7f);
                openGLRenderer.drawSquareTransfered(currentX,currentY,3.36f,this.getOrientationInDegrees(),15*0.7f,0);
                break;
            case 2://Лапки
                openGLRenderer.drawSquareTransfered(currentX,currentY,3.36f,this.getOrientationInDegrees(),30*0.7f,0);
                if (isWeakPoint){
                    if (isWeakPointDamaged) openGLRenderer.setColor(1);//Прозрачный цвет
                    openGLRenderer.drawRing2(currentX, currentY,12f*0.7f);
                    if (saturation){
                        //saturated
                        openGLRenderer.drawRound(currentX, currentY,5.5f*0.7f*(1+0.3773584905660377f/2));
                    }
                    else{
                        //notSaturated
                        openGLRenderer.drawRing2(currentX, currentY,5.5f*0.7f);
                    }
                    if (isWeakPointDamaged) openGLRenderer.setColor(0);//Вернуть цвет обратно на стандартный белый
                }
                else{
                    openGLRenderer.drawRing(currentX, currentY,12*0.7f);
                    if (saturation){
                        //saturated
                        openGLRenderer.drawRound(currentX, currentY,5.5f*0.7f*(1+0.3773584905660377f/2));
                    }
                    else{
                        //notSaturated
                        openGLRenderer.drawRing2(currentX, currentY,5.5f*0.7f);
                    }
                }
                openGLRenderer.drawSquaresTransferedWithScale(currentX,currentY,this.getOrientationInDegrees(),(Math.abs(canvasSnake-0.5f)*2f*0.3f*2+0.7f)*30*2,NsegmMax/9f*150*(float)(Math.pow(1/(segNum+5),1)));
                break;
            /*case 3://Сегменты
                openGLRenderer.drawRing2(currentX, currentY,12*0.7f);
                openGLRenderer.drawLowpolyRound(currentX, currentY,5.5f*0.7f);

                break;*/
            default:
                Log.wtf(LOG_TAG,"Тип еды задан неправильно. Обновление картинки drawWithScale");
                break;


                        /*
                        //Weak saturated
                        openGLRenderer.drawRing2(currentX, currentY,12f*0.7f);
                        openGLRenderer.drawLowpolyRound(currentX, currentY,5.5f*0.7f);

                        //Weak NotSaturated
                        openGLRenderer.drawRing2(currentX, currentY,12f*0.7f);
                        openGLRenderer.drawRing2(currentX, currentY,5.5f*0.7f);

                        //NotWeak saturated
                        openGLRenderer.drawRing(currentX, currentY,12*0.7f);
                        openGLRenderer.drawLowpolyRound(currentX, currentY,5.5f*0.7f);


                        //saturated
                        openGLRenderer.drawLowpolyRound(currentX, currentY,5.5f*0.7f);
                        //notSaturated
                        openGLRenderer.drawRing2(currentX, currentY,5.5f*0.7f);

                        //NotWeak
                        openGLRenderer.drawRing(currentX, currentY,12*0.7f);
                        //Weak undamaged
                        openGLRenderer.drawRing2(currentX, currentY,12f*0.7f);

                        */
        }
    }//Отображение
    public void drawDivision(OpenGLRenderer openGLRenderer, float dt){
        if (dt>0&&dt<1f) {
            openGLRenderer.setAlphaWhite((1 - (dt) / 1f)*0.47f);
            openGLRenderer.drawRing(currentX,currentY,howMuchIsTheFish*dt*25f);
            openGLRenderer.setColor(0);
        }
    }


    public void updateMapPosition(float headCurrentX, float headCurrentY, float dt, float headCurrentSpeed){
            //canvasSnake = (canvasSnake + dt * headCurrentSpeed / 100f/2.5f) % 1;

        canvasSnake = (canvasSnake + dt * headCurrentSpeed / 160f) % 1;
        //canvasSnake = (canvasSnake + dt * 0.8f) % 1;
        //canvasSnake = (canvasSnake + dt * currentSpeed / 160f) % 1;

            orientation = (float) Math.atan2(currentY - headCurrentY, currentX - headCurrentX);
            currentX = headCurrentX + distance * (float) Math.cos(orientation);
            currentY = headCurrentY + distance * (float) Math.sin(orientation);
    }

    public void changeType(int type_){
        type=type_;
        if (!isFirst)
        {
            this.setDistance();
        }
    }


    public void updateMapPosition(float currentX_, float currentY_, float orientation_){
        orientation = orientation_;
        currentX = currentX_;
        currentY = currentY_;
    }//Сегмент акулы
}
