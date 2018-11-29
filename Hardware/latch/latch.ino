//General Defines
#define BAUD_RATE 9600

// Global vars
const int LATCH_DELAY_MS = 1000;
int pin = 0;

void setup(){
  //set 7 digital output pins with the correspnding wire colour
  pinMode(2, OUTPUT); //brown//black
  pinMode(3, OUTPUT); //orange//orange
  pinMode(4, OUTPUT); //grey//grey
  pinMode(5, OUTPUT); //red//white
  pinMode(6, OUTPUT); //pink//purple
  pinMode(7, OUTPUT); //blue//blue
  pinMode(8, OUTPUT); //green//green

  pinMode(9, OUTPUT); //RESET ? //TODO connect this

  Serial.begin(BAUD_RATE);
  while (!Serial) {
    ; // wait for serial port to connect. Needed for native USB port only
  }
}

//-------------------MAIN-------------------//
void loop(){

  if( Serial.available() > 0){

    pin = Serial.parseInt();
    // pin
    pin++;
    Serial.println(pin);
    // pin = Serial.read() - 48; // remove ASCII encoding
    // pin = Serial.parseInt();
    //might need some pre processing here etc

    //Reset device cmd
    if(pin == 9){
      Serial.println(pin, HEX);
      digitalWrite(9, HIGH);
    }
    else if(pin!=0){
      Serial.println(pin, HEX);
      digitalWrite(pin, HIGH);
      delay(LATCH_DELAY_MS);
      digitalWrite(pin, LOW);
    }
  }
}
