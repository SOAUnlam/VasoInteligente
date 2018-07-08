#include <Wire.h>  
#include <LiquidCrystal_I2C.h>
#include <OneWire.h>
#include <DallasTemperature.h>
#include <SoftwareSerial.h> 
 
SoftwareSerial ModBluetooth(4, 5); // RX | TX 
 
/*SENSOR DE TEMPERATURA */
const int pinDatosDQ = 9;
OneWire oneWireObjeto(pinDatosDQ);
DallasTemperature sensorDS18B20(&oneWireObjeto);
LiquidCrystal_I2C lcd(0x27, 2, 1, 0, 4, 5, 6, 7, 3, POSITIVE);

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

/*FUNCION PARA SEPARAR VALORES RECIBIDOS DE ANDROID*/
String getStringDelimitar(String data, char separator, int index)
{
  int found = 0;
  int strIndex[] = {0, -1};
  int maxIndex = data.length()-1;

  for(int i=0; i<=maxIndex && found<=index; i++){
    if(data.charAt(i)==separator || i==maxIndex){
        found++;
        strIndex[0] = strIndex[1]+1;
        strIndex[1] = (i == maxIndex) ? i+1 : i;
    }
  }

  return found>index ? data.substring(strIndex[0], strIndex[1]) : "";
}


/*FUNCION PARA CALCULAR EL VOLUMEN DEL RECIPIENTE*/
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

/* FUNCION PARA CALCULAR EL VOLUMEN DE ALCOHOL */
float volumenAlcohol()
{
     float SumaValor = 0;
        
      for (int i = 0; i < 10; i ++)
      {
        float ValorSensor = analogRead(A0); //Lemos la salida analógica  del MQ
        SumaValor += ValorSensor;
        Serial.print(ValorSensor);
        delay(200);
      }

      float x = (SumaValor/10);
      float VolumenAlcohol = (analogRead(A0) - 80) * (0.277777777777);
      
      return VolumenAlcohol;
}
/*FUNCION PARA EL CALCULO DE TEMPERATURA*/
float temperaturaLiquido()
{
   sensorDS18B20.requestTemperatures();
   return sensorDS18B20.getTempCByIndex(0);
}



void setup()
{
  Serial.begin(9600);
  
  /* INICIO EL MODULO BLUETOOTH*/
  ModBluetooth.begin(9600); 
     
    
  /*CONFIGURAR PINES DE ENTRADA Y SALIDA*/
  pinMode(PinEcho, INPUT);
  pinMode(PinTrigger, OUTPUT);
  pinMode(PinLed, OUTPUT);
  pinMode(PinLedPWM, OUTPUT);
  
  pinMode(2, OUTPUT);
  
  /*INICIALIZACION SENSOR DE TEMPERATURA */
  sensorDS18B20.begin(); 
  
  /*INICIALIZACION SENSOR DISTANCIA */
  diametro = 7.5;
  radio = diametro / 2;
  area = 3.1415 * radio * radio; // pi x radio al cuadrado
   
   
  /*INICIALIZACION DISPLAY*/
  lcd.clear();
  lcd.begin(16,2); 
  lcd.print("Vaso Inteligente");
  delay(3000);
  lcd.clear();
  analogWrite(PinLedPWM,0);
}


void loop() 
{
  
       
      lcd.clear();
      
      /* INICIALIZACION DEL LED DE TEMPERATURA */
      analogWrite(PinLedPWM,0);
      
      /*LECTURA PARAMETROS BLUETOOTH*/
      String Parametros  = ""; 
      while (ModBluetooth.available())
      {
        Parametros += (char)ModBluetooth.read();
      }
      
      /*SI SE RECIBE ALGO POR EL MODULO BLUETOOTH */
      if (getStringDelimitar(Parametros,';',0) != "")
      {
           /*VERIFICO SI RECIBO DE ALGUN SENSOR DE ANDROID, SI ES ASI MUESTRO EL NOMBRE DEL SENSOR */
           String sensoresSend = getStringDelimitar(Parametros,';',0);
           if (sensoresSend == "SENSOR")  
           { 
                  lcd.clear();
                  lcd.setCursor(0, 0);
                  lcd.print(getStringDelimitar(Parametros,';',1));
                  delay(4000);
           }
           else
           {
                  /* CASO CONTRARIO LEO LOS SENSORES PARA CALCULAR LA GRADUACION */
                  String Sexo = getStringDelimitar(Parametros,';',0);
                  float Peso = getStringDelimitar(Parametros,';',1).toFloat();
                   
                  /* CALCULO VOLUMEN LIQUIDO, ALCOHOL Y TEMPERATURA */
                  float volLiquido = volumenLiquido();
                  float volAlcohol = volumenAlcohol();
                  float temPLiquido = temperaturaLiquido();
                  
                  /* CALCULO GRAMOS DE ALCOHOL */
                  float grAlcohol = (volLiquido * volAlcohol * 0.79) / 100;
                  
                  /* CALCULO GRADUACION DE ALCOHOL */
                  float gradHombre = grAlcohol / (Peso * 0.7);
                  float gradMujer = grAlcohol / (Peso * 0.6);
                  
                  /* PORCENTAJE DE TEMPERATURA Y NIVEL PWM */
                  float porcTemperatura = (temPLiquido + 55) * (0.55555555555555555) ;
                  int pwmLevel = (int)((porcTemperatura) * (2.55));
                  
                  Serial.println("Medicion: " + String(analogRead(A0)));
                  Serial.println("volLiquido: " + String(volLiquido));
                  Serial.println("volAlcohol: " + String(volAlcohol));
                  Serial.println("temperaturaLiquido: " + String(temPLiquido));
                  Serial.println("pwmLevel: " + String(pwmLevel));
                  Serial.println("Peso: " + String(Peso));
                  Serial.println("Sexo: " + String(Sexo));
                  
                  
                  /* DISPLAY GRADUACION*/
                  lcd.setCursor(0, 0);
                  lcd.print("  Miligramos/L  ");
                  lcd.setCursor(0, 1);
                  
                  if (Sexo == "M")
                  {
                    ModBluetooth.print("RESPUESTA;" + String(gradHombre) + ";" + String(temPLiquido)+ ";" + String(volAlcohol)+ ";" + String(volLiquido)); 
                    lcd.print("     " + String(gradHombre) + "mg     ");
                  }
                  else
                  {
                    ModBluetooth.print("RESPUESTA;" + String(gradMujer) + ";" + String(temPLiquido)+ ";" + String(volAlcohol)+ ";" + String(volLiquido)); 
                    lcd.print("     " + String(gradMujer) + "mg     ");
                  }   
                  
                  delay(4000);
                  lcd.clear();
                  
                  
                  /* DISPLAY TEMPERATURA*/
                  lcd.setCursor(0, 0);
                  lcd.print("  Temperatura  ");
                  lcd.setCursor(0, 1);
                  lcd.print("     " + String(temPLiquido) + " C     ");
                  
                  /*PWM NIVEL DE TEMPERATURA*/
                  analogWrite(PinLedPWM,pwmLevel);
                  delay(4000);
                  lcd.clear();    
          }
          
          String Parametros  = ""; 
      }
      else
      {
       lcd.print("Ingrese valores");
       delay(1000);
      }
  }


