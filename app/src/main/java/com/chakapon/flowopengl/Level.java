package com.chakapon.flowopengl;

import java.util.Random;

public class Level {
    final String LOG_TAG = "Level";
    int foods_arraySize=0, maxFoodArraySize=30;
    Food [] foods_array;
    int changeLevelFood_arraySize;
    ChangeLevelFood [] changeLevelFood_array;


    int color;

    //float screenWidth, screenHeight;

    int snakeHunter_arraySize=0, snakeHunter_numSegments=0, snakeHunter_numSegmEvolved=0;
    SnakeHunter [] snakeHunter_array;

    //float resolutionScale;

    SharkHunter [] sharkHunter_array;int sharkHunter_arraySize=0;

    FlockieBird [] flockieBird_array;
    int flockieBird_arraySize=0;

    Boss [] boss_array;int boss_arraySize=0;int bossType=0;//Если не задастся тип - выдавать ошибку

    Level(int levelNum){
        changeLevelFood_arraySize=2;
        switch (levelNum){
            case (0):
                color=0xFF009EE7;



                changeLevelFood_arraySize=1;//Переход только на уровень ниже

                //sharkHunter_arraySize=30;//3;//Акула
                //snakeHunter_arraySize=2;                snakeHunter_numSegments=10;                snakeHunter_numSegmEvolved=8;
                break;
            case (1):
                color=0xFF008DD8;
                foods_arraySize=15;
                boss_arraySize=1;bossType=1;//foods_arraySize=300;
                break;
            case (2):
                color=0xFF007CC9;
                foods_arraySize=15;

                snakeHunter_arraySize=2;                snakeHunter_numSegments=3;                snakeHunter_numSegmEvolved=1;

                break;
            case (3):
                color=0xFF006DBB;
                foods_arraySize=10;

                snakeHunter_arraySize=1;                snakeHunter_numSegments=8;                snakeHunter_numSegmEvolved=2;

                break;
            case (4):
                color=0xFF0066AD;
                foods_arraySize=10;
                sharkHunter_arraySize=3;//3;//Акула
                break;
            case (5):
                color=0xFF00619E;
                flockieBird_arraySize=1;//                birdType1=0;                Nbirds1=15;

                break;
            case (6)://Boss
                color=0xFF005C8F;
                foods_arraySize=10;
                flockieBird_arraySize=2;
                break;
            case (7):
                color=0xFF005780;
                foods_arraySize=30;
                flockieBird_arraySize=3;
                sharkHunter_arraySize=3;//3;//Акула
                snakeHunter_arraySize=4;                snakeHunter_numSegments=8;                snakeHunter_numSegmEvolved=4;

                changeLevelFood_arraySize=3;//Переход только на уровень выше. Обработать соответствующе
                //М.б. это ночной уровень, где всё чёрное, а вокруг гг свечение, м.б. жёлтое
                break;
        }

        switch (changeLevelFood_arraySize){
            case (0):
                changeLevelFood_array = new ChangeLevelFood[0];
                break;
            case (1):
                changeLevelFood_array = new ChangeLevelFood[1];
                changeLevelFood_array[0]=new ChangeLevelFood(0);
                break;
            case (2):
                changeLevelFood_array = new ChangeLevelFood[2];
                changeLevelFood_array[0]=new ChangeLevelFood(0);
                changeLevelFood_array[1]=new ChangeLevelFood(1);
                break;
            case (3):
                changeLevelFood_arraySize=1;
                changeLevelFood_array = new ChangeLevelFood[1];
                changeLevelFood_array[0]=new ChangeLevelFood(1);
                break;
        }

        maxFoodArraySize = Math.min(foods_arraySize + (snakeHunter_arraySize) * snakeHunter_numSegments + sharkHunter_arraySize * 5 + boss_arraySize*12, 30);//30


        foods_array = new Food[maxFoodArraySize];
        for (int j=0;j<maxFoodArraySize;j++){
            foods_array[j] = new Food();
            if (j>=foods_arraySize){
                foods_array[j].setEaten();
            }
        }

        snakeHunter_array = new SnakeHunter[snakeHunter_arraySize];
        for (int j=0;j<snakeHunter_arraySize;j++){
            snakeHunter_array[j] = new SnakeHunter(snakeHunter_numSegments,snakeHunter_numSegmEvolved);
        }

        sharkHunter_array = new SharkHunter[sharkHunter_arraySize];
        for (int j=0;j<sharkHunter_arraySize;j++){
            sharkHunter_array[j] = new SharkHunter();
        }

        Random random = new Random();
        flockieBird_array = new FlockieBird[flockieBird_arraySize];
        for (int j=0;j<flockieBird_arraySize;j++){
            flockieBird_array[j] = new FlockieBird(10,random.nextInt(3));
        }

        boss_array = new Boss[boss_arraySize];
        for (int j=0;j<boss_arraySize;j++){
            boss_array[j] = new Boss(bossType);
        }
    }

    public int getColor() {
        return color;
    }


    public int updateFoodMapPosition(float dt, Protagonist protagonist){

        for (int i=0;i<maxFoodArraySize;i++){
            foods_array[i].updateMapPosition(dt);
        }//Обновление местоположения еды (мелких)
        for (int i=0;i<changeLevelFood_arraySize;i++){
            changeLevelFood_array[i].updateMapPosition(dt);
        }//Обновление местоположения еды на смену уровня
        for (int i=0;i<snakeHunter_arraySize;i++){
            snakeHunter_array[i].updateMapPosition(dt);
        }//Обновление местоположения змейки
        for (int i=0;i<sharkHunter_arraySize;i++){
            sharkHunter_array[i].updateMapPosition(dt);
        }//Обновление местоположения акулы
        for (int i=0;i<flockieBird_arraySize;i++){
            flockieBird_array[i].updateMapPosition(dt);
        }//Обновление местоположения стайки клеток
        for (int i=0;i<boss_arraySize;i++){
            boss_array[i].updateMapPosition(dt,protagonist);
        }//Обновление местоположения босса

        int changeLevel = protagonist.updateEat(foods_array, snakeHunter_array, sharkHunter_array, changeLevelFood_array, flockieBird_array, boss_array);//Кушает гг
//Добавить поедание босса

        for (int i=0;i<snakeHunter_arraySize;i++){
            snakeHunter_array[i].findNearFood(foods_array);
        }//Кушают змейки

        for (int i=0;i<sharkHunter_arraySize;i++){
            sharkHunter_array[i].findNearFood(foods_array, protagonist);
        }//Кушают акулы

        for (int i=0;i<boss_arraySize;i++){
            boss_array[i].findNearFood(foods_array, protagonist);//Должен ли босс есть мелочь? Наверно, нет, или не все виды
        }//Кушают акулы

        return changeLevel;
    }

    public void draw(OpenGLRenderer openGLRenderer, Camera camera){
        float camCurX=camera.getCurrentX();
        float camCurY=camera.getCurrentY();

        for (int j=0;j<maxFoodArraySize;j++){
            if (!foods_array[j].isEaten) {
                if (Math.abs(foods_array[j].getCurrentX()-camCurX)<camera.getGamefieldHalfX()&&Math.abs(foods_array[j].getCurrentY()-camCurY)<camera.getGamefieldHalfY()) {///////////////////nia , resolutionScale_
                    foods_array[j].draw(openGLRenderer, camera);
                }
            }
        }

        for (int j=0;j<changeLevelFood_arraySize;j++){
            changeLevelFood_array[j].draw(openGLRenderer, camera);
        }

        for (int j=0;j<snakeHunter_arraySize;j++){
            snakeHunter_array[j].draw(openGLRenderer, camera);
        }

        for (int j=0;j<sharkHunter_arraySize;j++) {
            sharkHunter_array[j].draw(openGLRenderer, camera);
        }

        for (int j=0;j<flockieBird_arraySize;j++) {
            flockieBird_array[j].draw(openGLRenderer, camera);
        }

        for (int j=0;j<boss_arraySize;j++) {
            boss_array[j].draw(openGLRenderer, camera);//Отображение всегда (?)
        }


    }

    public void setScreen(float screenWidth, float screenHeight){
        for (int j=0;j<changeLevelFood_arraySize;j++){
            changeLevelFood_array[j].setScreen(screenWidth, screenHeight);
        }

    }
}
