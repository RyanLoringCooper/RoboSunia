#define MAX_BUFF_SIZE 64

char input[MAX_BUFF_SIZE];
unsigned long lasttime = 0;

void setup() {
  Serial.begin(9600);
  setupControlPins();
  setupDistanceSensor();
  setupEncoders();
  waitForSerialConnection(); 
}

void loop() {
  handleDistSensors();
  controlPWMs();
  handleSerialInput();
  delay(100);
}
