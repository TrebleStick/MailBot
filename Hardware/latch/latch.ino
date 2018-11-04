// global variables
int baud_rate = 57600;
int latch_delay_ms = 250;
// --------------------------------------------//
//-------------global functions---------------//

//---------------------------------------------//
void setup(){
  //set 7 digital output pins.
  pinMode(2, OUTPUT);
  pinMode(3, OUTPUT);
  pinMode(4, OUTPUT);
  pinMode(5, OUTPUT);
  pinMode(6, OUTPUT);
  pinMode(7, OUTPUT);
  pinMode(8, OUTPUT);


  Serial.begin(baud_rate);
  while (!Serial) {
    ; // wait for serial port to connect. Needed for native USB port only
  }
  //create a start up sequence
}

//-------------------MAIN-------------------//
void loop(){

  if( Serial.available() ){
    int pin = Serial.read();
    //might need some pre processing here etc
    //build in a reset code for example.

    digitalWrite(pin, HIGH);
    delay(latch_delay_ms);
    digitalWrite(pin, LOW);
  }

}
