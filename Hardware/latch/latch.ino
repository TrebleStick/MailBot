//General Defines
#define BAUD_RATE 9600


// Global vars
const int LATCH_DELAY_MS = 2000;
int pin = 0;
// --------------------------------------------//
//-------------global functions---------------//

//---------------------------------------------//
void setup(){
  //set 7 digital output pins with the correspnding wire colour
  pinMode(1, OUTPUT); //brown
  pinMode(2, OUTPUT); //orange
  pinMode(3, OUTPUT); //grey
  pinMode(4, OUTPUT); //red
  pinMode(5, OUTPUT); //pink
  pinMode(6, OUTPUT); //blue
  pinMode(7, OUTPUT); //green

  pinMode(9, OUTPUT); //RESET ?


  Serial.begin(BAUD_RATE);
  while (!Serial) {
    ; // wait for serial port to connect. Needed for native USB port only
  }
  //create a start up sequence
}

//-------------------MAIN-------------------//
void loop(){

  if( Serial.available() > 0){

    pin = Serial.parseInt();
    Serial.println(pin);
    // pin = Serial.read() - 48; // remove ASCII encoding
    pin = Serial.parseInt();
    //might need some pre processing here etc

    //Reset device cmd
    if(pin == 9){
      Serial.println(pin, HEX);
      digitalWrite(9, HIGH);
    }
    else {
      Serial.println(pin, HEX);
      digitalWrite(pin, HIGH);
      delay(LATCH_DELAY_MS);
      digitalWrite(pin, LOW);
    }

    // Serial.write(pin);

  }
}
