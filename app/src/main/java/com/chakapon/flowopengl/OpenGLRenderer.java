package com.chakapon.flowopengl;
import android.content.Context;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.text.DecimalFormat;
import java.util.Arrays;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_BLEND;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_LINES;
import static android.opengl.GLES20.GL_LINE_LOOP;
import static android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA;
import static android.opengl.GLES20.GL_SRC_ALPHA;
import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glBlendFunc;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glLineWidth;

public class OpenGLRenderer implements Renderer {

    private final static int POSITION_COUNT = 2;
    private Context context;
    private FloatBuffer vertexData;
    private int uColorLocation;
    private int uMatrixLocation;
    private int programId;
    private float[] mProjectionMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mModelMatrix = new float[16];
    private float[] mMatrix = new float[16];

    long currentTime;    float realFPS;    int countFPS;    long timeFPS;    long currentTimeFPS;    float dt;
    final String LOG_TAG = "OpenGLRenderer";
    float touchX, touchY;
    Protagonist protagonist;
    boolean isFingerDown=false,isDoubleTapped=false;
    Camera camera;
    Particle particle;

    int currentLevel=0;
    final int levelNum=8;//Количество уровней = Последний уровень + 1
    Level [] levelArray;
    int shouldIChangeLevel;

    float changeBackgroundColorTime=10;
    float [] oldColor, curColor, newColor;
    //int oldColor, curColor ,newColor;

    private final static int VERTEX_ROUND_COUNT = 16+2;//Количество вершин для заполненного круга +2
    private              int VERTEX_SUM_ROUND_COUNT;//Количество точек в памяти перед кругом //Забитые ручками вершины для всяких пакостей
    private final static int VERTEX_LOWPOLY_ROUND_COUNT = 8+2;//Количество вершин для кольца +2
    private              int VERTEX_SUM_LOWPOLY_ROUND_COUNT;
    private final static int VERTEX_RING_COUNT = 16*2+2;//Количество вершин для кольца +2
    private              int VERTEX_SUM_RING_COUNT;//Кольцо с средним радиусом 1 и толщиной 0,1886792452830189
    private final static int VERTEX_RING2_COUNT = 16*2+2;//Количество вершин для кольца +2
    private              int VERTEX_SUM_RING2_COUNT;//Кольцо с средним радиусом 1 и толщиной 0,3773584905660377
    private final static int VERTEX_RING3_COUNT = 16*2+2;//Количество вершин для кольца +2
    private              int VERTEX_SUM_RING3_COUNT;//Кольцо с средним радиусом 1 и толщиной 0,1f
    private final static int VERTEX_PLUS_COUNT = 16*2+10;//Количество вершин для плюсика *2+10
    private              int VERTEX_SUM_PLUS_COUNT;
    private final static int VERTEX_ELLIPSE_COUNT = 16*2+2;//Количество вершин для эллипса [(24 на 11 с толщиной 2)*0.7] +2
    private              int VERTEX_SUM_ELLIPSE_COUNT;
    private final static int VERTEX_BEZIER_COUNT = 16+1;//Лапки у type0 еды - через кривые безье
    private              int VERTEX_SUM_BEZIER_COUNT;
    private final static int ANIMATION_FRAMES = 21;//Количество кадров на лапки
    private final static int VERTEX_HALF_RING_COUNT = 16+7;
    private              int VERTEX_SUM_HALF_RING_COUNT;
    private final static int VERTEX_MOUTH_COUNT = 16+2+1;//Рот у ГГ - через кривые безье
    private              int VERTEX_SUM_MOUTH_COUNT;
    private final static int VERTEX_SHARKBODY_COUNT = 16*12;//Тело акулки //4 части * 3 точки у треугольника
    private              int VERTEX_SUM_SHARKBODY_COUNT;
    private final static int VERTEX_ROUNDED_TRIANGLE_INCENTER_COUNT = 16+7;//Треугольник в 60 градусов с выгнутой круглой стороной
    private              int VERTEX_SUM_ROUNDED_TRIANGLE_INCENTER_COUNT;
    private final static int VERTEX_ROUNDED_TRIANGLE_OUTCENTER_COUNT = 16+7;//Треугольник в 60 градусов с вогнутой круглой стороной
    private              int VERTEX_SUM_ROUNDED_TRIANGLE_OUTCENTER_COUNT;
    private final static int VERTEX_SHARKMOUTH_COUNT = 12*3;//Три "буквы Л"
    private              int VERTEX_SUM_SHARKMOUTH_COUNT;
    private final static int VERTEX_PARTICLE_COUNT = 16*9;
    private              int VERTEX_SUM_PARTICLE_COUNT;
    private final static int VERTEX_BOSS1_COUNT = 16*12;//Большая акулка с треугольниками  //4 части * 3 точки у треугольника
    private              int VERTEX_SUM_BOSS1_COUNT;



    //Food [] foods_array; int foods_arraySize=1;//30;
    Food [] foods_arrayTest; int foods_arraySizeTest=9;//30;

    float[] valuesAccelN = new float[3];
    float deltaX, deltaY, nulX, nulY;
    float accelerometerOrientation=0, accelerometerRadius=0;
    boolean isFirstTime=true;//Для калибровки "нулевого" положения
    float [][] valuesAccelAverage; int currentValuesAccelN=0;
    int averegeN=4;//Размерность усреднялки
    boolean isPhoneScreenUp=true;//Для ввода ленивого режима (лёжа на диванчике на спине, лицом вверх)




    public OpenGLRenderer(Context context) {
        this.context = context;
    }

    @Override
    public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {
        //Log.wtf(LOG_TAG,"onSurfaceCreated - Я родился!");
        glClearColor(0f,(float)0x6D/255f, (float)0xBB/255f, 0f);//Цвет фона
        //glEnable(GL_DEPTH_TEST);//Тест глубины //И ещё флаг очистки буфера glClear
        glEnable(GL_BLEND);glBlendFunc(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA);//Прозрачность
        int vertexShaderId = ShaderUtils.createShader(context, GL_VERTEX_SHADER, R.raw.vertex_shader);
        int fragmentShaderId = ShaderUtils.createShader(context, GL_FRAGMENT_SHADER, R.raw.fragment_shader);
        programId = ShaderUtils.createProgram(vertexShaderId, fragmentShaderId);
        glUseProgram(programId);
        createViewMatrix();
        prepareData();
        bindData();

        countFPS=0;
        currentTimeFPS=currentTime=timeFPS=System.currentTimeMillis();
    }

    public void loadInfo(){
        //Инициализация при создании
        //Log.wtf(LOG_TAG,"Инициализация при создании. load info");
       /* foods_array = new Food[foods_arraySize];
        for (int j=0;j<foods_arraySize;j++){
            foods_array[j] = new Food();
            if (j>=foods_arraySize){
                foods_array[j].setEaten();
            }
        }*/

        foods_arrayTest = new Food[foods_arraySizeTest];
        for (int j=0;j<foods_arraySizeTest;j++){
            foods_arrayTest[j] = new Food(j);
            if (j>=foods_arraySizeTest){
                foods_arrayTest[j].setEaten();
            }
        }

        protagonist = new Protagonist();
        touchX=0;touchY=0;
        camera = new Camera();

        particle=new Particle(30*4);//30 можно не отображать те, что далеко

        levelArray = new Level[levelNum];
        for (int j=0;j<levelNum;j++){
            levelArray[j] = new Level(j);
        }

        oldColor=new float[3];curColor=new float[3];newColor=new float[3];
        for (int i=0;i<3;i++){
            curColor[i]=(float)((levelArray[currentLevel].getColor()>>(2-i)*8)&0xFF)/255f;
        }

        valuesAccelAverage = new float[averegeN][3];
        for (int i=0;i<averegeN;i++){
            for (int j=0;j<3;j++){
                valuesAccelAverage[i][j]=0;
            }
        }
    }

    @Override
    public void onSurfaceChanged(GL10 arg0, int width, int height) {
        //Log.wtf(LOG_TAG,"onSurfaceChanged - Я изменился! " + width + " " + height);
        glViewport(0, 0, width, height);
        createProjectionMatrix(width, height);
        bindMatrix();
    }

    private void prepareData() {

        float[] vertices0 = {
                // ось X
                -360, 0,
                 360, 0,

                // ось Y
                0, -360,
                0,  360,

                //Квадрат
                -0.5f, -0.5f,
                -0.5f, 0.5f,
                 0.5f, 0.5f,
                 0.5f, -0.5f,

                //Пятиугольник с средним радиусом 1 и толщиной стенки 0.2f
                (float)(0.9*Math.cos(Math.PI*2/5*0)),(float)(0.9*Math.sin(Math.PI*2/5*0)),
                (float)(1.1*Math.cos(Math.PI*2/5*0)),(float)(1.1*Math.sin(Math.PI*2/5*0)),
                (float)(0.9*Math.cos(Math.PI*2/5*1)),(float)(0.9*Math.sin(Math.PI*2/5*1)),
                (float)(1.1*Math.cos(Math.PI*2/5*1)),(float)(1.1*Math.sin(Math.PI*2/5*1)),
                (float)(0.9*Math.cos(Math.PI*2/5*2)),(float)(0.9*Math.sin(Math.PI*2/5*2)),
                (float)(1.1*Math.cos(Math.PI*2/5*2)),(float)(1.1*Math.sin(Math.PI*2/5*2)),
                (float)(0.9*Math.cos(Math.PI*2/5*3)),(float)(0.9*Math.sin(Math.PI*2/5*3)),
                (float)(1.1*Math.cos(Math.PI*2/5*3)),(float)(1.1*Math.sin(Math.PI*2/5*3)),
                (float)(0.9*Math.cos(Math.PI*2/5*4)),(float)(0.9*Math.sin(Math.PI*2/5*4)),
                (float)(1.1*Math.cos(Math.PI*2/5*4)),(float)(1.1*Math.sin(Math.PI*2/5*4)),
                (float)(0.9*Math.cos(Math.PI*2/5*5)),(float)(0.9*Math.sin(Math.PI*2/5*5)),
                (float)(1.1*Math.cos(Math.PI*2/5*5)),(float)(1.1*Math.sin(Math.PI*2/5*5)),

                //Треугольник
                (float)((1-0.16)*Math.cos(Math.PI*2/3*0)),(float)((1-0.16)*Math.sin(Math.PI*2/3*0)),
                (float)((1+0.16)*Math.cos(Math.PI*2/3*0)),(float)((1+0.16)*Math.sin(Math.PI*2/3*0)),
                (float)((1-0.16)*Math.cos(Math.PI*2/3*1)),(float)((1-0.16)*Math.sin(Math.PI*2/3*1)),
                (float)((1+0.16)*Math.cos(Math.PI*2/3*1)),(float)((1+0.16)*Math.sin(Math.PI*2/3*1)),
                (float)((1-0.16)*Math.cos(Math.PI*2/3*2)),(float)((1-0.16)*Math.sin(Math.PI*2/3*2)),
                (float)((1+0.16)*Math.cos(Math.PI*2/3*2)),(float)((1+0.16)*Math.sin(Math.PI*2/3*2)),
                (float)((1-0.16)*Math.cos(Math.PI*2/3*3)),(float)((1-0.16)*Math.sin(Math.PI*2/3*3)),
                (float)((1+0.16)*Math.cos(Math.PI*2/3*3)),(float)((1+0.16)*Math.sin(Math.PI*2/3*3)),



                2000,2000,2000,2000,2000,2000,2000,2000,//Для проверки правильности: если улетит вдаль, то залезли куда не следует
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
        };
        VERTEX_SUM_ROUND_COUNT = vertices0.length/2;

        float vertices[] = Arrays.copyOf(vertices0,POSITION_COUNT*
                (VERTEX_SUM_ROUND_COUNT+VERTEX_ROUND_COUNT+VERTEX_LOWPOLY_ROUND_COUNT+VERTEX_RING_COUNT+VERTEX_RING2_COUNT+VERTEX_RING3_COUNT+VERTEX_PLUS_COUNT+
                        VERTEX_ELLIPSE_COUNT+VERTEX_BEZIER_COUNT*ANIMATION_FRAMES+VERTEX_HALF_RING_COUNT+VERTEX_MOUTH_COUNT*ANIMATION_FRAMES
                        +VERTEX_SHARKBODY_COUNT*ANIMATION_FRAMES+VERTEX_ROUNDED_TRIANGLE_INCENTER_COUNT+VERTEX_ROUNDED_TRIANGLE_OUTCENTER_COUNT+
                        VERTEX_SHARKMOUTH_COUNT+VERTEX_PARTICLE_COUNT+VERTEX_BOSS1_COUNT*ANIMATION_FRAMES));

        int idx=vertices0.length;
        //Заполненный круг радиуса 1
        vertices[idx++]=0;          vertices[idx++]=0;      if (POSITION_COUNT==3) vertices[idx++]=0;
        if (POSITION_COUNT==3) vertices[idx++]=0;
        int outerVertexCount=VERTEX_ROUND_COUNT-1;
        float percent,rad=0,outer_x,outer_y;
        for (int i=0;i<outerVertexCount;++i){
            percent=(i/(float)(outerVertexCount-1));
            //Log.wtf(LOG_TAG,"angle "+percent*360);
            rad=(float)(percent*2*Math.PI);
            outer_x=(float)(Math.cos(rad));
            outer_y=(float)(Math.sin(rad));
            vertices[idx++]=outer_x;
            vertices[idx++]=outer_y;
            if (POSITION_COUNT==3) vertices[idx++]=0;
        }
        //VERTEX_SUM_ROUND_COUNT = vertices0.length/2;

        //LowPoly круг радиуса 1
        vertices[idx++]=0;        vertices[idx++]=0;    if (POSITION_COUNT==3) vertices[idx++]=0;
        if (POSITION_COUNT==3) vertices[idx++]=0;
        outerVertexCount=VERTEX_LOWPOLY_ROUND_COUNT-1;
        for (int i=0;i<outerVertexCount;++i){
            percent=(i/(float)(outerVertexCount-1));
            rad=(float)(percent*2*Math.PI);
            outer_x=(float)(Math.cos(rad));
            outer_y=(float)(Math.sin(rad));
            vertices[idx++]=outer_x;
            vertices[idx++]=outer_y;
            if (POSITION_COUNT==3) vertices[idx++]=0;
        }
        VERTEX_SUM_LOWPOLY_ROUND_COUNT = VERTEX_SUM_ROUND_COUNT+VERTEX_ROUND_COUNT;

        //Кольцо с средним радиусом 1 и толщиной 0,1886792452830189
        float radius = 1, ringwidth = 0.1886792452830189f/2;
        outerVertexCount=VERTEX_RING_COUNT-3;
        for (int i=0;i<outerVertexCount+3;++i){
            percent=(i/(float)(outerVertexCount+1));
            //Log.wtf(LOG_TAG,"angle "+percent*360);
            rad=(float)(percent*2*Math.PI);
            if (i%2==0)//Чётные числа для внешего радиуса
            {
                outer_x=(float)((radius+ringwidth)*Math.cos(rad));
                outer_y=(float)((radius+ringwidth)*Math.sin(rad));
            }
            else{
                outer_x=(float)((radius-ringwidth)*Math.cos(rad));
                outer_y=(float)((radius-ringwidth)*Math.sin(rad));
            }
            vertices[idx++]=outer_x;
            vertices[idx++]=outer_y;
            if (POSITION_COUNT==3) vertices[idx++]=0;
        }
        VERTEX_SUM_RING_COUNT = VERTEX_SUM_LOWPOLY_ROUND_COUNT+VERTEX_LOWPOLY_ROUND_COUNT;


        //Кольцо с средним радиусом 1 и толщиной 0,3773584905660377
        radius = 1; ringwidth = 0.3773584905660377f/2;
        outerVertexCount=VERTEX_RING_COUNT-3;
        for (int i=0;i<outerVertexCount+3;++i){
            percent=(i/(float)(outerVertexCount+1));
            //Log.wtf(LOG_TAG,"angle "+percent*360);
            rad=(float)(percent*2*Math.PI);
            if (i%2==0)//Чётные числа для внешего радиуса
            {
                outer_x=(float)((radius+ringwidth)*Math.cos(rad));
                outer_y=(float)((radius+ringwidth)*Math.sin(rad));
            }
            else{
                outer_x=(float)((radius-ringwidth)*Math.cos(rad));
                outer_y=(float)((radius-ringwidth)*Math.sin(rad));
            }
            vertices[idx++]=outer_x;
            vertices[idx++]=outer_y;
            if (POSITION_COUNT==3) vertices[idx++]=0;
        }
        VERTEX_SUM_RING2_COUNT = VERTEX_SUM_RING_COUNT+VERTEX_RING_COUNT;





        //Кольцо с средним радиусом 1 и толщиной 0.1f
        radius = 1; ringwidth = 0.1f/2f;
        outerVertexCount=VERTEX_RING_COUNT-3;
        for (int i=0;i<outerVertexCount+3;++i){
            percent=(i/(float)(outerVertexCount+1));
            //Log.wtf(LOG_TAG,"angle "+percent*360);
            rad=(float)(percent*2*Math.PI);
            if (i%2==0)//Чётные числа для внешего радиуса
            {
                outer_x=(float)((radius+ringwidth)*Math.cos(rad));
                outer_y=(float)((radius+ringwidth)*Math.sin(rad));
            }
            else{
                outer_x=(float)((radius-ringwidth)*Math.cos(rad));
                outer_y=(float)((radius-ringwidth)*Math.sin(rad));
            }
            vertices[idx++]=outer_x;
            vertices[idx++]=outer_y;
            if (POSITION_COUNT==3) vertices[idx++]=0;
        }
        VERTEX_SUM_RING3_COUNT = VERTEX_SUM_RING2_COUNT+VERTEX_RING2_COUNT;


        //Плюсик
        radius = 1; float distToRadius=4;
        outerVertexCount=(VERTEX_PLUS_COUNT-10)/2;        //Log.wtf(LOG_TAG,"outerVertexCount/ "+(outerVertexCount/2+1));
        vertices[idx++]=0;             vertices[idx++]=0;     if (POSITION_COUNT==3) vertices[idx++]=0;
        vertices[idx++]=radius;        vertices[idx++]=radius;if (POSITION_COUNT==3) vertices[idx++]=0;
        for (int i=0;i<outerVertexCount/2+1;++i){
            percent=(i/(float)(outerVertexCount));
            rad=(float)(percent*2*Math.PI);
            outer_x=(float)(radius*Math.cos(rad));
            outer_y=distToRadius+(float)(radius*Math.sin(rad));
            vertices[idx++]=outer_x;
            vertices[idx++]=outer_y;
            if (POSITION_COUNT==3) vertices[idx++]=0;
        }//Верхушка
        vertices[idx++]=-radius;        vertices[idx++]=radius;     if (POSITION_COUNT==3) vertices[idx++]=0;
        for (int i=0;i<outerVertexCount/2+1;++i){
            percent=(i/(float)(outerVertexCount));
            //Log.wtf(LOG_TAG,"angle "+percent*360);
            rad=(float)(percent*2*Math.PI);
            outer_x=-distToRadius-(float)(radius*Math.sin(rad));
            outer_y=(float)(radius*Math.cos(rad));
            vertices[idx++]=outer_x;
            vertices[idx++]=outer_y;
            if (POSITION_COUNT==3) vertices[idx++]=0;
        }//Левая часть
        vertices[idx++]=-radius;        vertices[idx++]=-radius;    if (POSITION_COUNT==3) vertices[idx++]=0;
        for (int i=0;i<outerVertexCount/2+1;++i){
            percent=(i/(float)(outerVertexCount));
            //Log.wtf(LOG_TAG,"angle "+percent*360);
            rad=(float)(percent*2*Math.PI);
            outer_x=-(float)(radius*Math.cos(rad));
            outer_y=-distToRadius-(float)(radius*Math.sin(rad));
            vertices[idx++]=outer_x;
            vertices[idx++]=outer_y;
            if (POSITION_COUNT==3) vertices[idx++]=0;
        }//Низ
        vertices[idx++]=radius;        vertices[idx++]=-radius;    if (POSITION_COUNT==3) vertices[idx++]=0;
        for (int i=0;i<outerVertexCount/2+1;++i){
            percent=(i/(float)(outerVertexCount));
            //Log.wtf(LOG_TAG,"angle "+percent*360);
            rad=(float)(percent*2*Math.PI);
            outer_x=distToRadius+(float)(radius*Math.sin(rad));
            outer_y=-(float)(radius*Math.cos(rad));
            vertices[idx++]=outer_x;
            vertices[idx++]=outer_y;
            if (POSITION_COUNT==3) vertices[idx++]=0;
        }//Правая часть
        vertices[idx++]=radius;        vertices[idx++]=radius;      if (POSITION_COUNT==3) vertices[idx++]=0;
        VERTEX_SUM_PLUS_COUNT = VERTEX_SUM_RING3_COUNT+VERTEX_RING3_COUNT;

        //Эллипс (24 на 11 с толщиной 2)*0.7
        float radius1 = 24*0.7f, radius2 = 11*0.7f; ringwidth = 2*0.7f/2;
        outerVertexCount=VERTEX_ELLIPSE_COUNT-3;
        for (int i=0;i<outerVertexCount+3;++i){
            percent=(i/(float)(outerVertexCount+1));
            //Log.wtf(LOG_TAG,"angle "+percent*360);
            rad=(float)(percent*2*Math.PI);
            if (i%2==0)//Чётные числа для внешего радиуса
            {
                outer_x=(float)((radius1+ringwidth)*Math.cos(rad));
                outer_y=(float)((radius2+ringwidth)*Math.sin(rad));
            }
            else{
                outer_x=(float)((radius1-ringwidth)*Math.cos(rad));
                outer_y=(float)((radius2-ringwidth)*Math.sin(rad));
            }
            vertices[idx++]=outer_x;
            vertices[idx++]=outer_y;
            if (POSITION_COUNT==3) vertices[idx++]=0;
        }
        VERTEX_SUM_ELLIPSE_COUNT = VERTEX_SUM_PLUS_COUNT+VERTEX_PLUS_COUNT;

        float x0, y0, x1, y1, x2=0, y2 = 0; float animationStatus=0;
        x0 = 0; y0 = 11-4.5f; x1 = 0; y1 = 18-4.5f;
        for (int j=0;j<ANIMATION_FRAMES;++j) {
            animationStatus=(j/(float)(ANIMATION_FRAMES-1));
            x2 = -15*animationStatus; y2 = 28-10*animationStatus;//-4.5f;
            outerVertexCount=VERTEX_BEZIER_COUNT;
            for (int i=0;i<outerVertexCount;++i){
                percent=(i/(float)(outerVertexCount-1));
                if (i%2==0)//Чётные числа для внешего радиуса
                {
                    outer_x=(x0*(1-percent)*(1-percent)+2*percent*(1-percent)*x1+percent*percent*x2);
                    outer_y=(y0*(1-percent)*(1-percent)+2*percent*(1-percent)*y1+percent*percent*y2);
                }
                else{//Дополнительная кривая Безье для толщины
                    outer_x=((x0+3)*(1-percent)*(1-percent)+2*percent*(1-percent)*(x1+2)+percent*percent*(x2+2*animationStatus*animationStatus));
                    outer_y=(y0*(1-percent)*(1-percent)+2*percent*(1-percent)*(y1+2)+percent*percent*(y2+2*(1-animationStatus)*(1-animationStatus)));
                    //Множители вроде animationStatus в квадрате, чтобы окончания крылышек не были прямоугольными
                }
                vertices[idx++]=outer_x;
                vertices[idx++]=outer_y;
                if (POSITION_COUNT==3) vertices[idx++]=0;
            }
        }
        VERTEX_SUM_BEZIER_COUNT = VERTEX_SUM_ELLIPSE_COUNT + VERTEX_ELLIPSE_COUNT;

        VERTEX_SUM_HALF_RING_COUNT = VERTEX_SUM_BEZIER_COUNT + VERTEX_BEZIER_COUNT * ANIMATION_FRAMES;
        //Полукольцо с средним радиусом 1 и толщиной 0,1886792452830189. Верхняя часть
        radius = 1; ringwidth = 0.1886792452830189f/2;
        vertices[idx++]= (float)Math.sqrt(Math.pow(radius-ringwidth,2)-Math.pow(2*ringwidth,2))-0.0175f;      vertices[idx++]=ringwidth*2;if (POSITION_COUNT==3) vertices[idx++]=0;
        vertices[idx++]=-(float)Math.sqrt(Math.pow(radius-ringwidth,2)-Math.pow(2*ringwidth,2))+0.0175f;      vertices[idx++]=ringwidth*2;if (POSITION_COUNT==3) vertices[idx++]=0;
        vertices[idx++]=radius-ringwidth;                                                                     vertices[idx++]=0;          if (POSITION_COUNT==3) vertices[idx++]=0;
        vertices[idx++]=-(radius-ringwidth);                                                                  vertices[idx++]=0;          if (POSITION_COUNT==3) vertices[idx++]=0;
        vertices[idx++]=-radius;                                                                              vertices[idx++]=0;          if (POSITION_COUNT==3) vertices[idx++]=0;
        outerVertexCount=VERTEX_HALF_RING_COUNT-5;
        for (int i=0;i<outerVertexCount;++i){
            if (i%2==0)//Чётные числа для внешего радиуса
            {
                percent=(0.5f*i/(float)(outerVertexCount-2));
                //Log.wtf(LOG_TAG,"angle "+percent*360);
                rad=(float)(percent*2*Math.PI);
                outer_x=(float)((radius+ringwidth)*Math.cos(rad));
                outer_y=(float)((radius+ringwidth)*Math.sin(rad));
            }
            else{
                outer_x=(float)((radius-ringwidth)*Math.cos(rad));
                outer_y=(float)((radius-ringwidth)*Math.sin(rad));
            }
            vertices[idx++]=outer_x;
            vertices[idx++]=outer_y;
            if (POSITION_COUNT==3) vertices[idx++]=0;
        }


        float deltaX=9f;
        for (int j=0;j<ANIMATION_FRAMES;++j) {
            animationStatus=(j/(float)(ANIMATION_FRAMES-1));
            vertices[idx++]=4+deltaX;            vertices[idx++]=0;        if (POSITION_COUNT==3) vertices[idx++]=0;
            x0 = 0f; y0 = 0;
            x1 = 0; y1 = 18*1.4f;//1.4f - масштабный множитель
            x2 = (15+15*animationStatus)*1.4f; y2 = (28-25*animationStatus)*1.4f;
            outerVertexCount=VERTEX_MOUTH_COUNT-2;
            for (int i=0;i<outerVertexCount;++i){
                percent=(i/(float)(outerVertexCount-1));
                if (i%2==0)//Чётные числа для внешего радиуса
                {
                    outer_x=(x0*(1-percent)*(1-percent)+2*percent*(1-percent)*x1+percent*percent*x2);
                    outer_y=(y0*(1-percent)*(1-percent)+2*percent*(1-percent)*y1+percent*percent*y2);
                }
                else{//Дополнительная кривая Безье для толщины
                    outer_x=((x0+3*1.4f)*(1-percent)*(1-percent)+2*percent*(1-percent)*(x1+2+2*animationStatus)+percent*percent*(x2/*+2*animationStatus*animationStatus*/));
                    outer_y=(y0*(1-percent)*(1-percent)+2*percent*(1-percent)*(y1-2)+percent*percent*(y2-3*1.4f/*2*(1-animationStatus)*(1-animationStatus)*/));
                    //Множители вроде animationStatus в квадрате, чтобы окончания крылышек не были прямоугольными
                }
                vertices[idx++]=outer_x+deltaX;
                vertices[idx++]=outer_y;
                if (POSITION_COUNT==3) vertices[idx++]=0;
            }
            vertices[idx++]=x2-3.5f*animationStatus+deltaX;            vertices[idx++]=y2-3*1.4f+1.5f*animationStatus;        if (POSITION_COUNT==3) vertices[idx++]=0;
        }
        VERTEX_SUM_MOUTH_COUNT = VERTEX_SUM_HALF_RING_COUNT + VERTEX_HALF_RING_COUNT;




        VERTEX_SUM_SHARKBODY_COUNT=VERTEX_SUM_MOUTH_COUNT + VERTEX_MOUTH_COUNT * ANIMATION_FRAMES;//VERTEX_SUM_SHARKBACK_COUNT+VERTEX_SHARKBACK_COUNT * ANIMATION_FRAMES;
        float [] vertexVector = new float[2];
        deltaX=3;//Толщина линий
        outerVertexCount=VERTEX_SHARKBODY_COUNT/12;        //Log.wtf(LOG_TAG,""+outerVertexCount);
        for (int j=0;j<ANIMATION_FRAMES;++j) {
            animationStatus = (j / (float) (ANIMATION_FRAMES - 1));

            x0 = -36f; y0 = -499;
            x1 = 560; y1 = -329f;//1.4f - масштабный множитель
            x2 = 960f; y2 = 0;
            for (int i=0;i<outerVertexCount;++i){
                percent=(i/(float)(outerVertexCount-1));
                //Log.wtf(LOG_TAG,""+percent);
                sharkBodyBezier1(vertexVector,i%2,percent,animationStatus,x0,y0,x1,y1,x2,y2,deltaX);
                vertices[idx++]=vertexVector[0];                vertices[idx++]=vertexVector[1];                if (POSITION_COUNT==3) vertices[idx++]=0;
                if (i==0) {//первый треугольник
                    //percent=(i/(float)(outerVertexCount-1));
                    sharkBodyBezier1(vertexVector,(i+1)%2,percent,animationStatus,x0,y0,x1,y1,x2,y2,deltaX);
                    vertices[idx++]=vertexVector[0];                vertices[idx++]=vertexVector[1];                if (POSITION_COUNT==3) vertices[idx++]=0;
                }
                else{
                    percent=((i-1)/(float)(outerVertexCount-1));
                    sharkBodyBezier1(vertexVector,(i+1)%2,percent,animationStatus,x0,y0,x1,y1,x2,y2,deltaX);
                    vertices[idx++]=vertexVector[0];                vertices[idx++]=vertexVector[1];                if (POSITION_COUNT==3) vertices[idx++]=0;
                }
                if (i==outerVertexCount-1){//Последний треугольник
                    percent=(i/(float)(outerVertexCount-1));
                    sharkBodyBezier1(vertexVector,(i+1)%2,percent,animationStatus,x0,y0,x1,y1,x2,y2,deltaX);
                    vertices[idx++]=vertexVector[0];                vertices[idx++]=vertexVector[1];                if (POSITION_COUNT==3) vertices[idx++]=0;
                }
                else{
                    percent=((i+1)/(float)(outerVertexCount-1));
                    sharkBodyBezier1(vertexVector,(i+1)%2,percent,animationStatus,x0,y0,x1,y1,x2,y2,deltaX);
                    vertices[idx++]=vertexVector[0];                vertices[idx++]=vertexVector[1];                if (POSITION_COUNT==3) vertices[idx++]=0;
                }
            }
            for (int i=0;i<outerVertexCount;++i){
                percent=(i/(float)(outerVertexCount-1));
                //Log.wtf(LOG_TAG,""+percent);
                sharkBodyBezier1(vertexVector,i%2,percent,animationStatus,x0,y0,x1,y1,x2,y2,deltaX);
                vertices[idx++]=vertexVector[0];                vertices[idx++]=-vertexVector[1];                if (POSITION_COUNT==3) vertices[idx++]=0;
                if (i==0) {//первый треугольник
                    //percent=(i/(float)(outerVertexCount-1));
                    sharkBodyBezier1(vertexVector,(i+1)%2,percent,animationStatus,x0,y0,x1,y1,x2,y2,deltaX);
                    vertices[idx++]=vertexVector[0];                vertices[idx++]=-vertexVector[1];                if (POSITION_COUNT==3) vertices[idx++]=0;
                }
                else{
                    percent=((i-1)/(float)(outerVertexCount-1));
                    sharkBodyBezier1(vertexVector,(i+1)%2,percent,animationStatus,x0,y0,x1,y1,x2,y2,deltaX);
                    vertices[idx++]=vertexVector[0];                vertices[idx++]=-vertexVector[1];                if (POSITION_COUNT==3) vertices[idx++]=0;
                }
                if (i==outerVertexCount-1){//Последний треугольник
                    percent=(i/(float)(outerVertexCount-1));
                    sharkBodyBezier1(vertexVector,(i+1)%2,percent,animationStatus,x0,y0,x1,y1,x2,y2,deltaX);
                    vertices[idx++]=vertexVector[0];                vertices[idx++]=-vertexVector[1];                if (POSITION_COUNT==3) vertices[idx++]=0;
                }
                else{
                    percent=((i+1)/(float)(outerVertexCount-1));
                    sharkBodyBezier1(vertexVector,(i+1)%2,percent,animationStatus,x0,y0,x1,y1,x2,y2,deltaX);
                    vertices[idx++]=vertexVector[0];                vertices[idx++]=-vertexVector[1];                if (POSITION_COUNT==3) vertices[idx++]=0;
                }
            }

            x0 = -36; y0 = -499;
            x1 = -302; y1 = -164;
            x2 = -1113 - 410 * animationStatus; y2 = 0;
            for (int i=0;i<outerVertexCount;++i){
                percent=(i/(float)(outerVertexCount-1));
                //Log.wtf(LOG_TAG,""+percent);
                sharkBodyBezier2(vertexVector,i%2,percent,animationStatus,x0,y0,x1,y1,x2,y2,deltaX);
                vertices[idx++]=vertexVector[0];                vertices[idx++]=vertexVector[1];                if (POSITION_COUNT==3) vertices[idx++]=0;
                if (i==0) {//первый треугольник
                    //percent=(i/(float)(outerVertexCount-1));
                    sharkBodyBezier2(vertexVector,(i+1)%2,percent,animationStatus,x0,y0,x1,y1,x2,y2,deltaX);
                    vertices[idx++]=vertexVector[0];                vertices[idx++]=vertexVector[1];                if (POSITION_COUNT==3) vertices[idx++]=0;
                }
                else{
                    percent=((i-1)/(float)(outerVertexCount-1));
                    sharkBodyBezier2(vertexVector,(i+1)%2,percent,animationStatus,x0,y0,x1,y1,x2,y2,deltaX);
                    vertices[idx++]=vertexVector[0];                vertices[idx++]=vertexVector[1];                if (POSITION_COUNT==3) vertices[idx++]=0;
                }
                if (i==outerVertexCount-1){//Последний треугольник
                    percent=(i/(float)(outerVertexCount-1));
                    sharkBodyBezier2(vertexVector,(i+1)%2,percent,animationStatus,x0,y0,x1,y1,x2,y2,deltaX);
                    vertices[idx++]=vertexVector[0];                vertices[idx++]=vertexVector[1];                if (POSITION_COUNT==3) vertices[idx++]=0;
                }
                else{
                    percent=((i+1)/(float)(outerVertexCount-1));
                    sharkBodyBezier2(vertexVector,(i+1)%2,percent,animationStatus,x0,y0,x1,y1,x2,y2,deltaX);
                    vertices[idx++]=vertexVector[0];                vertices[idx++]=vertexVector[1];                if (POSITION_COUNT==3) vertices[idx++]=0;
                }
            }
            for (int i=0;i<outerVertexCount;++i){
                percent=(i/(float)(outerVertexCount-1));
                //Log.wtf(LOG_TAG,""+percent);
                sharkBodyBezier2(vertexVector,i%2,percent,animationStatus,x0,y0,x1,y1,x2,y2,deltaX);
                vertices[idx++]=vertexVector[0];                vertices[idx++]=-vertexVector[1];                if (POSITION_COUNT==3) vertices[idx++]=0;
                if (i==0) {//первый треугольник
                    //percent=(i/(float)(outerVertexCount-1));
                    sharkBodyBezier2(vertexVector,(i+1)%2,percent,animationStatus,x0,y0,x1,y1,x2,y2,deltaX);
                    vertices[idx++]=vertexVector[0];                vertices[idx++]=-vertexVector[1];                if (POSITION_COUNT==3) vertices[idx++]=0;
                }
                else{
                    percent=((i-1)/(float)(outerVertexCount-1));
                    sharkBodyBezier2(vertexVector,(i+1)%2,percent,animationStatus,x0,y0,x1,y1,x2,y2,deltaX);
                    vertices[idx++]=vertexVector[0];                vertices[idx++]=-vertexVector[1];                if (POSITION_COUNT==3) vertices[idx++]=0;
                }
                if (i==outerVertexCount-1){//Последний треугольник
                    percent=(i/(float)(outerVertexCount-1));
                    sharkBodyBezier2(vertexVector,(i+1)%2,percent,animationStatus,x0,y0,x1,y1,x2,y2,deltaX);
                    vertices[idx++]=vertexVector[0];                vertices[idx++]=-vertexVector[1];                if (POSITION_COUNT==3) vertices[idx++]=0;
                }
                else{
                    percent=((i+1)/(float)(outerVertexCount-1));
                    sharkBodyBezier2(vertexVector,(i+1)%2,percent,animationStatus,x0,y0,x1,y1,x2,y2,deltaX);
                    vertices[idx++]=vertexVector[0];                vertices[idx++]=-vertexVector[1];                if (POSITION_COUNT==3) vertices[idx++]=0;
                }
            }
        }








        //VERTEX_SUM_ROUNDED_TRIANGLE_INCENTER_COUNT=VERTEX_SUM_SHARKBACK_COUNT+VERTEX_SHARKBACK_COUNT * ANIMATION_FRAMES;
        VERTEX_SUM_ROUNDED_TRIANGLE_INCENTER_COUNT=VERTEX_SUM_SHARKBODY_COUNT+VERTEX_SHARKBODY_COUNT * ANIMATION_FRAMES;
        radius = 1; ringwidth = 0.1f/2; float dAngle = (float)(15f/2f*Math.PI/180f);//
        //vertices[idx++]=outer_x+deltaX;        vertices[idx++]=outer_y;        if (POSITION_COUNT==3) vertices[idx++]=0;
        //(float)(1.1*Math.cos(Math.PI*2/5*5)),(float)(1.1*Math.sin(Math.PI*2/5*5)),
        vertices[idx++]=(float)(-2*radius+(radius+ringwidth)*Math.cos(30*Math.PI/180f-dAngle));        vertices[idx++]=(float)((radius+ringwidth)*Math.sin(30*Math.PI/180f-dAngle));        if (POSITION_COUNT==3) vertices[idx++]=0;
        vertices[idx++]=(float)(-2*radius+(radius+ringwidth)*Math.cos(30*Math.PI/180f));        vertices[idx++]=(float)((radius+ringwidth)*Math.sin(30*Math.PI/180f));        if (POSITION_COUNT==3) vertices[idx++]=0;
        vertices[idx++]=-ringwidth*4;        vertices[idx++]=0;        if (POSITION_COUNT==3) vertices[idx++]=0;
        vertices[idx++]=0;        vertices[idx++]=0;        if (POSITION_COUNT==3) vertices[idx++]=0;
        vertices[idx++]=(float)(-2*radius+(radius+ringwidth)*Math.cos(-30*Math.PI/180f+dAngle));        vertices[idx++]=(float)((radius+ringwidth)*Math.sin(-30*Math.PI/180f+dAngle));        if (POSITION_COUNT==3) vertices[idx++]=0;
        vertices[idx++]=(float)(-2*radius+(radius+ringwidth)*Math.cos(-30*Math.PI/180f));        vertices[idx++]=(float)((radius+ringwidth)*Math.sin(-30*Math.PI/180f));        if (POSITION_COUNT==3) vertices[idx++]=0;
        vertices[idx++]=(float)(-2*radius+(radius+ringwidth)*Math.cos(-30*Math.PI/180f));        vertices[idx++]=(float)((radius+ringwidth)*Math.sin(-30*Math.PI/180f));        if (POSITION_COUNT==3) vertices[idx++]=0;
        outerVertexCount=VERTEX_ROUNDED_TRIANGLE_INCENTER_COUNT-7;//8
        for (int i=0;i<outerVertexCount;++i){
            if (i==outerVertexCount-1) percent=((i-1)/(float)(outerVertexCount-2));
            else percent=(i/(float)(outerVertexCount-2));
            rad=(float)((percent-0.5f)*2*Math.PI*60/360);
            //Log.wtf(LOG_TAG,"percent="+percent+" rad="+rad);
            if (i%2!=0){
                outer_x=(float)(-2*radius+(radius+ringwidth)*Math.cos(rad));
                outer_y=(float)((radius+ringwidth)*Math.sin(rad));
            }
            else{
                outer_x=(float)(-2*radius+(radius-ringwidth)*Math.cos(rad));
                outer_y=(float)((radius-ringwidth)*Math.sin(rad));
            }

            vertices[idx++]=outer_x;
            vertices[idx++]=outer_y;
            if (POSITION_COUNT==3) vertices[idx++]=0;
        }





        VERTEX_SUM_ROUNDED_TRIANGLE_OUTCENTER_COUNT=VERTEX_SUM_ROUNDED_TRIANGLE_INCENTER_COUNT+VERTEX_ROUNDED_TRIANGLE_INCENTER_COUNT;
        radius = 1; ringwidth = 0.1f/2; dAngle = (float)(12f/2f*Math.PI/180f);//
        vertices[idx++]=(float)((radius-ringwidth)*Math.cos(45*Math.PI/180f-dAngle));        vertices[idx++]=(float)((radius-ringwidth)*Math.sin(45*Math.PI/180f-dAngle));        if (POSITION_COUNT==3) vertices[idx++]=0;
        vertices[idx++]=(float)((radius-ringwidth)*Math.cos(45*Math.PI/180f));        vertices[idx++]=(float)((radius-ringwidth)*Math.sin(45*Math.PI/180f));        if (POSITION_COUNT==3) vertices[idx++]=0;
        vertices[idx++]=ringwidth*3f;        vertices[idx++]=0;        if (POSITION_COUNT==3) vertices[idx++]=0;
        vertices[idx++]=0;        vertices[idx++]=0;        if (POSITION_COUNT==3) vertices[idx++]=0;
        vertices[idx++]=(float)((radius-ringwidth)*Math.cos(-45*Math.PI/180f+dAngle));        vertices[idx++]=(float)((radius-ringwidth)*Math.sin(-45*Math.PI/180f+dAngle));        if (POSITION_COUNT==3) vertices[idx++]=0;
        vertices[idx++]=(float)((radius-ringwidth)*Math.cos(-45*Math.PI/180f));        vertices[idx++]=(float)((radius-ringwidth)*Math.sin(-45*Math.PI/180f));        if (POSITION_COUNT==3) vertices[idx++]=0;
        vertices[idx++]=(float)((radius-ringwidth)*Math.cos(-45*Math.PI/180f));        vertices[idx++]=(float)((radius-ringwidth)*Math.sin(-45*Math.PI/180f));        if (POSITION_COUNT==3) vertices[idx++]=0;
        outerVertexCount=VERTEX_ROUNDED_TRIANGLE_OUTCENTER_COUNT-7;//8
        for (int i=0;i<outerVertexCount;++i){
            if (i==outerVertexCount-1) percent=((i-1)/(float)(outerVertexCount-2));
            else percent=(i/(float)(outerVertexCount-2));
            rad=(float)((percent-0.5f)*2f*Math.PI*90f/360f);
            //Log.wtf(LOG_TAG,"percent="+percent+" rad="+rad);
            if (i%2==0){
                outer_x=(float)((radius+ringwidth)*Math.cos(rad));
                outer_y=(float)((radius+ringwidth)*Math.sin(rad));
            }
            else{
                outer_x=(float)((radius-ringwidth)*Math.cos(rad));
                outer_y=(float)((radius-ringwidth)*Math.sin(rad));
            }
            //Log.wtf(LOG_TAG,"alpha "+i+" " +rad/Math.PI*180+" radius="+Math.sqrt(outer_x*outer_x+outer_y*outer_y));
            vertices[idx++]=outer_x;
            vertices[idx++]=outer_y;
            if (POSITION_COUNT==3) vertices[idx++]=0;
        }//Верхушка

        VERTEX_SUM_SHARKMOUTH_COUNT = VERTEX_SUM_ROUNDED_TRIANGLE_OUTCENTER_COUNT+VERTEX_ROUNDED_TRIANGLE_OUTCENTER_COUNT;
        radius=103*0.94f;dAngle=(float)(8f/2f*Math.PI/180f);
        float timeyWimeyX, timeyWimeyY;
        for (int i=0;i<3;i++){
            switch (i){
                case 0:
                    x0=-92;y0=-40;x1=-28;y1=7;x2=-92;y2=40;
                    break;
                case 1:
                    x0=92;y0=-40;x1=23;y1=-15;x2=40;y2=-92;
                    break;
                case 2:
                    x0=21.7f;y0=97.9f;x1=16;y1=30;x2=79.7f;y2=60;
                    break;
                default:
                    Log.wtf(LOG_TAG,"Ошибка в цикле для акульего рта");
                    break;
            }
            timeyWimeyX=(x0+x2)*0.5f-x1;
            timeyWimeyY=(y0+y2)*0.5f-y1;
            deltaX=(float)(Math.sqrt(Math.pow(timeyWimeyX,2)+Math.pow(timeyWimeyY,2)))/10f;//Нормируем к единице и умножаем на некое число (ну, делим, на самом деле) - расст. до точек
            timeyWimeyX=timeyWimeyX/deltaX;
            timeyWimeyY=timeyWimeyY/deltaX;

            vertices[idx++]=(float)(radius*Math.cos(Math.atan2(y0,x0)+dAngle));        vertices[idx++]=(float)(radius*Math.sin(Math.atan2(y0,x0)+dAngle));        if (POSITION_COUNT==3) vertices[idx++]=0;
            vertices[idx++]=(float)(radius*Math.cos(Math.atan2(y0,x0)-dAngle));        vertices[idx++]=(float)(radius*Math.sin(Math.atan2(y0,x0)-dAngle));        if (POSITION_COUNT==3) vertices[idx++]=0;
            vertices[idx++]=x1-timeyWimeyX;        vertices[idx++]=y1-timeyWimeyY;        if (POSITION_COUNT==3) vertices[idx++]=0;

            vertices[idx++]=(float)(radius*Math.cos(Math.atan2(y0,x0)-dAngle));        vertices[idx++]=(float)(radius*Math.sin(Math.atan2(y0,x0)-dAngle));        if (POSITION_COUNT==3) vertices[idx++]=0;
            vertices[idx++]=x1-timeyWimeyX;        vertices[idx++]=y1-timeyWimeyY;        if (POSITION_COUNT==3) vertices[idx++]=0;
            vertices[idx++]=x1+timeyWimeyX;        vertices[idx++]=y1+timeyWimeyY;        if (POSITION_COUNT==3) vertices[idx++]=0;

            vertices[idx++]=x1-timeyWimeyX;        vertices[idx++]=y1-timeyWimeyY;        if (POSITION_COUNT==3) vertices[idx++]=0;
            vertices[idx++]=x1+timeyWimeyX;        vertices[idx++]=y1+timeyWimeyY;        if (POSITION_COUNT==3) vertices[idx++]=0;
            vertices[idx++]=(float)(radius*Math.cos(Math.atan2(y2,x2)+dAngle));        vertices[idx++]=(float)(radius*Math.sin(Math.atan2(y2,x2)+dAngle));        if (POSITION_COUNT==3) vertices[idx++]=0;

            vertices[idx++]=x1-timeyWimeyX;        vertices[idx++]=y1-timeyWimeyY;        if (POSITION_COUNT==3) vertices[idx++]=0;
            vertices[idx++]=(float)(radius*Math.cos(Math.atan2(y2,x2)+dAngle));        vertices[idx++]=(float)(radius*Math.sin(Math.atan2(y2,x2)+dAngle));        if (POSITION_COUNT==3) vertices[idx++]=0;
            vertices[idx++]=(float)(radius*Math.cos(Math.atan2(y2,x2)-dAngle));        vertices[idx++]=(float)(radius*Math.sin(Math.atan2(y2,x2)-dAngle));        if (POSITION_COUNT==3) vertices[idx++]=0;
        }

        VERTEX_SUM_PARTICLE_COUNT=VERTEX_SUM_SHARKMOUTH_COUNT+VERTEX_SHARKMOUTH_COUNT;
            for (int j = 0; j < 3; j++) {
                radius = 2f/3f;
                outerVertexCount = VERTEX_PARTICLE_COUNT/9 + 1;
                for (int i = 0; i < outerVertexCount - 1; ++i) {
                    vertices[idx++] = 0;
                    vertices[idx++] = 0;
                    if (POSITION_COUNT == 3) vertices[idx++] = 0;
                    percent = (i / (float) (outerVertexCount - 1));
                    rad = (float) (percent * 2 * Math.PI);
                    outer_x = (float) (radius * (j + 1) * Math.cos(rad));
                    outer_y = (float) (radius * (j + 1) * Math.sin(rad));
                    vertices[idx++] = outer_x;
                    vertices[idx++] = outer_y;
                    if (POSITION_COUNT == 3) vertices[idx++] = 0;

                    percent = ((i + 1) / (float) (outerVertexCount - 1));
                    rad = (float) (percent * 2 * Math.PI);
                    outer_x = (float) (radius * (j + 1) * Math.cos(rad));
                    outer_y = (float) (radius * (j + 1) * Math.sin(rad));
                    vertices[idx++] = outer_x;
                    vertices[idx++] = outer_y;
                    if (POSITION_COUNT == 3) vertices[idx++] = 0;
                }
            }




        VERTEX_SUM_BOSS1_COUNT=VERTEX_SUM_PARTICLE_COUNT + VERTEX_PARTICLE_COUNT;//VERTEX_SUM_SHARKBACK_COUNT+VERTEX_SHARKBACK_COUNT * ANIMATION_FRAMES;
        deltaX=2;//Толщина линий
        outerVertexCount=VERTEX_BOSS1_COUNT/12;        //Log.wtf(LOG_TAG,""+outerVertexCount);
        for (int j=0;j<ANIMATION_FRAMES;++j) {
            animationStatus = (j / (float) (ANIMATION_FRAMES - 1));

            x0 = 50; y0 = -297;
            x1 = 202; y1 = -175;//1.4f - масштабный множитель
            x2 = 348; y2 = 0;
            for (int i=0;i<outerVertexCount;++i){
                percent=(i/(float)(outerVertexCount-1));
                //Log.wtf(LOG_TAG,""+percent);
                boss1BodyBezier1(vertexVector,i%2,percent,animationStatus,x0,y0,x1,y1,x2,y2,deltaX);
                vertices[idx++]=vertexVector[0];                vertices[idx++]=vertexVector[1];                if (POSITION_COUNT==3) vertices[idx++]=0;
                if (i==0) {//первый треугольник
                    //percent=(i/(float)(outerVertexCount-1));
                    boss1BodyBezier1(vertexVector,(i+1)%2,percent,animationStatus,x0,y0,x1,y1,x2,y2,deltaX);
                    vertices[idx++]=vertexVector[0];                vertices[idx++]=vertexVector[1];                if (POSITION_COUNT==3) vertices[idx++]=0;
                }
                else{
                    percent=((i-1)/(float)(outerVertexCount-1));
                    boss1BodyBezier1(vertexVector,(i+1)%2,percent,animationStatus,x0,y0,x1,y1,x2,y2,deltaX);
                    vertices[idx++]=vertexVector[0];                vertices[idx++]=vertexVector[1];                if (POSITION_COUNT==3) vertices[idx++]=0;
                }
                if (i==outerVertexCount-1){//Последний треугольник
                    percent=(i/(float)(outerVertexCount-1));
                    boss1BodyBezier1(vertexVector,(i+1)%2,percent,animationStatus,x0,y0,x1,y1,x2,y2,deltaX);
                    vertices[idx++]=vertexVector[0];                vertices[idx++]=vertexVector[1];                if (POSITION_COUNT==3) vertices[idx++]=0;
                }
                else{
                    percent=((i+1)/(float)(outerVertexCount-1));
                    boss1BodyBezier1(vertexVector,(i+1)%2,percent,animationStatus,x0,y0,x1,y1,x2,y2,deltaX);
                    vertices[idx++]=vertexVector[0];                vertices[idx++]=vertexVector[1];                if (POSITION_COUNT==3) vertices[idx++]=0;
                }
            }
            for (int i=0;i<outerVertexCount;++i){
                percent=(i/(float)(outerVertexCount-1));
                //Log.wtf(LOG_TAG,""+percent);
                boss1BodyBezier1(vertexVector,i%2,percent,animationStatus,x0,y0,x1,y1,x2,y2,deltaX);
                vertices[idx++]=vertexVector[0];                vertices[idx++]=-vertexVector[1];                if (POSITION_COUNT==3) vertices[idx++]=0;
                if (i==0) {//первый треугольник
                    //percent=(i/(float)(outerVertexCount-1));
                    boss1BodyBezier1(vertexVector,(i+1)%2,percent,animationStatus,x0,y0,x1,y1,x2,y2,deltaX);
                    vertices[idx++]=vertexVector[0];                vertices[idx++]=-vertexVector[1];                if (POSITION_COUNT==3) vertices[idx++]=0;
                }
                else{
                    percent=((i-1)/(float)(outerVertexCount-1));
                    boss1BodyBezier1(vertexVector,(i+1)%2,percent,animationStatus,x0,y0,x1,y1,x2,y2,deltaX);
                    vertices[idx++]=vertexVector[0];                vertices[idx++]=-vertexVector[1];                if (POSITION_COUNT==3) vertices[idx++]=0;
                }
                if (i==outerVertexCount-1){//Последний треугольник
                    percent=(i/(float)(outerVertexCount-1));
                    boss1BodyBezier1(vertexVector,(i+1)%2,percent,animationStatus,x0,y0,x1,y1,x2,y2,deltaX);
                    vertices[idx++]=vertexVector[0];                vertices[idx++]=-vertexVector[1];                if (POSITION_COUNT==3) vertices[idx++]=0;
                }
                else{
                    percent=((i+1)/(float)(outerVertexCount-1));
                    boss1BodyBezier1(vertexVector,(i+1)%2,percent,animationStatus,x0,y0,x1,y1,x2,y2,deltaX);
                    vertices[idx++]=vertexVector[0];                vertices[idx++]=-vertexVector[1];                if (POSITION_COUNT==3) vertices[idx++]=0;
                }
            }

            x0 = 50; y0 = -297;
            x1 = -60; y1 = -60;
            x2 = -266 - 119 * animationStatus; y2 = 0;
            for (int i=0;i<outerVertexCount;++i){
                percent=(i/(float)(outerVertexCount-1));
                //Log.wtf(LOG_TAG,""+percent);
                boss1BodyBezier2(vertexVector,i%2,percent,animationStatus,x0,y0,x1,y1,x2,y2,deltaX);
                vertices[idx++]=vertexVector[0];                vertices[idx++]=vertexVector[1];                if (POSITION_COUNT==3) vertices[idx++]=0;
                if (i==0) {//первый треугольник
                    //percent=(i/(float)(outerVertexCount-1));
                    boss1BodyBezier2(vertexVector,(i+1)%2,percent,animationStatus,x0,y0,x1,y1,x2,y2,deltaX);
                    vertices[idx++]=vertexVector[0];                vertices[idx++]=vertexVector[1];                if (POSITION_COUNT==3) vertices[idx++]=0;
                }
                else{
                    percent=((i-1)/(float)(outerVertexCount-1));
                    boss1BodyBezier2(vertexVector,(i+1)%2,percent,animationStatus,x0,y0,x1,y1,x2,y2,deltaX);
                    vertices[idx++]=vertexVector[0];                vertices[idx++]=vertexVector[1];                if (POSITION_COUNT==3) vertices[idx++]=0;
                }
                if (i==outerVertexCount-1){//Последний треугольник
                    percent=(i/(float)(outerVertexCount-1));
                    boss1BodyBezier2(vertexVector,(i+1)%2,percent,animationStatus,x0,y0,x1,y1,x2,y2,deltaX);
                    vertices[idx++]=vertexVector[0];                vertices[idx++]=vertexVector[1];                if (POSITION_COUNT==3) vertices[idx++]=0;
                }
                else{
                    percent=((i+1)/(float)(outerVertexCount-1));
                    boss1BodyBezier2(vertexVector,(i+1)%2,percent,animationStatus,x0,y0,x1,y1,x2,y2,deltaX);
                    vertices[idx++]=vertexVector[0];                vertices[idx++]=vertexVector[1];                if (POSITION_COUNT==3) vertices[idx++]=0;
                }
            }
            for (int i=0;i<outerVertexCount;++i){
                percent=(i/(float)(outerVertexCount-1));
                //Log.wtf(LOG_TAG,""+percent);
                boss1BodyBezier2(vertexVector,i%2,percent,animationStatus,x0,y0,x1,y1,x2,y2,deltaX);
                vertices[idx++]=vertexVector[0];                vertices[idx++]=-vertexVector[1];                if (POSITION_COUNT==3) vertices[idx++]=0;
                if (i==0) {//первый треугольник
                    //percent=(i/(float)(outerVertexCount-1));
                    boss1BodyBezier2(vertexVector,(i+1)%2,percent,animationStatus,x0,y0,x1,y1,x2,y2,deltaX);
                    vertices[idx++]=vertexVector[0];                vertices[idx++]=-vertexVector[1];                if (POSITION_COUNT==3) vertices[idx++]=0;
                }
                else{
                    percent=((i-1)/(float)(outerVertexCount-1));
                    boss1BodyBezier2(vertexVector,(i+1)%2,percent,animationStatus,x0,y0,x1,y1,x2,y2,deltaX);
                    vertices[idx++]=vertexVector[0];                vertices[idx++]=-vertexVector[1];                if (POSITION_COUNT==3) vertices[idx++]=0;
                }
                if (i==outerVertexCount-1){//Последний треугольник
                    percent=(i/(float)(outerVertexCount-1));
                    boss1BodyBezier2(vertexVector,(i+1)%2,percent,animationStatus,x0,y0,x1,y1,x2,y2,deltaX);
                    vertices[idx++]=vertexVector[0];                vertices[idx++]=-vertexVector[1];                if (POSITION_COUNT==3) vertices[idx++]=0;
                }
                else{
                    percent=((i+1)/(float)(outerVertexCount-1));
                    boss1BodyBezier2(vertexVector,(i+1)%2,percent,animationStatus,x0,y0,x1,y1,x2,y2,deltaX);
                    vertices[idx++]=vertexVector[0];                vertices[idx++]=-vertexVector[1];                if (POSITION_COUNT==3) vertices[idx++]=0;
                }
            }

        }











        vertexData = ByteBuffer
                .allocateDirect(vertices.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexData.put(vertices);

        //Log.wtf(LOG_TAG,"Массив вершин "+ vertices.length*4/1024+" килобайт");
    }//Запись массива в видеопамять?

    private void bindData() {
        // примитивы
        int aPositionLocation = glGetAttribLocation(programId, "a_Position");
        vertexData.position(0);
        glVertexAttribPointer(aPositionLocation, POSITION_COUNT, GL_FLOAT,
                false, 0, vertexData);
        glEnableVertexAttribArray(aPositionLocation);

        // цвет
        uColorLocation = glGetUniformLocation(programId, "u_Color");

        // матрица
        uMatrixLocation = glGetUniformLocation(programId, "u_Matrix");
    }

    private void createProjectionMatrix(int width, int height) {
        //if (width<height) {Log.wtf(LOG_TAG,"createProjectionMatrix width<height");return;}

        float ratio;// = 1;
        float left = -1;//360
        float right = 1;
        float bottom = -1;
        float top = 1;
        left *= 360;right *= 360;bottom*=360;top*=360;

        //left *= 2;right *= 2;bottom*=2;top*=2;

        float near = 2;
        float far = 12;
        if (width > height) {
            ratio = (float) width / height;
            left *= ratio;
            right *= ratio;
        } else {
            ratio = (float) height / width;
            bottom *= ratio;
            top *= ratio;
        }

        //Log.wtf(LOG_TAG,"Screenwidth="+width+" height="+height);



        protagonist.setScreen(width, height);
        camera.setScreen(width, height, right, top);
        for (int j=0;j<levelNum;j++){
            levelArray[j].setScreen(width, height);
        }

        //Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);//Перспектива 3D
        Matrix.orthoM(mProjectionMatrix, 0, left, right, bottom, top, near, far);  //Прямоугольная проекция
    }

    private void createViewMatrix() {
        // точка положения камеры
        float eyeX = 0;
        float eyeY = 0;
        float eyeZ = 3;

        // точка направления камеры
        float centerX = 0;
        float centerY = 0;
        float centerZ = 0;

        // up-вектор
        float upX = 0;
        float upY = 1;
        float upZ = 0;

        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);
    }//Первоначальная настройка, пости ни на что не dkbztn

    private void bindMatrix() {
        Matrix.multiplyMM(mMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMatrix, 0, mProjectionMatrix, 0, mMatrix, 0);
        glUniformMatrix4fv(uMatrixLocation, 1, false, mMatrix, 0);
    }

    @Override
    public void onDrawFrame(GL10 arg0) {
        glClear(GL_COLOR_BUFFER_BIT);// | GL_DEPTH_BUFFER_BIT);//glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);//С тестом глубины
/*
        if (false) {
            drawAxes();

            glUniform4f(uColorLocation, 0.0f, 0.0f, 1.0f, 0.5f);
            drawSquare(-300, 300, 50);
            drawSquare(-200, 300, 50, 45);
            drawPentagon(-100, 300, 25, 0);
            drawRound(-300, 200, 25);
            drawLowpolyRound(-200, 200, 25);
            drawRoundedTriangleInCenter(-100, 200, 25, 0);
            drawRoundedTriangleOutCenter(-100, 200, 25, 0);
            drawPlus(-300, 100, 25 / 5f, 45);
            drawEllipse(-200, 100, 3, -45);
            drawTriangle(-100, 100, 25, -90);

            drawRing2(-300, -100, 25);
            drawRing(-200, -100, 25);
            drawRing3(-100, -100, 25);

            drawBezier(500, 300, 2, 0, 0);
            drawBezier(550, 300, 2, 0, 0.5f);
            drawBezier(600, 300, 2, 0, 1);
            drawBezier2(500, 200, 2, 0, 0);
            drawBezier2(550, 200, 2, 0, 0.5f);
            drawBezier2(600, 200, 2, 0, 1);
            drawBezier3(500, 100, 2, 0, 0);
            drawBezier3(550, 100, 2, 0, 0.5f);
            drawBezier3(600, 100, 2, 0, 1);
            drawBezier4(500, 0, 2, 0, 0);
            drawBezier4(550, 0, 2, 0, 0.5f);
            drawBezier4(600, 0, 2, 0, 1);


            drawHalfRing(-300, -200, 25);
            drawHalfRings(-200, -200, 25, 45, 0);
            drawHalfRings(-100, -200, 25, 45, 30);


            drawMouth(-300, -300, -45, 0);
            drawMouth(-200, -300, -45, 0.5f);
            drawMouth(-100, -300, -45, 1);

            drawSharkBody(-450, 350, 45, 0, 0.1f);
            drawSharkBody(-580, 350, 45, 1, 0.1f);

            int DELETEIT=4;boolean testFlashing=false;

            float radius=2f*DELETEIT;
            if (testFlashing) glUniform4f(uColorLocation, 1.0f, 1.0f, 1.0f, 0.13f*foods_array[0].getMultiplSnake());
            else glUniform4f(uColorLocation, 1.0f, 1.0f, 1.0f, 0.13f);
            for (int i=0;i<6;i++){
                drawSquare(300, 100, radius*(i+1),i*360/(6f));
                //drawLowpolyRound(100, 100, radius*(i+1));
            }
            drawRound(300, 100, radius*4);

            radius=2f;
            for (int i=0;i<6;i++){
                drawSquare(300, 200, radius*(i+1),i*360/(6f));
            }
            drawRound(300, 200, radius*4);
        }
*/


        if (changeBackgroundColorTime>=1){//Цвет уже сменился
            for (int i=0;i<3;i++){
                curColor[i]=(float)((levelArray[currentLevel].getColor()>>(2-i)*8)&0xFF)/255f;
            }
        }
        else {
            if (shouldIChangeLevel!=0){//!changeBackgroundColorAccepted) {//Уровень только что сменился
                for (int i=0;i<3;i++){
                    oldColor[i] = curColor[i];
                    newColor[i]=(float)((levelArray[currentLevel].getColor()>>(2-i)*8)&0xFF)/255f;
                }
            } else {
                for (int i=0;i<3;i++){
                    curColor[i] = newColor[i]*changeBackgroundColorTime+oldColor[i]*(1-changeBackgroundColorTime);
                }
            }
        }
        //Зависимость цвета от удаления от центра координат. Дальше - темнее
                float distMult=1*Math.min(
                Math.max(1.66f-((float)Math.sqrt(Math.pow(protagonist.getCurrentX(),2)+Math.pow(protagonist.getCurrentY(),2)))/3000f,0.26f)
                ,1);
        glClearColor(curColor[0]*distMult,curColor[1]*distMult, curColor[2]*distMult, 0f);//Цвет фона
        //glClearColor(curColor[0],curColor[1], curColor[2], 0f);//Цвет фона

        this.setColor(0);
        if (protagonist==null) {Log.wtf(LOG_TAG,"Лолкекчебурек, нуль");return;}
        protagonist.draw(this);
        particle.draw(this,camera);
        this.setColor(0);
        levelArray[currentLevel].draw(this,camera);
        this.setColor(0);
        for (int i=0;i<foods_arraySizeTest;i++){
            foods_arrayTest[i].draw(this,camera);
        }//Обновление местоположения еды Test


        //drawSharkBody(protagonist.getCurrentX(),protagonist.getCurrentY(),protagonist.getOrientationInDegrees(),Math.abs(protagonist.canvasSnake - 0.5f) * 2f,0.1f*3);
        //drawSharkBody2(protagonist.getCurrentX(),protagonist.getCurrentY(),protagonist.getOrientationInDegrees()+180,Math.abs(protagonist.canvasSnake - 0.5f) * 2f,0.1f*3);
        //drawBossBody(protagonist.getCurrentX(),protagonist.getCurrentY(),protagonist.getOrientationInDegrees()+180,Math.abs(protagonist.canvasSnake - 0.5f) * 2f,1);

        dt=((float)(System.currentTimeMillis()-currentTime))/1000f;
        currentTime = System.currentTimeMillis();
        if (true) {
            countFPS++;
            currentTimeFPS = System.currentTimeMillis();
            if (currentTimeFPS - timeFPS >= 1000)
            {
                timeFPS = currentTimeFPS - timeFPS;
                realFPS = (float) countFPS / timeFPS * 1000f;
                if (realFPS < 59)
                    Log.wtf(LOG_TAG, "FPS:" + (new DecimalFormat("#0.0").format(realFPS)));
                countFPS = 0;
                timeFPS = currentTimeFPS;
            }
        }//////////////////Отображение ФПС

        camera.updateMovement(dt, protagonist);
        Matrix.setLookAtM(mViewMatrix, 0, camera.getCurrentX(), camera.getCurrentY(), 3, camera.getCurrentX(), camera.getCurrentY(), 0, 0, 1, 0);

        protagonist.updateMapPosition(dt,isFingerDown, isDoubleTapped,touchX,touchY, camera);

        //this.updateAccelerometer();
        //protagonist.updateMapPosition(dt,accelerometerRadius,accelerometerOrientation);

        particle.updateMapPosition(dt, protagonist);

        shouldIChangeLevel = levelArray[currentLevel].updateFoodMapPosition(dt, protagonist);
        if (shouldIChangeLevel != 0) {
            changeBackgroundColorTime = 0;
            currentLevel = currentLevel + shouldIChangeLevel;
            Log.wtf(LOG_TAG,"New level="+currentLevel);
        } else {
            changeBackgroundColorTime = changeBackgroundColorTime + dt;
        }
/*
        for (int i=0;i<foods_arraySize;i++){
            foods_array[i].updateMapPosition(dt);
        }//Обновление местоположения еды (мелких)
        */
        for (int i=0;i<foods_arraySizeTest;i++){
            foods_arrayTest[i].updateMapPositionTest(dt);
        }//Обновление местоположения еды Test

    }






    public void sendTouch(boolean isFingerDown, boolean isDoubleTapped, float touchX, float touchY){
        this.touchX=touchX;this.touchY=touchY;
        this.isFingerDown=isFingerDown;
        this.isDoubleTapped=isDoubleTapped;
    }

    //Обработка данных с сенсоров
    public void sendValuesAccel(float [] valuesAccel){
        //Log.wtf(LOG_TAG,"Поступили данные "+valuesAccel[0]+" "+valuesAccel[1]+" "+valuesAccel[2]);
        //float [] dddDDDDDDDDDDDDDD = new float[3];
        if (isFirstTime){
            for (int i=0;i<averegeN;i++){
                for (int j=0;j<3;j++){
                    valuesAccelAverage[i][j]=valuesAccel[j];
                }
            }
        }
        else{
            for (int j=0;j<3;j++){
                valuesAccelAverage[currentValuesAccelN][j]=valuesAccel[j]; ///Замена значений на новые
            }
            currentValuesAccelN=(currentValuesAccelN+1)%averegeN;
        }

        if (isFirstTime){
            isFirstTime=false;

            for (int i=0;i<3;i++){
                valuesAccelN[i]=(float)(valuesAccel[i]/Math.sqrt(Math.pow(valuesAccel[0],2)+Math.pow(valuesAccel[1],2)+Math.pow(valuesAccel[2],2)));
            }
            nulX=(float)(-Math.atan2(valuesAccelN[0],Math.sqrt(Math.pow(valuesAccelN[1],2)+Math.pow(valuesAccelN[2],2)))/Math.PI*180);
            nulY=(float)(Math.atan2(valuesAccelN[1],Math.sqrt(Math.pow(valuesAccelN[0],2)+Math.pow(valuesAccelN[2],2)))/Math.PI*180);

            //isPhoneScreenUp
            isPhoneScreenUp = valuesAccel[2] > 0;
            //Log.wtf(LOG_TAG,"firstTime. isPhoneScreenUp = "+isPhoneScreenUp);
        }
    }
    public void setIsFirstTimeTrue(){isFirstTime=true;}
    private void updateAccelerometer(){
        float [] dddDDDDDDDDDDDDDD = new float[3];

        for (int i=0;i<3;i++){//Столбец
            //valuesAccel[i]=0;
            dddDDDDDDDDDDDDDD[i]=0;
            for (int j=0;j<averegeN;j++){
                dddDDDDDDDDDDDDDD[i]=dddDDDDDDDDDDDDDD[i]+valuesAccelAverage[j][i]/(float) averegeN;
                //valuesAccel[i]=valuesAccel[i]+valuesAccelAverage[i][j]/(float) averegeN;
            }
            //Log.wtf(LOG_TAG,"Усреднённые данные["+i+"] "+dddDDDDDDDDDDDDDD[i]);
        }
        for (int i=0;i<3;i++){
            valuesAccelN[i]=(float)(dddDDDDDDDDDDDDDD[i]/Math.sqrt(Math.pow(dddDDDDDDDDDDDDDD[0],2)+Math.pow(dddDDDDDDDDDDDDDD[1],2)+Math.pow(dddDDDDDDDDDDDDDD[2],2)));
            //valuesAccelN[i]=(float)(valuesAccel[i]/Math.sqrt(Math.pow(valuesAccel[0],2)+Math.pow(valuesAccel[1],2)+Math.pow(valuesAccel[2],2)));
            //Log.wtf(LOG_TAG,"Нормированные данные ["+i+"] "+valuesAccelN[i]);
        }
        deltaY=(float)(Math.atan2(valuesAccelN[1],Math.sqrt(Math.pow(valuesAccelN[0],2)+Math.pow(valuesAccelN[2],2)))/Math.PI*180);
        deltaX=(float)(-Math.atan2(valuesAccelN[0],Math.sqrt(Math.pow(valuesAccelN[1],2)+Math.pow(valuesAccelN[2],2)))/Math.PI*180);
        if (isFirstTime){
            return;
        }
        //Log.wtf(LOG_TAG,"updACC+ "+deltaX+" "+nulX+" "+deltaY+" "+nulY);
        if (isPhoneScreenUp){
            this.accelerometerOrientation = (float) Math.atan2(deltaX-nulX,deltaY-nulY);}
        else{
            this.accelerometerOrientation = (float) Math.atan2(deltaX-nulX,deltaY-nulY)+(float) Math.PI;
        }//Ленивый режим
        this.accelerometerRadius = (float)Math.sqrt(Math.pow(deltaX-nulX,2)+Math.pow(deltaY-nulY,2));
        //Log.wtf(LOG_TAG,"updACC "+accelerometerOrientation+" "+accelerometerRadius);
    }


    private void drawAxes() {
        Matrix.setIdentityM(mModelMatrix, 0);
        bindMatrix();

        glLineWidth(3);
        glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 1.0f);//x red
        glDrawArrays(GL_LINES, 0, 2);

        glUniform4f(uColorLocation, 0.0f, 0.0f, 1.0f, 1.0f);//y blue
        glDrawArrays(GL_LINES, 2, 2);
    }
    private void drawSquare(float centerX, float centerY, float sizeScale){
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix,0,centerX,centerY,0);
        Matrix.scaleM(mModelMatrix,0,sizeScale,sizeScale,1);
        bindMatrix();
        glDrawArrays(GL_TRIANGLE_FAN, 4, 4);
    }//Нарисовать квадрат со стороной sizeScale
    void drawSquareTransfered(float centerX, float centerY, float sizeScale, float orientation, float offsetX, float offsetY){
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix,0,centerX,centerY,0);
        Matrix.scaleM(mModelMatrix,0,sizeScale,sizeScale,1);
        Matrix.rotateM(mModelMatrix,0,orientation,0,0,1);
        Matrix.translateM(mModelMatrix,0,offsetX/sizeScale,offsetY/sizeScale,0);
        bindMatrix();
        glDrawArrays(GL_TRIANGLE_FAN, 4, 4);
    }//Нарисовать квадрат со стороной sizeScale
    void drawSquaresTransfered(float centerX, float centerY, float orientation, float deltaAngle){
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix,0,centerX,centerY,0);
        Matrix.rotateM(mModelMatrix,0,orientation,0,0,1);
        Matrix.translateM(mModelMatrix,0,2,16*0.7f,0);

        Matrix.rotateM(mModelMatrix,0,deltaAngle,0,0,1);
        Matrix.translateM(mModelMatrix,0,50,0,0);//50=100/2
        Matrix.scaleM(mModelMatrix,0,100,4,1);//100
        bindMatrix();
        glDrawArrays(GL_TRIANGLE_FAN, 4, 4);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix,0,centerX,centerY,0);
        Matrix.rotateM(mModelMatrix,0,orientation,0,0,1);
        Matrix.translateM(mModelMatrix,0,2,-16*0.7f,0);

        Matrix.rotateM(mModelMatrix,0,-deltaAngle,0,0,1);
        Matrix.translateM(mModelMatrix,0,50,0,0);//50=100/2
        Matrix.scaleM(mModelMatrix,0,100,4,1);//100
        bindMatrix();
        glDrawArrays(GL_TRIANGLE_FAN, 4, 4);
    }//Нарисовать растянутый смещённый квадрат
    void drawSquaresTransferedWithScale(float centerX, float centerY, float orientation, float deltaAngle, float scale){
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix,0,centerX,centerY,0);
        Matrix.rotateM(mModelMatrix,0,orientation,0,0,1);
        Matrix.translateM(mModelMatrix,0,2,16*0.7f,0);

        Matrix.rotateM(mModelMatrix,0,deltaAngle,0,0,1);
        Matrix.translateM(mModelMatrix,0,scale,0,0);//50=100/2
        Matrix.scaleM(mModelMatrix,0,scale*2,3,1);//100
        bindMatrix();
        glDrawArrays(GL_TRIANGLE_FAN, 4, 4);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix,0,centerX,centerY,0);
        Matrix.rotateM(mModelMatrix,0,orientation,0,0,1);
        Matrix.translateM(mModelMatrix,0,2,-16*0.7f,0);

        Matrix.rotateM(mModelMatrix,0,-deltaAngle,0,0,1);
        Matrix.translateM(mModelMatrix,0,scale,0,0);//50=100/2
        Matrix.scaleM(mModelMatrix,0,scale*2,3,1);//100
        bindMatrix();
        glDrawArrays(GL_TRIANGLE_FAN, 4, 4);
    }//Нарисовать растянутый смещённый квадрат
    void drawSquare(float centerX, float centerY, float sizeScale, float orientation){
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix,0,centerX,centerY,0);
        Matrix.scaleM(mModelMatrix,0,sizeScale,sizeScale,1);
        Matrix.rotateM(mModelMatrix,0,orientation,0,0,1);
        bindMatrix();
        glDrawArrays(GL_TRIANGLE_FAN, 4, 4);
    }//Нарисовать квадрат со стороной sizeScale, повёрнутый на orientation градусов
    void drawPentagon(float centerX, float centerY, float sizeScale, float orientation) {
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix,0,centerX,centerY,0);
        Matrix.scaleM(mModelMatrix,0,sizeScale,sizeScale,1);
        Matrix.rotateM(mModelMatrix,0,orientation,0,0,1);
        bindMatrix();
        glDrawArrays(GL_TRIANGLE_STRIP, 8, 12);
    }
    void drawTriangle(float centerX, float centerY, float radius, float orientation) {
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix,0,centerX,centerY,0);
        Matrix.scaleM(mModelMatrix,0,radius,radius,1);
        Matrix.rotateM(mModelMatrix,0,orientation,0,0,1);
        bindMatrix();
        glDrawArrays(GL_TRIANGLE_STRIP, 20, 8);
    }

    void drawRound(float centerX, float centerY, float radius){
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix,0,centerX,centerY,0);
        Matrix.scaleM(mModelMatrix,0,radius,radius,1);
    bindMatrix();
    glDrawArrays(GL_TRIANGLE_FAN,VERTEX_SUM_ROUND_COUNT,VERTEX_ROUND_COUNT);
}//Нарисовать заполненный круг вершинами
    public void drawRoundTransfered(float centerX, float centerY, float radius, float orientation, float offsetX, float offsetY){
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix,0,centerX,centerY,0);
        Matrix.scaleM(mModelMatrix,0,radius,radius,1);
        Matrix.rotateM(mModelMatrix,0,orientation,0,0,1);
        Matrix.translateM(mModelMatrix,0,offsetX/radius,offsetY/radius,0);
        bindMatrix();
        glDrawArrays(GL_TRIANGLE_FAN,VERTEX_SUM_ROUND_COUNT,VERTEX_ROUND_COUNT);
    }//Нарисовать заполненный круг вершинами, смещённый перед рисованием
    void drawLowpolyRound(float centerX, float centerY, float radius){
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix,0,centerX,centerY,0);
        Matrix.scaleM(mModelMatrix,0,radius,radius,1);
        bindMatrix();
        glDrawArrays(GL_TRIANGLE_FAN,VERTEX_SUM_LOWPOLY_ROUND_COUNT,VERTEX_LOWPOLY_ROUND_COUNT);
    }//Нарисовать заполненный круг вершинами
    void drawLowpolyRoundTransfered(float centerX, float centerY, float radius, float orientation, float offsetX, float offsetY){
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix,0,centerX,centerY,0);
        Matrix.scaleM(mModelMatrix,0,radius,radius,1);
        Matrix.rotateM(mModelMatrix,0,orientation,0,0,1);
        Matrix.translateM(mModelMatrix,0,offsetX/radius,offsetY/radius,0);
        bindMatrix();
        glDrawArrays(GL_TRIANGLE_FAN,VERTEX_SUM_LOWPOLY_ROUND_COUNT,VERTEX_LOWPOLY_ROUND_COUNT);
    }//Нарисовать заполненный круг вершинами, смещённый перед рисованием
    void drawRing(float centerX, float centerY, float radius){
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix,0,centerX,centerY,0);
        Matrix.scaleM(mModelMatrix,0,radius,radius,1);
        bindMatrix();
        glDrawArrays(GL_TRIANGLE_STRIP,VERTEX_SUM_RING_COUNT,VERTEX_RING_COUNT);
    }//Нарисовать кольцо с толщиной 0,1886792452830189 радиуса
    void drawRing2(float centerX, float centerY, float radius){
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix,0,centerX,centerY,0);
        Matrix.scaleM(mModelMatrix,0,radius,radius,1);
        bindMatrix();
        glDrawArrays(GL_TRIANGLE_STRIP,VERTEX_SUM_RING2_COUNT,VERTEX_RING2_COUNT);
    }//Нарисовать кольцо с толщиной 0,3773584905660377 радиуса
    void drawRing3(float centerX, float centerY, float radius){
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix,0,centerX,centerY,0);
        Matrix.scaleM(mModelMatrix,0,radius,radius,1);
        bindMatrix();
        glDrawArrays(GL_TRIANGLE_STRIP,VERTEX_SUM_RING3_COUNT,VERTEX_RING3_COUNT);
    }//Нарисовать кольцо с толщиной 0,1 радиуса

    void drawRingTransfered(float centerX, float centerY, float radius, float orientation, float offsetX, float offsetY){
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix,0,centerX,centerY,0);
        Matrix.scaleM(mModelMatrix,0,radius,radius,1);
        Matrix.rotateM(mModelMatrix,0,orientation,0,0,1);
        Matrix.translateM(mModelMatrix,0,offsetX/radius,offsetY/radius,0);
        bindMatrix();
        glDrawArrays(GL_TRIANGLE_STRIP,VERTEX_SUM_RING_COUNT,VERTEX_RING_COUNT);
    }//Нарисовать кольцо с толщиной 0,1886792452830189 радиуса
    void drawRing2Transfered(float centerX, float centerY, float radius, float orientation, float offsetX, float offsetY){
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix,0,centerX,centerY,0);
        Matrix.scaleM(mModelMatrix,0,radius,radius,1);
        Matrix.rotateM(mModelMatrix,0,orientation,0,0,1);
        Matrix.translateM(mModelMatrix,0,offsetX/radius,offsetY/radius,0);
        bindMatrix();
        glDrawArrays(GL_TRIANGLE_STRIP,VERTEX_SUM_RING2_COUNT,VERTEX_RING2_COUNT);
    }//Нарисовать кольцо с толщиной 0,1886792452830189 радиуса
    void drawRing3Transfered(float centerX, float centerY, float radius, float orientation, float offsetX, float offsetY){
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix,0,centerX,centerY,0);
        Matrix.scaleM(mModelMatrix,0,radius,radius,1);
        Matrix.rotateM(mModelMatrix,0,orientation,0,0,1);
        Matrix.translateM(mModelMatrix,0,offsetX/radius,offsetY/radius,0);
        bindMatrix();
        glDrawArrays(GL_TRIANGLE_STRIP,VERTEX_SUM_RING3_COUNT,VERTEX_RING3_COUNT);
    }//Нарисовать кольцо с толщиной 0,1886792452830189 радиуса

    void drawPlus(float centerX, float centerY, float sizeScale, float orientation){
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix,0,centerX,centerY,0);
        Matrix.scaleM(mModelMatrix,0,sizeScale,sizeScale,1);
        Matrix.rotateM(mModelMatrix,0,orientation,0,0,1);
        bindMatrix();
        glDrawArrays(GL_TRIANGLE_FAN,VERTEX_SUM_PLUS_COUNT,VERTEX_PLUS_COUNT);
    }
    void drawEllipse(float centerX, float centerY, float sizeScale, float orientation) {
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix,0,centerX,centerY,0);
        Matrix.scaleM(mModelMatrix,0,sizeScale,sizeScale,1);
        Matrix.rotateM(mModelMatrix,0,orientation,0,0,1);
        bindMatrix();
        glDrawArrays(GL_TRIANGLE_STRIP,VERTEX_SUM_ELLIPSE_COUNT,VERTEX_ELLIPSE_COUNT);
    }
    void drawBezier(float centerX, float centerY, float sizeScale, float orientation, float multiplSnake) {
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix,0,centerX,centerY,0);
        Matrix.scaleM(mModelMatrix,0,sizeScale,sizeScale,1);
        Matrix.rotateM(mModelMatrix,0,orientation,0,0,1);
        bindMatrix();
        glDrawArrays(GL_TRIANGLE_STRIP,VERTEX_SUM_BEZIER_COUNT+VERTEX_BEZIER_COUNT*Math.round(multiplSnake*(ANIMATION_FRAMES-1)),VERTEX_BEZIER_COUNT);
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix,0,centerX,centerY,0);
        Matrix.scaleM(mModelMatrix,0,sizeScale,-sizeScale,1);
        Matrix.rotateM(mModelMatrix,0,-orientation,0,0,1);
        bindMatrix();
        glDrawArrays(GL_TRIANGLE_STRIP,VERTEX_SUM_BEZIER_COUNT+VERTEX_BEZIER_COUNT*Math.round(multiplSnake*(ANIMATION_FRAMES-1)),VERTEX_BEZIER_COUNT);
    }//Для еды типа 0 (по бокам)
    void drawBezier2(float centerX, float centerY, float sizeScale, float orientation, float multiplSnake) {
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix,0,centerX,centerY,0);
        Matrix.scaleM(mModelMatrix,0,sizeScale,sizeScale,1);
        Matrix.rotateM(mModelMatrix,0,orientation+90,0,0,1);
        bindMatrix();
        glDrawArrays(GL_TRIANGLE_STRIP,VERTEX_SUM_BEZIER_COUNT+VERTEX_BEZIER_COUNT*Math.round(multiplSnake*(ANIMATION_FRAMES-1)),VERTEX_BEZIER_COUNT);
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix,0,centerX,centerY,0);
        Matrix.scaleM(mModelMatrix,0,sizeScale,-sizeScale,1);
        Matrix.rotateM(mModelMatrix,0,-orientation+90,0,0,1);
        bindMatrix();
        glDrawArrays(GL_TRIANGLE_STRIP,VERTEX_SUM_BEZIER_COUNT+VERTEX_BEZIER_COUNT*Math.round(multiplSnake*(ANIMATION_FRAMES-1)),VERTEX_BEZIER_COUNT);
    }//Для еды типа 3 (сзади два)
    void drawBezier3(float centerX, float centerY, float sizeScale, float orientation, float multiplSnake) {
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix,0,centerX,centerY,0);
        if (multiplSnake>=0.5){
            Matrix.scaleM(mModelMatrix,0,sizeScale,sizeScale,1);
            Matrix.rotateM(mModelMatrix,0,orientation+90,0,0,1);
            Matrix.translateM(mModelMatrix,0,-1,1,0);//Состыковка
            bindMatrix();
            glDrawArrays(GL_TRIANGLE_STRIP,VERTEX_SUM_BEZIER_COUNT+VERTEX_BEZIER_COUNT*Math.round((multiplSnake-0.5f)*2*(ANIMATION_FRAMES-1)),VERTEX_BEZIER_COUNT);
        }
        else{
            Matrix.scaleM(mModelMatrix,0,sizeScale,-sizeScale,1);
            Matrix.rotateM(mModelMatrix,0,-orientation+90,0,0,1);
            Matrix.translateM(mModelMatrix,0,-1,1,0);
            bindMatrix();
            glDrawArrays(GL_TRIANGLE_STRIP,VERTEX_SUM_BEZIER_COUNT+VERTEX_BEZIER_COUNT*Math.round((1-multiplSnake*2)*(ANIMATION_FRAMES-1)),VERTEX_BEZIER_COUNT);
        }//Зеркалим
    }//Для еды типа 4 (сзади один)
    void drawBezier4(float centerX, float centerY, float sizeScale, float orientation, float multiplSnake) {
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix,0,centerX,centerY,0);
        Matrix.scaleM(mModelMatrix,0,sizeScale,sizeScale,1);
        Matrix.rotateM(mModelMatrix,0,orientation+90,0,0,1);
        Matrix.translateM(mModelMatrix,0,-5,0,0);
        bindMatrix();
        glDrawArrays(GL_TRIANGLE_STRIP,VERTEX_SUM_BEZIER_COUNT+VERTEX_BEZIER_COUNT*Math.round(multiplSnake*(ANIMATION_FRAMES-1)),VERTEX_BEZIER_COUNT);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix,0,centerX,centerY,0);
        Matrix.scaleM(mModelMatrix,0,sizeScale,-sizeScale,1);


        Matrix.rotateM(mModelMatrix,0,-orientation+90,0,0,1);
        Matrix.translateM(mModelMatrix,0,-5,0,0);
        bindMatrix();
        glDrawArrays(GL_TRIANGLE_STRIP,VERTEX_SUM_BEZIER_COUNT+VERTEX_BEZIER_COUNT*Math.round(multiplSnake*(ANIMATION_FRAMES-1)),VERTEX_BEZIER_COUNT);
    }//Для еды типа 6 (сзади два, чуть поодаль друг от друга)
    void drawBezier5(float centerX, float centerY, float sizeScale, float orientation, float multiplSnake, float changeLevelFoodRadius) {
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix,0,centerX,centerY,0);
        if (multiplSnake>=0.5){
            Matrix.scaleM(mModelMatrix,0,sizeScale,sizeScale,1);

            Matrix.rotateM(mModelMatrix,0,orientation+90,0,0,1);

            Matrix.translateM(mModelMatrix,0,0,changeLevelFoodRadius,0);
            Matrix.translateM(mModelMatrix,0,-1,1,0);//Состыковка
            bindMatrix();
            glDrawArrays(GL_TRIANGLE_STRIP,VERTEX_SUM_BEZIER_COUNT+VERTEX_BEZIER_COUNT*Math.round((multiplSnake-0.5f)*2*(ANIMATION_FRAMES-1)),VERTEX_BEZIER_COUNT);
        }
        else{
            Matrix.scaleM(mModelMatrix,0,sizeScale,-sizeScale,1);


            Matrix.rotateM(mModelMatrix,0,-orientation+90,0,0,1);
            Matrix.translateM(mModelMatrix,0,0,changeLevelFoodRadius,0);//Состыковка
            Matrix.translateM(mModelMatrix,0,-1,1,0);
            bindMatrix();
            glDrawArrays(GL_TRIANGLE_STRIP,VERTEX_SUM_BEZIER_COUNT+VERTEX_BEZIER_COUNT*Math.round((1-multiplSnake*2)*(ANIMATION_FRAMES-1)),VERTEX_BEZIER_COUNT);
        }//Зеркалим
    }//Для смены уровня (почти как ля еды типа 4 (сзади один))
    public void drawHalfRing(float centerX, float centerY, float radius){
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix,0,centerX,centerY,0);
        Matrix.scaleM(mModelMatrix,0,radius,radius,1);
        bindMatrix();
        glDrawArrays(GL_TRIANGLE_STRIP,VERTEX_SUM_HALF_RING_COUNT,VERTEX_HALF_RING_COUNT);
    }//Нарисовать полукруг (верхняя часть)
    void drawHalfRings(float centerX, float centerY, float radius, float orientation, float rotationForEating){
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix,0,centerX,centerY,0);
        Matrix.rotateM(mModelMatrix,0,orientation,0,0,1);
        Matrix.scaleM(mModelMatrix,0,radius,radius,1);
        Matrix.translateM(mModelMatrix,0,-1.0943396226415094f+(float)Math.cos(rotationForEating/180*Math.PI),(float)Math.sin(rotationForEating/180*Math.PI),0);
        Matrix.rotateM(mModelMatrix,0,rotationForEating,0,0,1);
        Matrix.translateM(mModelMatrix,0,0.0943396226415094f,0,0);//Сдвиг в исходной системе из-за рамки
        bindMatrix();
        glDrawArrays(GL_TRIANGLE_STRIP,VERTEX_SUM_HALF_RING_COUNT,VERTEX_HALF_RING_COUNT);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix,0,centerX,centerY,0);
        Matrix.rotateM(mModelMatrix,0,orientation,0,0,1);
        Matrix.scaleM(mModelMatrix,0,radius,radius,1);
        Matrix.translateM(mModelMatrix,0,-1.0943396226415094f+(float)Math.cos(rotationForEating/180*Math.PI),(float)Math.sin(-rotationForEating/180*Math.PI),0);
        Matrix.rotateM(mModelMatrix,0,-rotationForEating,0,0,1);//Тут и выше поворачиваем на "минусовой" угол
        Matrix.scaleM(mModelMatrix,0,1,-1,1);//Зеркалим нижнюю челюсть
        Matrix.translateM(mModelMatrix,0,0.0943396226415094f,0,0);//Сдвиг в исходной системе из-за рамки
        bindMatrix();
        glDrawArrays(GL_TRIANGLE_STRIP,VERTEX_SUM_HALF_RING_COUNT,VERTEX_HALF_RING_COUNT);
    }//Нарисовать два полукруга как открытый рот
    void drawMouth(float centerX, float centerY, float orientation, float multiplSnake){
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix,0,centerX,centerY,0);
        Matrix.rotateM(mModelMatrix,0,orientation,0,0,1);
        bindMatrix();
        glDrawArrays(GL_TRIANGLE_STRIP,VERTEX_SUM_MOUTH_COUNT+VERTEX_MOUTH_COUNT*Math.round(multiplSnake*(ANIMATION_FRAMES-1)),VERTEX_MOUTH_COUNT);
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix,0,centerX,centerY,0);
        Matrix.rotateM(mModelMatrix,0,orientation,0,0,1);

        Matrix.scaleM(mModelMatrix,0,1,-1,1);
        bindMatrix();
        glDrawArrays(GL_TRIANGLE_STRIP,VERTEX_SUM_MOUTH_COUNT+VERTEX_MOUTH_COUNT*Math.round(multiplSnake*(ANIMATION_FRAMES-1)),VERTEX_MOUTH_COUNT);
    }//Рот ГГ

    void drawSharkBody(float centerX, float centerY, float orientation, float multiplSnake, float scaleForLittleOrBigFish){
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix,0,centerX,centerY,0);
        Matrix.rotateM(mModelMatrix,0,orientation,0,0,1);
        Matrix.scaleM(mModelMatrix,0,scaleForLittleOrBigFish,scaleForLittleOrBigFish,1);//bigfishornot
        bindMatrix();
        glDrawArrays(GL_TRIANGLES,VERTEX_SUM_SHARKBODY_COUNT+VERTEX_SHARKBODY_COUNT*Math.round(multiplSnake*(ANIMATION_FRAMES-1)),VERTEX_SHARKBODY_COUNT);//16+3
    }

    void drawRoundedTriangleInCenter(float centerX, float centerY, float sizeScale, float orientation){
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix,0,centerX,centerY,0);
        Matrix.scaleM(mModelMatrix,0,sizeScale,sizeScale,1);

        Matrix.rotateM(mModelMatrix,0,orientation,0,0,1);
        bindMatrix();
        glDrawArrays(GL_TRIANGLE_STRIP, VERTEX_SUM_ROUNDED_TRIANGLE_INCENTER_COUNT, VERTEX_ROUNDED_TRIANGLE_INCENTER_COUNT);
    }
    void drawRoundedTriangleOutCenter(float centerX, float centerY, float sizeScale, float orientation){
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix,0,centerX,centerY,0);
        Matrix.scaleM(mModelMatrix,0,sizeScale,sizeScale,1);

        Matrix.rotateM(mModelMatrix,0,orientation,0,0,1);
        bindMatrix();
        glDrawArrays(GL_TRIANGLE_STRIP, VERTEX_SUM_ROUNDED_TRIANGLE_OUTCENTER_COUNT, VERTEX_ROUNDED_TRIANGLE_OUTCENTER_COUNT);
    }

    void drawRoundedTriangleInCenterTransfered(float centerX, float centerY, float sizeScale, float orientation){
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix,0,centerX,centerY,0);
        Matrix.scaleM(mModelMatrix,0,sizeScale,sizeScale,1);

        Matrix.rotateM(mModelMatrix,0,orientation,0,0,1);
        Matrix.translateM(mModelMatrix,0,2,0,0);
        bindMatrix();
        glDrawArrays(GL_TRIANGLE_STRIP, VERTEX_SUM_ROUNDED_TRIANGLE_INCENTER_COUNT, VERTEX_ROUNDED_TRIANGLE_INCENTER_COUNT);
    }

    void drawRoundedTriangleOutCenterTransfered(float centerX, float centerY, float sizeScale, float orientation){
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix,0,centerX,centerY,0);
        Matrix.scaleM(mModelMatrix,0,sizeScale,sizeScale,1);

        Matrix.rotateM(mModelMatrix,0,orientation,0,0,1);
        Matrix.translateM(mModelMatrix,0,0.538f,0,0);
        bindMatrix();
        glDrawArrays(GL_TRIANGLE_STRIP, VERTEX_SUM_ROUNDED_TRIANGLE_OUTCENTER_COUNT, VERTEX_ROUNDED_TRIANGLE_OUTCENTER_COUNT);
    }



    void drawSharkmouthTransfered(float centerX, float centerY, float sizeScale, float orientation, float offsetX, float offsetY, float rotationForEating) {
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, centerX, centerY, 0);
        Matrix.scaleM(mModelMatrix, 0, sizeScale, sizeScale, 1);
        Matrix.rotateM(mModelMatrix, 0, orientation, 0, 0, 1);
        Matrix.translateM(mModelMatrix,0,offsetX/sizeScale,offsetY/sizeScale,0);
        Matrix.rotateM(mModelMatrix, 0, rotationForEating, 0, 0, 1);
        bindMatrix();
        glDrawArrays(GL_TRIANGLES, VERTEX_SUM_SHARKMOUTH_COUNT, VERTEX_SHARKMOUTH_COUNT);
    }

    void drawParticle(float centerX, float centerY){
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix,0,centerX,centerY,0);
        Matrix.scaleM(mModelMatrix,0,2.5f,2.5f,1);
        bindMatrix();
        glDrawArrays(GL_TRIANGLES, VERTEX_SUM_PARTICLE_COUNT, VERTEX_PARTICLE_COUNT);
    }

    void drawBossBody(float centerX, float centerY, float orientation, float multiplSnake, float scaleForLittleOrBigFish){
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix,0,centerX,centerY,0);
        Matrix.rotateM(mModelMatrix,0,orientation,0,0,1);
        Matrix.scaleM(mModelMatrix,0,-scaleForLittleOrBigFish,scaleForLittleOrBigFish,1);//bigfishornot
        bindMatrix();
        glDrawArrays(GL_TRIANGLES,VERTEX_SUM_BOSS1_COUNT+VERTEX_BOSS1_COUNT*Math.round(multiplSnake*(ANIMATION_FRAMES-1)),VERTEX_BOSS1_COUNT);//16+3
    }

    void setColor(int color){
        switch (color) {
            case 0://Standart white
                glUniform4f(uColorLocation, 1.0f, 1.0f, 1.0f, 0.47f);
                break;
            case 1://Ghostly white
                glUniform4f(uColorLocation, 1.0f, 1.0f, 1.0f, 0.23f);
                break;
            case 5://Cyan
                glUniform4f(uColorLocation, 0.0f, 1.0f, 1.0f, 1.0f);
                break;
            case 6://Red
                glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 1.0f);
                break;
            case 7://Ghostly red
                glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 0.7f);
                break;
            default:
                Log.wtf(LOG_TAG, "Цвет задан неверно");
                break;
        }
    }
    void setAlphaWhite(float alpha){
        glUniform4f(uColorLocation, 1.0f, 1.0f, 1.0f, alpha);
    }
    void setAlphaCyan(float alpha){
        glUniform4f(uColorLocation, 0.0f, 1.0f, 1.0f, alpha);
    }
    void setAlphaRed(float alpha){
        glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, alpha);
    }


    private void sharkBodyBezier1(float [] whatIshouldReturn, int side, float percent, float animationStatus, float x0, float y0, float x1, float y1, float x2, float y2, float deltaX){
        if (side%2==0)
        {
            whatIshouldReturn[0]= (x0*(1-percent)*(1-percent)+2*percent*(1-percent)*x1+percent*percent*x2);
            whatIshouldReturn[1]=(y0*(1-percent)*(1-percent)+2*percent*(1-percent)*y1+percent*percent*y2);
        }
        else{//Дополнительная кривая Безье для толщины
            whatIshouldReturn[0]= ((x0+3*deltaX)*(1-percent)*(1-percent)+2*percent*(1-percent)*(x1-4*deltaX)+percent*percent*(x2-12*deltaX));
            whatIshouldReturn[1]=((y0+10*deltaX)*(1-percent)*(1-percent)+2*percent*(1-percent)*(y1+8*deltaX)+percent*percent*(y2));
        }
    }//Передняя часть акулки
    private void sharkBodyBezier2(float [] whatIshouldReturn, int side, float percent, float animationStatus, float x0, float y0, float x1, float y1, float x2, float y2, float deltaX){
        if (side%2==0)//Чётные числа для внешего радиуса
        {
            whatIshouldReturn[0]=(x0*(1-percent)*(1-percent)+2*percent*(1-percent)*x1+percent*percent*x2);
            whatIshouldReturn[1]=(y0*(1-percent)*(1-percent)+2*percent*(1-percent)*y1+percent*percent*y2);
        }
        else{//Дополнительная кривая Безье для толщины
            whatIshouldReturn[0]=((x0+3*deltaX)*(1-percent)*(1-percent)+2*percent*(1-percent)*(x1+12*deltaX)+percent*percent*(x2+20*deltaX*(2+animationStatus*1f)));
            whatIshouldReturn[1]=((y0+10*deltaX)*(1-percent)*(1-percent)+2*percent*(1-percent)*(y1+12*deltaX)+percent*percent*(y2));
        }
    }//Хвост акулки
    private void boss1BodyBezier1(float [] whatIshouldReturn, int side, float percent, float animationStatus, float x0, float y0, float x1, float y1, float x2, float y2, float deltaX){
        if (side%2==0)
        {
            whatIshouldReturn[0]= (x0*(1-percent)*(1-percent)+2*percent*(1-percent)*x1+percent*percent*x2);
            whatIshouldReturn[1]=(y0*(1-percent)*(1-percent)+2*percent*(1-percent)*y1+percent*percent*y2);
        }
        else{//Дополнительная кривая Безье для толщины
            whatIshouldReturn[0]= ((x0+3*deltaX)*(1-percent)*(1-percent)+2*percent*(1-percent)*(x1-4*deltaX)+percent*percent*(x2-10*deltaX));
            whatIshouldReturn[1]=((y0+10*deltaX)*(1-percent)*(1-percent)+2*percent*(1-percent)*(y1+8*deltaX)+percent*percent*(y2));
        }
    }//Передняя часть акулки
    private void boss1BodyBezier2(float [] whatIshouldReturn, int side, float percent, float animationStatus, float x0, float y0, float x1, float y1, float x2, float y2, float deltaX){
        if (side%2==0)//Чётные числа для внешего радиуса
        {
            whatIshouldReturn[0]=(x0*(1-percent)*(1-percent)+2*percent*(1-percent)*x1+percent*percent*x2);
            whatIshouldReturn[1]=(y0*(1-percent)*(1-percent)+2*percent*(1-percent)*y1+percent*percent*y2);
        }
        else{//Дополнительная кривая Безье для толщины
            whatIshouldReturn[0]=((x0+3*deltaX)*(1-percent)*(1-percent)+2*percent*(1-percent)*(x1)+percent*percent*(x2+13*deltaX*(1.5f+animationStatus*0.5f)));
            whatIshouldReturn[1]=((y0+10*deltaX)*(1-percent)*(1-percent)+2*percent*(1-percent)*(y1+4*deltaX)+percent*percent*(y2));
        }
    }//Хвост акулки
}
