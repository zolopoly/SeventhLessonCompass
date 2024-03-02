package com.msaggik.seventhlessoncompass;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    // поля
    private TextView output;
    private ImageView imageView;
    private SensorManager sensorManager; // менеджер сенсоров устройства
    private Sensor sensorAccelerometer, sensorMagneticField; // сенсоры акселерометр и магнитометр

    // контейнеры для генерируемых данных
    private final float[] accelerometerData = new float[3]; // массив для данных акселерометра по трём осям
    private final float[] magnetometerData = new float[3]; // массив для данных магнетометра по трём осям

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // привязка разметки к полям
        output = findViewById(R.id.output);
        imageView = findViewById(R.id.imageView);

        // инициализация сенсор менеджера (получение доступа к управлению сенсорами)
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // инициализация сенсоров
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }
    // создание слушателя для сенсоров
    private SensorEventListener sensorEventListener = new SensorEventListener() {

        // обработчик события (вызывается всякий раз при измерении показаний сенсора)
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            // получаем мультиссылку на сенсоры
            Sensor multiSensor = sensorEvent.sensor;

            switch (multiSensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    // считывание данных акселерометра (запись контейнера accelerometerData)
                    System.arraycopy(sensorEvent.values, 0, accelerometerData, 0, accelerometerData.length);
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    // считывание данных магнетометра (запись контейнера magnetometerData)
                    System.arraycopy(sensorEvent.values, 0, magnetometerData, 0, magnetometerData.length);
                    break;
            }

            // обработка данных акселерометра и магнитометра
            float degreeNorth = sensorDataToDegree(accelerometerData, magnetometerData);

            // вывод полученных данных на экран смартфона
            output.setText("Поворот смартфона относительно севера " + String.format("%.2f",degreeNorth));
            // поворот картинки компаса
            imageView.setRotation(-degreeNorth);
        }

        // метод задания точности сенсора
        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        // регистрация слушателя для сенсоров sensorAccelerometer и sensorMagneticField
        sensorManager.registerListener(sensorEventListener, sensorAccelerometer, SensorManager.SENSOR_DELAY_UI); // (слушатель, сенсор, время обновления - среднее)
        sensorManager.registerListener(sensorEventListener, sensorMagneticField, SensorManager.SENSOR_DELAY_UI); // (слушатель, сенсор, время обновления - среднее)
    }

    @Override
    protected void onPause() {
        super.onPause();
        // отзыв регистрации сенсоров (отключение слушателя)
        sensorManager.unregisterListener(sensorEventListener);
    }

    // метод перевода данных массивов magnetometer и accelerometer в угол поворота картинки компаса
    private float sensorDataToDegree(float[] accelerometer, float[] magnetometer) {
        float[] rotationMatrix = new float[9]; // матрица сгенерированных данных на основе данных акселерометра и магнитометра
        // запись контейнера rotationMatrix
        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometer, magnetometer);
        float[] radians = new float[3]; // матрица радиан поворота смартфона относительно магнитного поля Земли
        // запись контейнера radians
        SensorManager.getOrientation(rotationMatrix, radians);
        // вывод угла поворота компаса (с конвертацией из радиан в градусы)
        return (float) (Math.toDegrees(radians[0]));
    }
}