#include <SoftwareSerial.h> 
 
SoftwareSerial ModBluetooth(2, 3); // RX | TX 
 
void setup()  
{ 
    pinMode(13, OUTPUT); 
    digitalWrite(13, LOW);  
     
    ModBluetooth.begin(9600); 
    Serial.begin(9600);  
    ModBluetooth.print("MODULO CONECTADO");  
    ModBluetooth.print("#");  
} 
 
void loop()  
{  
    if (ModBluetooth.available())  
    { 
        char VarChar; 
         
        VarChar = ModBluetooth.read(); 
         
        if(VarChar == 'M') 
        { 
        digitalWrite(13, HIGH); 
        digitalWrite(12, LOW);
        delay(100); 
        ModBluetooth.print("LED ENCENDIDO"); 
        Serial.print("LED ENCENDIDO"); 
        ModBluetooth.print("#"); 
        } 
        if(VarChar == 'F') 
        { 
        digitalWrite(13, LOW); 
        digitalWrite(12, HIGH);
        delay(100); 
        ModBluetooth.print("LED APAGADO#"); 
        Serial.print("LED APAGADO#"); 
        } 
    } 
} 
