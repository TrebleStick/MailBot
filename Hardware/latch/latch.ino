// global variables
int baud_rate = 9600;
int latch_delay_ms = 2000;
int pin = 0;
// --------------------------------------------//
//-------------global functions---------------//

//---------------------------------------------//
void setup(){
  //set 7 digital output pins.
  pinMode(1, OUTPUT); //brown
  pinMode(2, OUTPUT); //orange
  pinMode(3, OUTPUT); //grey
  pinMode(4, OUTPUT); //red
  pinMode(5, OUTPUT); //pink
  pinMode(6, OUTPUT); //blue
  pinMode(7, OUTPUT); //green

  pinMode(9, OUTPUT); //RESET


  Serial.begin(baud_rate);
  while (!Serial) {
    ; // wait for serial port to connect. Needed for native USB port only
  }
  //create a start up sequence
}

//-------------------MAIN-------------------//
void loop(){

  if( Serial.available() > 0){
    // pin = Serial.read() - 48; // remove ASCII encoding
    pin = Serial.parseInt();
    //might need some pre processing here etc

    //Reset device cmd
    if(pin == 9){
      digitalWrite(9, HIGH);
    }
    else if(pin != 0){
      digitalWrite(9, HIGH);
      digitalWrite(pin, HIGH);
      delay(latch_delay_ms);
      digitalWrite(pin, LOW);
      digitalWrite(9, LOW);
    }

    // Serial.write(pin);

    Serial.println(pin, HEX);
  }

  // digitalWrite(3, HIGH);
  // delay(500);
  // digitalWrite(3, LOW);
  // delay(500);

}
