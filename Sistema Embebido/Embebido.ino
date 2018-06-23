#include <Wire.h>  
#include <LiquidCrystal_I2C.h>
#include <OneWire.h>
#include <DallasTemperature.h>
 
/*SENSOR DE TEMPERATURA */
const int pinDatosDQ = 9;
OneWire oneWireObjeto(pinDatosDQ);
DallasTemperature sensorDS18B20(&oneWireObjeto);
LiquidCrystal_I2C lcd(0x27, 2, 1, 0, 4, 5, 6, 7, 3, POSITIVE);

/*SENSOR MAGNETICO CIERRE TAPA, VARIABLES  */
int SensorReed = LOW;

/* PINES DIGITALES */
const int PinEcho = 11;
const int PinTrigger = 12;
const int PinLed = 13;
const int PinLedPWM = 3;

/*SENSOR DE DISTANCIA, VARIABLES */
const int AlturaVaso = 13.5; //Desde la base hasta el sensor
const int Hmax = 8.7; //Desde base hasta ultima indicacion
unsigned int tiempo;
float diametro, area, radio, volumen,distancia_sensor_liquido,altura_liquido;

/*VARIABLES MQ3*/
float Peso = 75;


float volumenLiquido()
{
  digitalWrite(PinTrigger, LOW);
  delayMicroseconds(2);
  digitalWrite(PinTrigger, HIGH);
  // EL PULSO DURA AL MENOS 10 uS EN ESTADO ALTO
  delayMicroseconds(10);
  digitalWrite(PinTrigger, LOW);
 
  // MEDIR EL TIEMPO EN ESTADO ALTO DEL PIN "ECHO" EL PULSO ES PROPORCIONAL A LA DISTANCIA MEDIDA
  tiempo = pulseIn(PinEcho, HIGH);
 
  // LA VELOCIDAD DEL SONIDO ES DE 340 M/S O 29 MICROSEGUNDOS POR CENTIMETRO
  // DIVIDIMOS EL TIEMPO DEL PULSO ENTRE 58, TIEMPO QUE TARDA RECORRER IDA Y VUELTA UN CENTIMETRO LA ONDA SONORA
  distancia_sensor_liquido = tiempo / 58.001;

  //distancia es el espacio vacio
  // volumen= area*h
  altura_liquido = AlturaVaso - distancia_sensor_liquido; //altura lleno
  volumen = area * altura_liquido;
  
  return volumen;
}

float volumenAlcohol()
{
     float SumaValor = 0;
        
      for (int i = 0; i < 10; i ++)
      {
        float ValorSensor = analogRead(A0); //Lemos la salida analógica  del MQ
        SumaValor += ValorSensor;
        delay(200);
      }
      
      float x = (SumaValor/10);
      float VolumenAlcohol = (analogRead(A0) - 80) * (0.277777777777);
      
      return VolumenAlcohol;
}


void setup()
{
  Serial.begin(9600);
  
  
  /*CONFIGURAR PINES DE ENTRADA Y SALIDA*/
  pinMode(PinEcho, INPUT);
  pinMode(PinTrigger, OUTPUT);
  pinMode(PinLed, OUTPUT);
  pinMode(PinLedPWM, OUTPUT);
  
  /*INICIALIZACION SENSOR DE TEMPERATURA */
  sensorDS18B20.begin(); 
  
  /*INICIALIZACION SENSOR DISTANCIA */
  diametro = 7.5;
  radio = diametro / 2;
  area = 3.1415 * radio * radio; // pi x radio al cuadrado
   
   
  /*INICIALIZACION DISPLAY*/
  lcd.clear();
  lcd.begin(16,2); // 2 filas y 16 columnas
  lcd.print("Vaso Inteligente");
  delay(3000);
  lcd.clear();
  analogWrite(PinLedPWM,0);
}

float temperaturaLiquido()
{
   sensorDS18B20.requestTemperatures();
   return sensorDS18B20.getTempCByIndex(0);
}

void loop() 
{
  
   /*PIN ENTRADA SWITCH ENCENDIDO*/
  pinMode(2, INPUT);
  SensorReed = digitalRead(2);
  lcd.clear();
  
  if (SensorReed == HIGH)
  {
     
        float volLiquido = volumenLiquido();
        float volAlcohol = volumenAlcohol();
        float temPLiquido = temperaturaLiquido();
        
        float grAlcohol = (volLiquido * volAlcohol * 0.79) / 100;
        
        float gradHombre = grAlcohol / (Peso * 0.7);
        float gradMujer = grAlcohol / (Peso * 0.6);
        float porcTemperatura = (temPLiquido + 55) * (0.55555555555555555) ;
        int pwmLevel = (int)((porcTemperatura) * (2.56));
        
        Serial.println("Medicion: " + String(analogRead(A0)));
        Serial.println("volLiquido: " + String(volLiquido));
        Serial.println("volAlcohol: " + String(volAlcohol));
        Serial.println("temperaturaLiquido: " + String(temPLiquido));
        Serial.println("pwmLevel: " + String(pwmLevel));
        
        
        lcd.print("GHombre:" + String(gradHombre));
        delay(2000);
        lcd.clear();
        lcd.print("GMujer:" + String(gradMujer));
        delay(2000);
        lcd.clear();
        lcd.print("Temperatura :" + String(temPLiquido));
        analogWrite(PinLedPWM,pwmLevel);
        delay(2000);
        lcd.clear();
  }
  else
  {
    lcd.print("Coloque la Tapa");
    delay(100);
  }
  
  /*
  if (
  Encendido = digitalRead(2);
  
  lcd.clear();
  lcd.print("  Presione para");
  lcd.setCursor(0,1);
  lcd.print("      Medir");
  
  if (Encendido == HIGH)
  {

      lcd.clear();
      lcd.print("  Calibrando...");
      
      Estable = false;
      
      while (Estable == false)
      {
        ADCMQ = analogRead(A0);
        Serial.println(ADCMQ);
        
        if (ADCMQ <= NivelEstableMQ3)
        {
          Estable = true;
        }
      }
      
      lcd.clear();
      lcd.print("Coloque la tapa");
      delay(10000);
      
      
      lcd.clear();
      lcd.print("  Midiendo...");
        
    
      for (int i = 0; i < 10; i ++)
      {
        int ADCMQ = analogRead(A0); //Lemos la salida analógica  del MQ
        float Voltaje = (ADCMQ - NivelEstableMQ3) * (5.0 / 1023.0); //Convertimos la lectura en un valor de voltaje
        float Rs=1000*((5-Voltaje)/Voltaje);  //Calculamos Rs con un RL de 1k
        double Concentracion=0.4091*pow(Rs/5463, -1.497); // calculamos la concentración  de alcohol con la ecuación obtenida.
        SumaConcentracion += Concentracion;
        delay(1000);
      }
      
      
      lcd.clear();
      lcd.print("Concentr:" + String(SumaConcentracion/10));
      delay(10000);
      */
    
  }


