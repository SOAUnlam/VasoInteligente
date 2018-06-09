// DECLARACION DE VARIABLES PARA PINES
const int pinecho = 11;
const int pintrigger = 12;
const int pinled = 13;
const int altura_vaso = 13.5; //Desde la base hasta el sensor
const int hmax = 8.7; //Desde base hasta ultima indicacion

 
// VARIABLES PARA CALCULOS
unsigned int tiempo;
float diametro, area, radio, volumen,distancia_sensor_liquido,altura_liquido;
 
void setup() {
  // PREPARAR LA COMUNICACION SERIAL
  Serial.begin(9600);
  // CONFIGURAR PINES DE ENTRADA Y SALIDA
  pinMode(pinecho, INPUT);
  pinMode(pintrigger, OUTPUT);
  pinMode(13, OUTPUT);

  // valores entrada vaso
  diametro = 7.5;
  radio = diametro / 2;
  area = 3.1415 * radio * radio; // pi x radio al cuadrado

}
 
void loop() {
  // ENVIAR PULSO DE DISPARO EN EL PIN "TRIGGER"
  digitalWrite(pintrigger, LOW);
  delayMicroseconds(2);
  digitalWrite(pintrigger, HIGH);
  // EL PULSO DURA AL MENOS 10 uS EN ESTADO ALTO
  delayMicroseconds(10);
  digitalWrite(pintrigger, LOW);
 
  // MEDIR EL TIEMPO EN ESTADO ALTO DEL PIN "ECHO" EL PULSO ES PROPORCIONAL A LA DISTANCIA MEDIDA
  tiempo = pulseIn(pinecho, HIGH);
 
  // LA VELOCIDAD DEL SONIDO ES DE 340 M/S O 29 MICROSEGUNDOS POR CENTIMETRO
  // DIVIDIMOS EL TIEMPO DEL PULSO ENTRE 58, TIEMPO QUE TARDA RECORRER IDA Y VUELTA UN CENTIMETRO LA ONDA SONORA
  distancia_sensor_liquido = tiempo / 58.001;

  //distancia es el espacio vacio
  // volumen= area*h
  altura_liquido = altura_vaso - distancia_sensor_liquido; //altura lleno
  volumen = area * altura_liquido;


 
  // ENVIAR EL RESULTADO AL MONITOR SERIAL
  Serial.print(altura_liquido);
  Serial.println(" cm");
  Serial.print(volumen);
  Serial.println(" cc");
  delay(500);

 /*
  // ENCENDER EL LED CUANDO SE CUMPLA CON CIERTA DISTANCIA
  if (distancia <= 15) {
    digitalWrite(13, HIGH);
    delay(500);
  } else {
    digitalWrite(13, LOW);
  }
  */
}
