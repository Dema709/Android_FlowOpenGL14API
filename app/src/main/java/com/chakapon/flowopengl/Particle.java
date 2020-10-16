package com.chakapon.flowopengl;

//Частицы, что придают иллюзию движения
public class Particle {
    private int particleNum;
    private float [] currentX;
    private float [] currentY;//Положение на карте
    private float [] lifetime;
    private float maxLifetime;
    private float maxRange;

    //float screenSizeX,screenSizeY;


    final String LOG_TAG = "Particle";

    Particle(int particleNum_){
       //particleNum=1;maxRange=50f;
        particleNum=particleNum_;

        lifetime = new float[particleNum];
        currentX = new float[particleNum];
        currentY = new float[particleNum];

        maxLifetime = 10f;maxRange=500*2;//500f;

        //float protagonistX=protagonist.getCurrentX();
        //float protagonistY=protagonist.getCurrentY();

        for (int i=0;i<particleNum;i++){
            lifetime[i]=(i+1)*maxLifetime/particleNum;
            //Log.wtf(LOG_TAG, "lifetime["+i+"]="+lifetime[i]);
            currentX[i]=((float)Math.random()*2-1)*maxRange;
            currentY[i]=((float)Math.random()*2-1)*maxRange;
        }

    }

    public void draw(OpenGLRenderer openGLRenderer, Camera camera){
        for (int j=0;j<particleNum;j++) {
            if (Math.abs(currentX[j]-camera.getCurrentX())<camera.getGamefieldHalfX()&&Math.abs(currentY[j]-camera.getCurrentY())<camera.getGamefieldHalfY()) {
                openGLRenderer.setAlphaWhite((0.5f - Math.abs(lifetime[j] - maxLifetime / 2f) / maxLifetime) * 0.26f);
                openGLRenderer.drawParticle(currentX[j], currentY[j]);
            }
        }
    }

    public void updateMapPosition(float dt, Protagonist protagonist){
        for (int i=0;i<particleNum;i++){
            lifetime[i]=lifetime[i]+dt;
            if (lifetime[i]>maxLifetime){

                lifetime[i]=lifetime[i]-maxLifetime;
                currentX[i]=protagonist.getCurrentX()+((float)Math.random()*2-1)*maxRange;
                currentY[i]=protagonist.getCurrentY()+((float)Math.random()*2-1)*maxRange;
            }

        }
    }
}
