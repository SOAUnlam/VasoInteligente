
void setup() 
{
 Serial.begin(9600);       // use the serial port
}

void loop() {
    double SumaConcentracion = 0;
    for (int i = 0; i < 10; i ++)
      {
        int ADCMQ = analogRead(A0); //Lemos la salida analógica  del MQ
        float Voltaje = (ADCMQ) * (5.0 / 1023.0); //Convertimos la lectura en un valor de voltaje
        float Rs=1000*((5-Voltaje)/Voltaje);  //Calculamos Rs con un RL de 1k
        double Concentracion=0.4091*pow(Rs/5463, -1.497); // calculamos la concentración  de alcohol con la ecuación obtenida.
        SumaConcentracion += Concentracion;
        delay(1000);
      }
      
      Serial.print("Concentr:" + String(SumaConcentracion/10));
      delay(500);
}

