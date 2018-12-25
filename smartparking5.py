from firebase import firebase
import RPi.GPIO as gpio
import time
from datetime import datetime
import json
import os

gpio.setmode(gpio.BCM)

echo=23
trig=24


firebase=firebase.FirebaseApplication('https://smartparking-526aa.firebaseio.com/',None)

print "start"

gpio.setup(trig, gpio.OUT)
gpio.setup(echo, gpio.IN)

try:
    while True:
        gpio.output(trig,False)
        time.sleep(0.5)
        
        gpio.output(trig, True) #make trig pin high and send ultrasonic wave
        time.sleep(0.00001)
        gpio.output(trig,False)
        

        while gpio.input(echo)==0: #when low print time
            pulse_start=time.time()
            
        while gpio.input(echo)==1: #when high print time
            pulse_end=time.time()
            
        pulse_duration=pulse_end-pulse_start #calculate duration
        
        distance=(pulse_duration * 17000) 
        distance=round(distance ,2)
        
        
        
        #print "Distance ", distance, "cm"
        
        if(distance<5.0) :
            print "Distance : %.1f" % distance
            #print "Parking Available : no"
            available="false"
        else  : 
            print "Distance : %.1f" % distance
            #print "Parking Available : yes"
            available="true"
        firebase.put('/','distance',distance)
        firebase.put('/available/','available',available)
        
        
except:
    gpio.cleanup()



